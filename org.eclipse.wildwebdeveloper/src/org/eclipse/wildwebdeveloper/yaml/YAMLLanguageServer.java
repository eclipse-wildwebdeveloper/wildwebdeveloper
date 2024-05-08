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
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.SchemaAssociationsPreferenceInitializer;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.ui.preferences.ProcessStreamConnectionProviderWithPreference;
import org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants;

/**
 * YAML language server.
 *
 */
public class YAMLLanguageServer extends ProcessStreamConnectionProviderWithPreference {

	private static final String YAML_LANGUAGE_SERVER_ID = "org.eclipse.wildwebdeveloper.yaml";

	private static final String[] SUPPORTED_SECTIONS = { "yaml" };

	public YAMLLanguageServer() {
		super(YAML_LANGUAGE_SERVER_ID, Activator.getDefault().getPreferenceStore(), SUPPORTED_SECTIONS);
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
			ILog.get().error(e.getMessage(), e);
		}
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage responseMessage) {
			if (responseMessage.getResult() instanceof InitializeResult) {
				Object settings = createSettings();

				DidChangeConfigurationParams params = new DidChangeConfigurationParams(settings);
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}

	@Override
	protected boolean isAffected(PropertyChangeEvent event) {
		return super.isAffected(event)
				|| SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE.equals(event.getProperty());
	}

	@Override
	protected Object createSettings() {
		return YAMLPreferenceServerConstants.getGlobalSettings();
	}

	@Override
	public String toString() {
		return "YAML Language Server: " + super.toString();
	}

}
