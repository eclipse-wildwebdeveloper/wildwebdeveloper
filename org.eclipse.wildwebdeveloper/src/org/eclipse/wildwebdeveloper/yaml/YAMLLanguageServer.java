/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.yaml;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.SchemaAssociationRegistry;
import org.eclipse.wildwebdeveloper.SchemaAssociationsPreferenceInitializer;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.json.JSonLanguageServer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class YAMLLanguageServer extends ProcessStreamConnectionProvider {
	private static final String YAML_KEY = "yaml";
	private static final String VALIDATE_KEY = "validate";
	private static final String COMPLETION_KEY = "completion";
	private static final String HOVER_KEY = "hover";
	private static final String SCHEMAS_KEY = "schemas";

	private static final IPreferenceStore PREFERENCE_STORE = Activator.getDefault().getPreferenceStore();
	private static final LanguageServerDefinition YAML_LS_DEFINITION = LanguageServersRegistry.getInstance()
			.getDefinition("org.eclipse.wildwebdeveloper.yaml");
	private static final IPropertyChangeListener PROPERTY_CHANGE_LISTENER = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE.equals(event.getProperty())) {
				Map<String, Object> settings = new HashMap<>();
				settings.put(YAML_KEY, getYamlConfigurationOptions());

				DidChangeConfigurationParams params = new DidChangeConfigurationParams(settings);
				LanguageServiceAccessor.getActiveLanguageServers(null).stream()
						.filter(server -> YAML_LS_DEFINITION
								.equals(LanguageServiceAccessor.resolveServerDefinition(server).get()))
						.forEach(ls -> ls.getWorkspaceService().didChangeConfiguration(params));
			}
		}
	};

	public YAMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator
					.toFileURL(getClass().getResource("/node_modules/yaml-language-server/out/server/src/server.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage) message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				Map<String, Object> settings = new HashMap<>();
				settings.put(YAML_KEY, getYamlConfigurationOptions());

				DidChangeConfigurationParams params = new DidChangeConfigurationParams(settings);
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}

	static Map<String, Object> getYamlConfigurationOptions() {
		Map<String, Object> yaml = new HashMap<>();
		yaml.put(SCHEMAS_KEY, getSchemaAssociations());
		yaml.put(VALIDATE_KEY, true);
		yaml.put(COMPLETION_KEY, true);
		yaml.put(HOVER_KEY, true);
		yaml.put("schemaStore.enable", true);
		return yaml;
	}

	private static Map<String, Object> getSchemaAssociations() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String schemaString = preferenceStore
				.getString(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE);

		Map<String, Object> contentTypeAssociations = new Gson().fromJson(schemaString,
				new TypeToken<HashMap<String, String>>() {
				}.getType());

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType yamlBaseContentType = contentTypeManager.getContentType("org.eclipse.wildwebdeveloper.yaml");

		Map<String, Object> associations = new HashMap<>();

		contentTypeAssociations.forEach((key, value) -> {
			IContentType contentType = contentTypeManager.getContentType(key);
			if (contentType != null && contentType.getBaseType().equals(yamlBaseContentType)) {
				String[] fileNames = contentType.getFileSpecs(IContentType.FILE_NAME_SPEC);
				for (String fileName : fileNames) {
					associations.put(value.toString(), fileName);
				}

				String[] filePatterns = contentType.getFileSpecs(IContentType.FILE_PATTERN_SPEC);
				for (String pattern : filePatterns) {
					associations.put(value.toString(), pattern);
				}

				String[] fileExtensions = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				for (String extension : fileExtensions) {
					associations.put(value.toString(), "*." + extension);
				}
			}
		});
		
		IConfigurationElement[] conf = Platform.getExtensionRegistry().getConfigurationElementsFor(JSonLanguageServer.SCHEMA_EXT);
		for (IConfigurationElement el : conf) {
			String url = el.getAttribute(JSonLanguageServer.URL_ATTR);
			String pattern = el.getAttribute(JSonLanguageServer.PATTERN_ATTR);
			if (!url.isBlank() && !pattern.isBlank()) {
				associations.put(SchemaAssociationRegistry.translate(url), pattern);
			}
		}

		return associations;
	}

	@Override
	public String toString() {
		return "YAML Language Server: " + super.toString();
	}

	@Override
	public void start() throws IOException {
		super.start();
		PREFERENCE_STORE.addPropertyChangeListener(PROPERTY_CHANGE_LISTENER);
	}

	@Override
	public void stop() {
		PREFERENCE_STORE.removePropertyChangeListener(PROPERTY_CHANGE_LISTENER);
		super.stop();
	}
}
