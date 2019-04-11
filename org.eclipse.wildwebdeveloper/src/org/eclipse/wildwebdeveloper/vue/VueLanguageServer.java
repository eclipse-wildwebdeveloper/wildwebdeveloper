/*******************************************************************************
 * Copyright (c) 2019 Gautier de Saint Martin Lacaze and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VueLanguageServer extends ProcessStreamConnectionProvider {

	public VueLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(
					getClass().getResource("/language-servers/node_modules/vue-language-server/dist/vueServerMain.js"));
			commands.add(new File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		JsonElement defaultConfiguration = getDefaultConfiguration();
		JsonObject config = new JsonObject();
		config.add("config", defaultConfiguration);
		return config;
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage) message;
			if (responseMessage.getResult() instanceof InitializeResult) {			
				DidChangeConfigurationParams params = new DidChangeConfigurationParams(getDefaultConfiguration());
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}

	private JsonElement getDefaultConfiguration() {
		String config = "{}";
		
		try {
			URL resource = FileLocator.toFileURL(getClass().getResource("/language-configurations/vue/vue-default-config.json"));
			config = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining());
		} catch (IOException | URISyntaxException e) {
			// nothing to do
		}
		Gson gson = new Gson();
		return gson.fromJson(config, JsonElement.class);
	}
}
