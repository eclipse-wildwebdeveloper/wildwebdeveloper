/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.markdown;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.client.DefaultLanguageClient;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;
import org.eclipse.wildwebdeveloper.util.FileUtils;

import com.google.gson.Gson;

/**
 * LSP client-side handlers for vscode-markdown-languageserver custom requests.
 *
 * See https://github.com/microsoft/vscode-markdown-languageserver#custom-requests
 */
public final class MarkdownLanguageClient extends DefaultLanguageClient {

	private static Path resolveResource(final String resourcePath) throws IOException {
		try {
			final URL url = FileLocator.toFileURL(MarkdownLanguageClient.class.getResource(resourcePath));
			return Paths.get(url.toURI()).toAbsolutePath();
		} catch (URISyntaxException ex) {
			throw new IOException("Failed to resolve resource URI: " + resourcePath, ex);
		}
	}

	private static final class Watcher implements IResourceChangeListener {
		final int id;
		final Path watchRoot;
		final boolean ignoreCreate;
		final boolean ignoreChange;
		final boolean ignoreDelete;
		final MarkdownLanguageServerAPI server;

		Watcher(final MarkdownLanguageServerAPI server, final int id, Path watchRoot,
				final boolean ignoreCreate, final boolean ignoreChange, final boolean ignoreDelete) {
			this.server = server;
			this.id = id;
			this.watchRoot = watchRoot;
			this.ignoreCreate = ignoreCreate;
			this.ignoreChange = ignoreChange;
			this.ignoreDelete = ignoreDelete;
		}

		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			final IResourceDelta delta = event.getDelta();
			if (delta == null)
				return;
			try {
				delta.accept(deltaNode -> { // visit every changed node in this delta tree
					final IResource res = deltaNode.getResource();

					// only care about file-level changes
					if (res == null || res.getType() != IResource.FILE)
						return true;

					final URI uri = res.getLocationURI();
					if (uri == null)
						return true;

					// Constrain events to the configured watcher baseUri. If resolution fails
					// or the path is outside the base, skip this node.
					if (watchRoot != null) {
						try {
							if (!Paths.get(uri).startsWith(watchRoot))
								return true; // outside watched root
						} catch (final Exception ex) {
							return true; // resolution error: ignore
						}
					}

					// Map delta kind to watcher kind, honoring ignore* options.
					final String kind;
					switch (deltaNode.getKind()) {
						case IResourceDelta.ADDED:
							if (ignoreCreate)
								return true;
							kind = "create";
							break;
						case IResourceDelta.REMOVED:
							if (ignoreDelete)
								return true;
							kind = "delete";
							break;
						case IResourceDelta.CHANGED:
							if (ignoreChange)
								return true;
							// Only content changes; skip metadata-only changes
							if ((deltaNode.getFlags() & IResourceDelta.CONTENT) == 0)
								return true;
							kind = "change";
							break;
						default:
							return true;
					}

					// Notify server of the file system event for this resource
					final var payload = new HashMap<String, Object>();
					payload.put("id", Integer.valueOf(id));
					payload.put("uri", normalizeFileUriForLanguageServer(uri));
					payload.put("kind", kind);
					server.fsWatcherOnChange(payload);
					return true;
				});
			} catch (final Exception ex) {
				ILog.get().warn(ex.getMessage(), ex);
			}
		}
	}

	private static volatile String mdParseHelperPath;

	private final Map<Integer, Watcher> watchersById = new ConcurrentHashMap<>();

	public MarkdownLanguageClient() throws IOException {
		if (mdParseHelperPath == null) {
			mdParseHelperPath = resolveResource("md-parse.js").toString();
		}
	}

	@Override
	public CompletableFuture<List<Object>> configuration(final ConfigurationParams params) {
		return CompletableFuture.supplyAsync(() -> {
			final var results = new ArrayList<>();
			for (final ConfigurationItem item : params.getItems()) {
				final String section = item.getSection();
				if (MarkdownPreferences.isMatchMarkdownSection(section)) {
					final Settings md = MarkdownPreferences.getGlobalSettings();
					results.add(md.findSettings(section.split("[.]")));
				} else {
					results.add(null);
				}
			}
			return results;
		});
	}

	// Acknowledge diagnostic refresh requests and trigger a diagnostic pull
	@Override
	public CompletableFuture<Void> refreshDiagnostics() {
		return CompletableFuture.runAsync(() -> MarkdownDiagnosticsManager.refreshAllOpenMarkdownFiles(getLanguageServer()));
	}

	/**
	 * <pre>
	 * markdown/parse
	 * Request: { uri: string; text?: string }
	 * Response: md.Token[] (serialized markdown-it token objects)
	 * </pre>
	 */
	@JsonRequest("markdown/parse")
	public CompletableFuture<List<Map<String, Object>>> parseMarkdown(final Map<String, Object> params) {
		return CompletableFuture.supplyAsync(() -> {
			Path tmp = null;
			try {
				String text = null;
				final boolean hasText = params != null && params.get("text") instanceof String;
				if (hasText) {
					text = (String) params.get("text");
				}
				Path uriPath = null;
				if (params != null) {
					final var uri = params.get("uri");
					if (uri != null) {
						final Path p = FileUtils.uriToPath(uri.toString());
						if (Files.exists(p)) {
							uriPath = p;
						}
					}
				}
				if (!hasText && uriPath == null) {
					return List.of();
				}
				final String argPath;
				if (hasText) {
					tmp = Files.createTempFile("wwd-md-", ".md");
					Files.writeString(tmp, text, StandardCharsets.UTF_8);
					argPath = tmp.toAbsolutePath().toString();
				} else {
					argPath = uriPath.toAbsolutePath().toString();
				}
				final var pb = new ProcessBuilder(List.of(NodeJSManager.getNodeJsLocation().getAbsolutePath(), mdParseHelperPath, argPath));
				final Process proc = pb.start();
				final var out = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
				final int exit = proc.waitFor();
				if (exit != 0) {
					final var err = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
					ILog.get().warn("markdown-it parser failed (" + exit + "): " + err, null);
					return List.of();
				}
				final var gson = new Gson();
				final List<Map<String, Object>> tokens = gson.fromJson(out, List.class);
				// Opportunistically trigger a diagnostic pull for this document
				try {
					final var uri = URI.create((String) params.get("uri"));
					final var res = LSPEclipseUtils.findResourceFor(uri);
					if (res instanceof final IFile file) {
						CompletableFuture.runAsync(() -> MarkdownDiagnosticsManager.refreshFile(file));
					}
				} catch (final Exception ignore) {
				}
				return tokens != null ? tokens : List.of();
			} catch (final InterruptedException ex) {
				ILog.get().warn(ex.getMessage(), ex);
				/* Clean up whatever needs to be handled before interrupting  */
				Thread.currentThread().interrupt();
			} catch (final Exception ex) {
				ILog.get().warn(ex.getMessage(), ex);
			} finally {
				if (tmp != null)
					try {
						Files.deleteIfExists(tmp);
					} catch (final Exception ignore) {
					}
			}
			return List.of();
		});
	}

	/**
	 * <pre>
	 * Request: {}
	 * Response: string[] (URIs of Markdown files in the workspace)
	 * </pre>
	 */
	@JsonRequest("markdown/findMarkdownFilesInWorkspace")
	public CompletableFuture<List<String>> findMarkdownFilesInWorkspace(final Object unused) {
		return CompletableFuture.supplyAsync(() -> {
			final var uris = new ArrayList<String>();
			// Compile exclude globs from preferences once per request
			final String[] excludeGlobs = MarkdownPreferences.getSuggestPathsExcludeGlobs();
			final List<PathMatcher> excludeMatchers = compileGlobMatchers(excludeGlobs);
			try {
				final var roots = MarkdownLanguageServer.getServerRoots();
				if (roots != null && !roots.isEmpty()) {
					for (String rootUri : roots) {
						final var containers = ResourcesPlugin.getWorkspace().getRoot()
								.findContainersForLocationURI(URI.create(rootUri));
						if (containers != null && containers.length > 0) {
							for (final var container : containers) {
								if (container.isDerived() || container.isHidden())
									continue;
								container.accept((final IResource res) -> {
									if (res.isDerived() || res.isHidden())
										return false;
									if (res.getType() == IResource.FILE) {
										final String name = res.getName().toLowerCase();
										if ((name.endsWith(".md") || name.endsWith(".markdown") || name.endsWith(".mdown"))
												&& !isExcludedByGlobs(res, excludeMatchers)) {
											uris.add(normalizeFileUriForLanguageServer(res.getLocationURI()));
										}
										return false; // no children
									}
									return true; // continue
								});
							}
						}
					}
				} else {
					// Fallback: scan entire workspace
					final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
					wsRoot.accept((final IResource res) -> {
						if (res.isDerived() || res.isHidden())
							return false;
						if (res.getType() == IResource.FILE) {
							final String name = res.getName().toLowerCase();
							if ((name.endsWith(".md") || name.endsWith(".markdown") || name.endsWith(".mdown"))
									&& !isExcludedByGlobs(res, excludeMatchers)) {
								uris.add(normalizeFileUriForLanguageServer(res.getLocationURI()));
							}
							return false; // no children
						}
						return true; // continue
					});
				}
			} catch (final Exception ex) {
				ILog.get().warn(ex.getMessage(), ex);
			}
			return uris;
		});
	}

	private static List<PathMatcher> compileGlobMatchers(String... globs) {
		if (globs == null || globs.length == 0)
			return List.of();

		var fs = FileSystems.getDefault();
		var matchers = new ArrayList<PathMatcher>();

		for (String glob : globs) {
			if (glob == null || (glob = glob.trim()).isEmpty())
				continue;

			// If pattern starts with "**/", also add a root-level variant without it.
			// This makes "**/node_modules/**" also match "node_modules/**".
			List<String> patterns = glob.startsWith("**/") && glob.length() > 3
					? List.of(glob, glob.substring(3))
					: List.of(glob);

			for (String pattern : patterns) {
				try {
					matchers.add(fs.getPathMatcher("glob:" + pattern));
				} catch (Exception ex) {
					ILog.get().warn(ex.getMessage(), ex);
				}
			}
		}
		return matchers;
	}

	private static boolean isExcludedByGlobs(final IResource res, final List<PathMatcher> matchers) {
		if (matchers == null || matchers.isEmpty())
			return false;
		final IPath pr = res.getProjectRelativePath();
		if (pr == null)
			return false;
		final Path p = pr.toPath();
		for (final PathMatcher m : matchers) {
			if (m.matches(p))
				return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * Request: { uri: string }
	 * Response: [string, { isDirectory: boolean }][]
	 * </pre>
	 */
	@JsonRequest("markdown/fs/readDirectory")
	public CompletableFuture<List<List<Object>>> fsReadDirectory(final Map<String, Object> params) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				final var uri = params.get("uri");
				if (uri == null)
					return List.of();

				return Files.list(FileUtils.uriToPath(uri.toString())) //
						.map(child -> List.of( //
								child.getFileName().toString(), //
								Map.of("isDirectory", Files.isDirectory(child)))) //
						.toList();
			} catch (final IOException ex) {
				throw new UncheckedIOException(ex);
			}
		});
	}

	/**
	 * <pre>
	 * Request: { uri: string }
	 * Response: number[] (file bytes 0-255)
	 * </pre>
	 */
	@JsonRequest("markdown/fs/readFile")
	public CompletableFuture<List<Integer>> fsReadFile(final Map<String, Object> params) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				final var uri = params.get("uri");
				if (uri == null)
					return List.of();

				final var path = FileUtils.uriToPath(uri.toString());
				final byte[] bytes = Files.readAllBytes(path);
				final var out = new ArrayList<Integer>(bytes.length);
				for (final byte b : bytes) {
					out.add(Byte.toUnsignedInt(b));
				}
				return out;
			} catch (final IOException ex) {
				throw new UncheckedIOException(ex);
			}
		});
	}

	/**
	 * <pre>
	 * Request: { uri: string }
	 * Response: { isDirectory: boolean } | undefined (null here represents undefined)
	 * </pre>
	 */
	@JsonRequest("markdown/fs/stat")
	public CompletableFuture<Map<String, Object>> fsStat(final Map<String, Object> params) {
		return CompletableFuture.supplyAsync(() -> {
			final var uri = params.get("uri");
			if (uri == null)
				return null;
			final var file = FileUtils.uriToFile(uri.toString());
			if (!file.exists())
				return null;
			final var result = new HashMap<String, Object>(1, 1f);
			result.put("isDirectory", Boolean.valueOf(file.isDirectory()));
			return result;
		});
	}

	/**
	 * <pre>
	 * Request: {
	 *   id: number;
	 *   uri: string; // file: URI of file or directory to watch
	 *   options: { ignoreCreate?: boolean, ignoreChange?: boolean, ignoreDelete?: boolean };
	 *   watchParentDirs: boolean;
	 * }
	 * Response: void
	 * </pre>
	 */
	@JsonRequest("markdown/fs/watcher/create")
	public CompletableFuture<Void> fsWatcherCreate(final Map<String, Object> params) {
		return CompletableFuture.supplyAsync(() -> {
			final int id = ((Number) params.get("id")).intValue();

			final var uri = params.get("uri");
			if (uri == null)
				return null;
			final var path = FileUtils.uriToPath(uri.toString());

			@SuppressWarnings("unchecked")
			final Map<String, Object> options = params.get("options") instanceof Map //
					? (Map<String, Object>) params.get("options")
					: Map.of();
			final var watcher = new Watcher((MarkdownLanguageServerAPI) getLanguageServer(), id, path, //
					Boolean.TRUE.equals(options.get("ignoreCreate")), //
					Boolean.TRUE.equals(options.get("ignoreChange")), //
					Boolean.TRUE.equals(options.get("ignoreDelete")));
			ResourcesPlugin.getWorkspace().addResourceChangeListener(watcher, IResourceChangeEvent.POST_CHANGE);
			watchersById.put(Integer.valueOf(id), watcher);
			return null;
		});
	}

	/**
	 * <pre>
	 * Request: { id: number }
	 * Response: void
	 * </pre>
	 */
	@JsonRequest("markdown/fs/watcher/delete")
	public CompletableFuture<Void> fsWatcherDelete(final Map<String, Object> params) {
		return CompletableFuture.supplyAsync(() -> {
			final Object idObj = params != null ? params.get("id") : null;
			if (idObj instanceof final Number id) {
				final Watcher watcher = watchersById.remove(id.intValue());
				if (watcher != null) {
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(watcher);
				}
			}
			return null;
		});
	}

	/**
	 * Normalize Windows file URIs to match vscode-uri's URI.toString() form used by the server.
	 * <br>
	 * Examples:
	 * <li>file:/D:/path -> file:///d%3A/path
	 * <li>file:///D:/path -> file:///d%3A/path
	 */
	private static String normalizeFileUriForLanguageServer(final URI uri) {
		if (uri == null)
			return null;
		if (!FileUtils.FILE_SCHEME.equalsIgnoreCase(uri.getScheme()))
			return uri.toString();
		final String uriAsString = uri.toString();

		// Ensure triple slash prefix
		String withoutScheme = uriAsString.substring("file:".length()); // could be :/, :///
		while (withoutScheme.startsWith("/")) {
			withoutScheme = withoutScheme.substring(1);
		}

		// Expect leading like D:/ or d:/ on Windows
		if (withoutScheme.length() >= 2 && Character.isLetter(withoutScheme.charAt(0)) && withoutScheme.charAt(1) == ':') {
			final char drive = Character.toLowerCase(withoutScheme.charAt(0));
			final String rest = withoutScheme.substring(2); // drop ':'
			return "file:///" + drive + "%3A" + (rest.startsWith("/") ? rest : "/" + rest);
		}

		// Already in a normalized or UNC form; fall back to original
		return uriAsString;
	}
}
