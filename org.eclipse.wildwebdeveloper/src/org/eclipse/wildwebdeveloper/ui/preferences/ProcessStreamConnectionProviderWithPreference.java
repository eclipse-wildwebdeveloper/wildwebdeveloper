/*******************************************************************************
 * Copyright (c) 2022, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.ui.preferences;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This class extends {@link ProcessStreamConnectionProvider} to manage
 * {@link IPreferenceStore} and call
 * {@link WorkspaceService#didChangeConfiguration(DidChangeConfigurationParams)}
 * when the preference store changes.
 *
 */
public abstract class ProcessStreamConnectionProviderWithPreference extends ProcessStreamConnectionProvider
		implements IPropertyChangeListener {

	private static class PreferenceStoreKey {

		public final IPreferenceStore preferenceStore;

		public final String languageServerId;

		public PreferenceStoreKey(IPreferenceStore preferenceStore, String languageServerId) {
			super();
			this.preferenceStore = preferenceStore;
			this.languageServerId = languageServerId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(languageServerId, preferenceStore);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PreferenceStoreKey other = (PreferenceStoreKey) obj;
			return Objects.equals(languageServerId, other.languageServerId)
					&& Objects.equals(preferenceStore, other.preferenceStore);
		}
	}

	private static class PropertyChangeListenerWrapper implements IPropertyChangeListener {

		private final IPropertyChangeListener listener;

		private int languageServerStartedNumber;

		public PropertyChangeListenerWrapper(IPropertyChangeListener listener) {
			this.listener = listener;
		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			listener.propertyChange(event);
		}

		public boolean hasNoServerStarted() {
			return languageServerStartedNumber == 0;
		}

		public void startServer() {
			languageServerStartedNumber++;
		}

		public void endServer() {
			languageServerStartedNumber--;
		}

	}

	// Map used to store a single property change listener for a given
	// preferenceStore/languageServerId
	private static final Map<PreferenceStoreKey, PropertyChangeListenerWrapper> listenerPerLanguageServer = new HashMap<>();

	private final PreferenceStoreKey preferenceStoreKey;

	private final String[] supportedSections;

	private LanguageServerDefinition languageServerDefinition;

	public ProcessStreamConnectionProviderWithPreference(String languageServerId, IPreferenceStore preferenceStore,
			String[] supportedSections) {
		this.preferenceStoreKey = new PreferenceStoreKey(preferenceStore, languageServerId);
		this.supportedSections = supportedSections;
	}

	@Override
	public void start() throws IOException {
		super.start();
		addPropertyChangeListenerIfNeed();
	}

	private void addPropertyChangeListenerIfNeed() {
		synchronized (listenerPerLanguageServer) {
			PropertyChangeListenerWrapper listener = listenerPerLanguageServer.get(preferenceStoreKey);
			if (listener == null) {
				listener = new PropertyChangeListenerWrapper(this);
				listenerPerLanguageServer.put(preferenceStoreKey, listener);
			}
			if (listener.hasNoServerStarted()) {
				preferenceStoreKey.preferenceStore.addPropertyChangeListener(listener);
			}
			listener.startServer();
		}
	}

	@Override
	public void stop() {
		removePropertyChangeListenerIfNeed();
		super.stop();
	}

	private void removePropertyChangeListenerIfNeed() {
		synchronized (listenerPerLanguageServer) {
			PropertyChangeListenerWrapper listener = listenerPerLanguageServer.get(preferenceStoreKey);
			if (listener != null) {
				listener.endServer();
				if (listener.hasNoServerStarted()) {
					preferenceStoreKey.preferenceStore.removePropertyChangeListener(listener);
					listenerPerLanguageServer.remove(preferenceStoreKey);
				}
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (isAffected(event)) {
		   LanguageServerDefinition languageServerDefinition = getLanguageServerDefinition();
			@SuppressWarnings("rawtypes")
			DidChangeConfigurationParams params = new DidChangeConfigurationParams(createSettings());

			/*
			 * Fan-out strategy and rationale
			 * --------------------------------
			 * We must deliver didChangeConfiguration to every running instance of the
			 * language server for this provider's id, regardless of how LSP4E can find it.
			 * LSP4E discovery varies by scope, so we notify in 3 passes and de-duplicate:
			 * 1) Workspace-wide (null project): catches singleton or workspace-folder-aware servers.
			 * 2) Per-project: picks up per-project servers that don't expose workspace folders (eg JSTS).
			 * 3) Per-document (open editors): covers files outside the workspace or not yet tied to a project.
			 *
			 * Note: withPreferredServer(...) only reorders candidates; it does not filter.
			 * We therefore compare wrapper.serverDefinition for equality and track already
			 * notified LanguageServer proxies to avoid duplicate notifications across scopes.
			 * excludeInactive() is intentional to avoid starting servers as a side-effect
			 * of a preference change.
			 */

			final Set<LanguageServer> notifiedServers = ConcurrentHashMap.newKeySet();

			// 1) Workspace-wide: singleton or workspace-folder-aware servers
			LanguageServers.forProject(null).withPreferredServer(languageServerDefinition).excludeInactive()
					.collectAll((wrapper, server) -> {
						if (languageServerDefinition.equals(wrapper.serverDefinition) && notifiedServers.add(server)) {
							server.getWorkspaceService().didChangeConfiguration(params);
						}
						return CompletableFuture.completedFuture(null);
					});

			// 2) Per-project: include servers that don't support workspace folders (they won't be returned by forProject(null))
			for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				if (!project.isOpen())
					continue;

				LanguageServers.forProject(project).withPreferredServer(languageServerDefinition).excludeInactive()
						.collectAll((wrapper, server) -> {
							if (languageServerDefinition.equals(wrapper.serverDefinition) && notifiedServers.add(server)) {
								server.getWorkspaceService().didChangeConfiguration(params);
							}
							return CompletableFuture.completedFuture(null);
						});
			}

			// 3) Per-document: open editors (covers external files or untied docs)
			for (final IWorkbenchWindow win : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				for (final IWorkbenchPage page : win.getPages()) {
					for (final IEditorReference ref : page.getEditorReferences()) {
					   final IEditorPart editor = ref.getEditor(false); // do not restore unopened editors
						if (editor == null)
							continue;

						final IDocument doc = LSPEclipseUtils.getDocument(editor.getEditorInput());
						if (doc == null)
							continue;

						LanguageServers.forDocument(doc).withPreferredServer(languageServerDefinition)
								.collectAll((wrapper, server) -> {
									if (languageServerDefinition.equals(wrapper.serverDefinition) && notifiedServers.add(server)) {
										server.getWorkspaceService().didChangeConfiguration(params);
									}
									return CompletableFuture.completedFuture(null);
								});
					}
				}
			}
		}
	}

	private LanguageServerDefinition getLanguageServerDefinition() {
		if (languageServerDefinition == null) {
			languageServerDefinition = LanguageServersRegistry.getInstance()
					.getDefinition(preferenceStoreKey.languageServerId);
		}
		return languageServerDefinition;
	}

	protected boolean isAffected(PropertyChangeEvent event) {
		String property = event.getProperty();
		for (String supportedSection : supportedSections) {
			if (isMatchSection(property, supportedSection)) {
				return true;
			}
		}
		return false;
	}

	protected abstract Object createSettings();
}
