/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.eslint;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class ESLintLanguageServer extends ProcessStreamConnectionProvider {

	public ESLintLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		//commands.add("--inspect-brk"); // for local debug
		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/node_modules/eslint-server/out/eslintServer.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			// commands.add("/home/mistria/git/vscode-eslint/server/out/eslintServer.js"); // to use and debug against local sources
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	

}
