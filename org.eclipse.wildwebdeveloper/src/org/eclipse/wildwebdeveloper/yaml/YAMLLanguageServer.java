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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class YAMLLanguageServer extends ProcessStreamConnectionProvider {
	
	private String cachedSchema;
	
	public YAMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator.toFileURL(getClass()
					.getResource("/node_modules/yaml-language-server/out/server/src/server.js"));
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
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String schemaStr = preferenceStore.getString(YAMLPreferenceInitializer.YAML_SCHEMA_PREFERENCE);
		if (cachedSchema == null || !schemaStr.equals(cachedSchema)) {
			cachedSchema = schemaStr;
			Map<String, Object> schemas = new Gson().fromJson(schemaStr, new TypeToken<HashMap<String, Object>>() {}.getType());
			Map<String, Object> yaml = new HashMap<>();
			yaml.put("schemas", schemas);
			yaml.put("validate", true);
			yaml.put("completion", true);
			yaml.put("hover", true);
			
			Map<String, Object> settings = new HashMap<>();
			settings.put("yaml", yaml);
			
			DidChangeConfigurationParams params = new DidChangeConfigurationParams(settings);
			languageServer.getWorkspaceService().didChangeConfiguration(params);
		}
	}
	
	@Override
	public String toString() {
		return "YAML Language Server: " + super.toString();
	}
}
