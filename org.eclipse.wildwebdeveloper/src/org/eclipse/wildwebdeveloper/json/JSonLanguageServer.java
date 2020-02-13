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
 *  Michal� Niewrzal� (Rogue Wave Software Inc.) - initial implementation
 *  Angelo Zerr <angelo.zerr@gmail.com> - JSON Schema support
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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;

public class JSonLanguageServer extends ProcessStreamConnectionProvider {

	public JSonLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(getClass()
					.getResource("/language-servers/node_modules/vscode-json-languageserver/dist/jsonServerMain.js"));
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
				// Send json/schemaAssociations notification to register JSON Schema on JSON
				// Language server side.
				JSonLanguageServerInterface server = (JSonLanguageServerInterface) languageServer;
				Map<String, List<String>> schemaAssociations = getSchemaAssociations();
				server.sendJSonchemaAssociations(schemaAssociations);
			}
		}
	}

	private Map<String, List<String>> getSchemaAssociations() {
		// TODO: provide eclipse extension point to defines JSON Schema associations.
		Map<String, List<String>> associations = new HashMap<>();
		fillSchemaAssociationsForJavascript(associations);
		fillSchemaAssociationsForTypeScript(associations);
		fillSchemaAssociationsForOmnisharp(associations);
		return associations;
	}

	/**
	 * JSON Schema contributions for JavaScript
	 * 
	 * @param associations
	 */
	private void fillSchemaAssociationsForJavascript(Map<String, List<String>> associations) {
		associations.put("package.json", Arrays.asList("http://json.schemastore.org/package"));
		associations.put("/bower.json", Arrays.asList("http://json.schemastore.org/bower"));
		associations.put("/.bower.json", Arrays.asList("http://json.schemastore.org/bower"));
		associations.put("/.bowerrc", Arrays.asList("http://json.schemastore.org/bowerrc"));
		associations.put("/jsconfig.json", Arrays.asList("http://json.schemastore.org/jsconfig"));
		associations.put("/.eslintrc", Arrays.asList("http://json.schemastore.org/eslintrc"));
	}

	/**
	 * JSON Schema contributions for TypeScript
	 * 
	 * @param associations
	 */
	private void fillSchemaAssociationsForTypeScript(Map<String, List<String>> associations) {
		associations.put("/tsconfig.json", Arrays.asList("http://json.schemastore.org/tsconfig"));
		associations.put("/tsconfig.*.json", Arrays.asList("http://json.schemastore.org/tsconfig"));
		associations.put("/typing.json", Arrays.asList("http://json.schemastore.org/typing"));
	}

	/**
	 * JSON Schema contributions for TypeScript
	 * 
	 * @param associations
	 */
	private void fillSchemaAssociationsForOmnisharp(Map<String, List<String>> associations) {
		associations.put("/omnisharp.json", Arrays.asList("http://json.schemastore.org/omnisharp"));
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		return Collections.singletonMap("provideFormatter", true);
	}
}
