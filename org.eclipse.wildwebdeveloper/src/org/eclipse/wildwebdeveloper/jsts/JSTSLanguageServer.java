/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *   Andrew Obuchowicz (Red Hat Inc.) - Add ESLint support
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.typescript.TypeScriptPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.ui.preferences.ProcessStreamConnectionProviderWithPreference;

public class JSTSLanguageServer extends ProcessStreamConnectionProviderWithPreference {

	private static final String JSTS_LANGUAGE_SERVER_ID = "org.eclipse.wildwebdeveloper.jsts";

	private static final String[] SUPPORTED_SECTIONS = { "typescript", "javascript" };

	private static String tsserverPath;
	
	public JSTSLanguageServer() {
		super(JSTS_LANGUAGE_SERVER_ID, Activator.getDefault().getPreferenceStore(), SUPPORTED_SECTIONS);
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator
					.toFileURL(getClass().getResource("/node_modules/typescript-language-server/lib/cli.mjs"));
			File nodeModules = new File(url.getPath()).getParentFile().getParentFile().getParentFile();
			tsserverPath = new File(nodeModules, "typescript/lib/tssserver.js").getAbsolutePath();
			commands.add(new File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			URL nodeDependencies = FileLocator.toFileURL(getClass().getResource("/"));
			setWorkingDirectory(nodeDependencies.getPath()); // Required for typescript-eslint-language-service to find
																// it's dependencies

		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		Map<String, Object> options = new HashMap<>();
		// plugins
		List<TypeScriptPlugin> plugins = new ArrayList<>();
		try {
//			plugins.add(new TypeScriptPlugin("@angular/language-service"));
			plugins.add(new TypeScriptPlugin("typescript-plugin-css-modules"));
			plugins.add(new TypeScriptPlugin("typescript-lit-html-plugin"));
			options.put("plugins", plugins.stream().map(TypeScriptPlugin::toMap).toArray());
			
			// Initialize tsserver path
			Map<String, String> tsServer = new HashMap<>();
			tsServer.put("path", tsserverPath);
			options.put("tsserver", tsServer);
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
		String maxTsServerMemory = System.getProperty("org.eclipse.wildwebdeveloper.maxTsServerMemory");
		if (maxTsServerMemory != null) {
			options.put("maxTsServerMemory", maxTsServerMemory);
		}
		return options;
	}

	@Override
	protected Object createSettings() {
		Map<String, Object> settings = new HashMap<>();
		// javascript
		settings.putAll(JavaScriptPreferenceServerConstants.getGlobalSettings());
		// typescript
		settings.putAll(TypeScriptPreferenceServerConstants.getGlobalSettings());
		return settings;
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage responseMessage) {
			if (responseMessage.getResult() instanceof InitializeResult) {
				// enable validation: so far, no better way found than changing conf after init.
				DidChangeConfigurationParams params = new DidChangeConfigurationParams(createSettings());
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}
}
