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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences;
import org.eclipse.wildwebdeveloper.ui.preferences.ProcessStreamConnectionProviderWithPreference;

/**
 * Launches the embedded Node.js based Markdown language server.
 *
 * See https://github.com/microsoft/vscode-markdown-languageservice
 *
 * @author Sebastian Thomschke
 */
public final class MarkdownLanguageServer extends ProcessStreamConnectionProviderWithPreference {

	static final String MARKDOWN_LANGUAGE_SERVER_ID = "org.eclipse.wildwebdeveloper.markdown";

	// If/when Markdown preferences are added, list their root sections here
	private static final String[] SUPPORTED_SECTIONS = { "markdown" };

	private static volatile String markdownLanguageServerPath;
	private static volatile String proxyPath;

	// Track roots with ref-counts to avoid leaks when servers stop
	private static final ConcurrentHashMap<String, AtomicInteger> SERVER_ROOT_COUNTS = new ConcurrentHashMap<>();
	private String instanceRootUri;

	public static Set<String> getServerRoots() {
		return Collections.unmodifiableSet(SERVER_ROOT_COUNTS.keySet());
	}

	private static Path resolveResource(String resourcePath) throws IOException {
		try {
			URL url = FileLocator.toFileURL(MarkdownLanguageServer.class.getResource(resourcePath));
			return Paths.get(url.toURI()).toAbsolutePath();
		} catch (URISyntaxException ex) {
			throw new IOException("Failed to resolve resource URI: " + resourcePath, ex);
		}
	}

	public MarkdownLanguageServer() throws IOException {
		super(MARKDOWN_LANGUAGE_SERVER_ID, Activator.getDefault().getPreferenceStore(), SUPPORTED_SECTIONS);

		if (markdownLanguageServerPath == null) {
			markdownLanguageServerPath = resolveResource("/node_modules/vscode-markdown-languageserver/dist/node/workerMain.js").toString();
		}
		if (proxyPath == null) {
			proxyPath = resolveResource("md-lsp-proxy.js").toString();
		}
		setCommands(List.of(
				NodeJSManager.getNodeJsLocation().getAbsolutePath(),
				proxyPath,
				markdownLanguageServerPath,
				"--stdio"));
		setWorkingDirectory(System.getProperty("user.dir"));
	}

	@Override
	public Map<String, Object> getInitializationOptions(final URI projectRootUri) {
		final Map<String, Object> options = new HashMap<>();

		if (projectRootUri != null) {
			setWorkingDirectory(projectRootUri.getRawPath());

			// Remember this root for scoping client-side workspace queries
			instanceRootUri = projectRootUri.toString();
			SERVER_ROOT_COUNTS.compute(instanceRootUri, (k, v) -> {
				if (v == null)
					return new AtomicInteger(1);
				v.incrementAndGet();
				return v;
			});
		}

		// https://github.com/microsoft/vscode-markdown-languageserver#initialization-options
		options.put("markdownFileExtensions", List.of("md", "markdown", "mdown"));
		return options;
	}

	@Override
	protected Object createSettings() {
		return MarkdownPreferences.getGlobalSettings();
	}

	@Override
	public void stop() {
		if (instanceRootUri != null) {
			SERVER_ROOT_COUNTS.computeIfPresent(instanceRootUri, (k, v) -> v.decrementAndGet() <= 0 ? null : v);
			instanceRootUri = null;
		}
		super.stop();
	}

	@Override
	public void handleMessage(final Message message, final LanguageServer languageServer, final URI rootUri) {
		if (message instanceof final ResponseMessage response) {
			if (response.getResult() instanceof InitializeResult) {
				final var params = new DidChangeConfigurationParams(createSettings());
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}
}
