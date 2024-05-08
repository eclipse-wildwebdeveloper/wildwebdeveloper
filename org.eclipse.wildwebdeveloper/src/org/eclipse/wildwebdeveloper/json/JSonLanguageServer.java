/*******************************************************************************
 * Copyright (c) 2016, 2023 Rogue Wave Software Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Michał Niewrzal (Rogue Wave Software Inc.) - initial implementation
 *  Angelo Zerr <angelo.zerr@gmail.com> - JSON Schema support
 *  Dawid Pakuła <zulus@w3des.net> - JSON Schema extension point
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.SchemaAssociationRegistry;
import org.eclipse.wildwebdeveloper.SchemaAssociationsPreferenceInitializer;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class JSonLanguageServer extends ProcessStreamConnectionProvider {
	
	public final static String SCHEMA_EXT = "org.eclipse.wildwebdeveloper.json.schema"; //$NON-NLS-1$
	public final static String PATTERN_ATTR = "pattern"; //$NON-NLS-1$
	public final static String URL_ATTR = "url"; //$NON-NLS-1$

	private static final IPreferenceStore PREFERENCE_STORE = Activator.getDefault().getPreferenceStore();
	private static final LanguageServerDefinition JSON_LS_DEFINITION = LanguageServersRegistry.getInstance()
			.getDefinition("org.eclipse.wildwebdeveloper.json");
	private static final IPropertyChangeListener PROPERTY_CHANGE_LISTENER = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE.equals(event.getProperty())) {
				Map<String, List<String>> associations = getSchemaAssociations();

				LanguageServers.forProject(null).withPreferredServer(JSON_LS_DEFINITION).excludeInactive()
						.collectAll((w, ls) -> CompletableFuture.completedFuture(ls)).thenAccept(
								lss -> lss.stream().forEach(ls -> ((JSonLanguageServerInterface) ls).sendJSonchemaAssociations(associations)));
			}
		}
	};

	public JSonLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator.toFileURL(
				getClass().getResource("/node_modules/vscode-json-languageserver/dist/node/jsonServerMain.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage responseMessage) {
			if (responseMessage.getResult() instanceof InitializeResult) {
				// Send json/schemaAssociations notification to register JSON Schema on JSON
				// Language server side.
				JSonLanguageServerInterface server = (JSonLanguageServerInterface) languageServer;
				server.sendJSonchemaAssociations(getSchemaAssociations());
			}
		}
	}
	
	private static Map<String, List<String>> getSchemaAssociations() {
		Map<String, List<String>> associations = new HashMap<>();
		fillSchemaAssociationsFromPreferenceStore(associations);
		fillSchemaAssociationsFromExtensionPoint(associations);
		return associations;
	}

	private static void fillSchemaAssociationsFromPreferenceStore(Map<String, List<String>> associations) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String schemaString = preferenceStore
				.getString(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE);

		Map<String, String> contentTypeAssociations = new Gson().fromJson(schemaString,
				new TypeToken<HashMap<String, String>>() {
				}.getType());

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType jsonBaseContentType = contentTypeManager.getContentType("org.eclipse.wildwebdeveloper.json");

		contentTypeAssociations.forEach((key, value) -> {
			IContentType contentType = contentTypeManager.getContentType(key);
			if (contentType != null && contentType.getBaseType().equals(jsonBaseContentType)) {
				String[] fileNames = contentType.getFileSpecs(IContentType.FILE_NAME_SPEC);
				for (String fileName : fileNames) {
					associations.put(fileName, Arrays.asList(value));
				}

				String[] filePatterns = contentType.getFileSpecs(IContentType.FILE_PATTERN_SPEC);
				for (String pattern : filePatterns) {
					associations.put(pattern, Arrays.asList(value));
				}

				String[] fileExtensions = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				for (String extension : fileExtensions) {
					associations.put("*." + extension, Arrays.asList(value));
				}
			}
		});
	}
	
	private static void fillSchemaAssociationsFromExtensionPoint(Map<String, List<String>> associations) {
		IConfigurationElement[] conf = Platform.getExtensionRegistry().getConfigurationElementsFor(SCHEMA_EXT);
		for (IConfigurationElement el : conf) {
			String pattern = el.getAttribute(PATTERN_ATTR);
			if (!associations.containsKey(pattern)) {
				associations.put(pattern, new ArrayList<>());
			}
			associations.get(pattern).add(SchemaAssociationRegistry.translate(el.getAttribute(URL_ATTR)));
		}
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		return Collections.singletonMap("provideFormatter", true);
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
