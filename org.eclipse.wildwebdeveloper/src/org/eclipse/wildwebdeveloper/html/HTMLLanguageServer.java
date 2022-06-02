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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class HTMLLanguageServer extends ProcessStreamConnectionProvider {

	public HTMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator.toFileURL(getClass()
					.getResource("/node_modules/vscode-html-languageserver/dist/node/htmlServerMain.js"));
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
		options.put("provideFormatter", true);
		return options;
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage responseMessage) {
			if (responseMessage.getResult() instanceof InitializeResult) {
				Map<String, Object> htmlOptions = new HashMap<>();

				Map<String, Object> validateOptions = new HashMap<>();
				validateOptions.put("scripts", true);
				validateOptions.put("styles", true);
				htmlOptions.put("validate", validateOptions);

				htmlOptions.put("format", Collections.singletonMap("enable", Boolean.TRUE));

				Map<String, Object> html = new HashMap<>();
				html.put("html", htmlOptions);

				DidChangeConfigurationParams params = new DidChangeConfigurationParams(html);
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}

	@Override
	public String toString() {
		return "HTML Language Server: " + super.toString();
	}

}
