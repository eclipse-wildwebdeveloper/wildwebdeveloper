/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Handles the {@code xml.open.uri} command sent by LemMinX via a code lens,
 * opening the referenced grammar file in the editor.
 *
 * <p>
 * Supports two URI schemes:
 * <ul>
 * <li>{@code file:} — opened directly via EFS (e.g. cached XSD on disk)</li>
 * <li>{@code jar:} — content read from the JAR entry and opened read-only via
 * {@link IStorageEditorInput} (e.g. embedded xml.xsd inside a plugin JAR)</li>
 * </ul>
 *
 * @author Angelo ZERR
 */
public class OpenUriHandler extends LSPCommandHandler {

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String uri = ArgumentsUtils.getArgAt(command, 0, String.class);
		if (uri == null) {
			return null;
		}
		try {
			if (uri.startsWith("jar:")) {
				var url = URI.create(uri).toURL();
				String entryPath = url.getPath();
				String entryName = entryPath.substring(entryPath.lastIndexOf('/') + 1);
				// Read JAR bytes in a background Job to avoid blocking the workbench thread.
				Job.create("Opening " + entryName, _monitor -> {
					try {
						byte[] content;
						try (InputStream in = url.openStream()) {
							content = in.readAllBytes();
						}
						PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
							try {
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
										.getActivePage();
								openJarEditor(entryName, content, uri, page);
							} catch (Exception e) {
								ILog.get().error("Error opening URI: " + uri, e);
							}
						});
					} catch (Exception e) {
						ILog.get().error("Error opening URI: " + uri, e);
					}
				}).schedule();
			} else if (uri.startsWith("http:") || uri.startsWith("https:")) {
				// Remote schemas are not cached when downloadExternalResources is disabled;
				// EFS cannot open them, so open in the browser instead.
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
					try {
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
								.openURL(URI.create(uri).toURL());
					} catch (Exception e) {
						ILog.get().error("Error opening URI in browser: " + uri, e);
					}
				});
			} else {
				IFileStore fileStore = EFS.getStore(URI.create(uri));
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (Exception e) {
						ILog.get().error("Error opening URI: " + uri, e);
					}
				});
			}
		} catch (Exception e) {
			ILog.get().error("Error opening URI: " + uri, e);
		}
		return null;
	}

	private static IEditorPart openJarEditor(String entryName, byte[] content, String uri, IWorkbenchPage page)
			throws Exception {
		IStorage storage = new IStorage() {
			@Override
			public InputStream getContents() {
				return new ByteArrayInputStream(content);
			}

			@Override
			public IPath getFullPath() {
				return null;
			}

			@Override
			public String getName() {
				return entryName;
			}

			@Override
			public boolean isReadOnly() {
				return true;
			}

			@Override
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}
		};
		String editorId = IDE.getEditorDescriptor(entryName, true, false).getId();
		return page.openEditor(new JarEditorInput(storage, entryName, uri), editorId);
	}

	/**
	 * Editor input for JAR entries, opened read-only via
	 * {@link IStorageEditorInput}.
	 * <p>
	 * Note: {@code jar:} URIs are intentionally not exposed via
	 * {@code IURIEditorInput}. Eclipse EFS has no file-system provider for the
	 * {@code jar:} scheme, so LSP4E would fail (e.g. {@code EFS.getStore(jar:...)}
	 * in its outline adapter) if it tried to treat this editor input as a
	 * URI-backed resource.
	 * </p>
	 */
	static final class JarEditorInput implements IStorageEditorInput {

		private final IStorage storage;
		private final String name;
		private final URI uri;

		JarEditorInput(IStorage storage, String name, String rawUri) {
			this.storage = storage;
			this.name = name;
			this.uri = URI.create(rawUri);
		}

		@Override
		public IStorage getStorage() {
			return storage;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return uri.toString();
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof JarEditorInput other && uri.equals(other.uri);
		}

		@Override
		public int hashCode() {
			return uri.hashCode();
		}
	}

}
