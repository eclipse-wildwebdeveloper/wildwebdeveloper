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
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.ui.preferences;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.services.WorkspaceService;

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

			LanguageServers.forProject(null).withPreferredServer(languageServerDefinition).excludeInactive()
					.collectAll((w, ls) -> CompletableFuture.completedFuture(ls)).thenAccept(
							lss -> lss.stream().forEach(ls -> ls.getWorkspaceService().didChangeConfiguration(params)));
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
