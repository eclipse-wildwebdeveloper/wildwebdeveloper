/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.bluesky.css;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.bluesky.Activator;
import org.eclipse.bluesky.InitializeLaunchConfigurations;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;

public class CSSLanguageServer extends ProcessStreamConnectionProvider {

	public CSSLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/language-servers/node_modules/vscode-css-languageserver/out/cssServerMain.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	@Override
	public String toString() {
		return "CSS Language Server: " + super.toString();
	}
	
	@Override
	public Object getInitializationOptions(URI rootUri) {
		Map<String, Object> settings = new HashMap<>();
		settings.put("css", Collections.singletonMap("validate", true));
		settings.put("scss", Collections.singletonMap("validate", true));
		settings.put("less", Collections.singletonMap("validate", true));
		return settings;
	}
	
	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage)message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				// enable validation: so far, no better way found than changing conf after init.
				DidChangeConfigurationParams params = new DidChangeConfigurationParams(getInitializationOptions(rootUri));
				languageServer.getWorkspaceService().didChangeConfiguration(params);
			}
		}
	}
}
