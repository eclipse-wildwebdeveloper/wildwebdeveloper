/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.html;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;

public class HTMLLanguageServer extends ProcessStreamConnectionProvider {

	public HTMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(getClass()
					.getResource("/language-servers/node_modules/vscode-html-languageserver/out/htmlServerMain.js"));
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
	public Object getInitializationOptions(URI rootUri) {
		Map<String, Object> map = new HashMap<>();
		map.put("css", true);
		map.put("javascript", true);

		Map<String, Object> options = new HashMap<>();
		options.put("embeddedLanguages", map);
		options.put("format.enable", true);
		return options;
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage) message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				Map<String, Object> options = new HashMap<>();
				options.put("scripts", true);
				options.put("styles", true);

				Map<String, Object> validate = new HashMap<>();
				validate.put("validate", options);

				Map<String, Object> html = new HashMap<>();
				html.put("html", validate);

				Map<String, Object> settings = new HashMap<>();
				settings.put("settings", html);

				DidChangeConfigurationParams params = new DidChangeConfigurationParams(settings);
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}

	@Override
	public String toString() {
		return "HTML Language Server: " + super.toString();
	}
}
