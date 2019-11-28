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
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;

public class JSTSLanguageServer extends ProcessStreamConnectionProvider {

	public JSTSLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/language-servers/node_modules/typescript-language-server/lib/cli.js"));
			URL tsServer = FileLocator.toFileURL(getClass().getResource("/language-servers/node_modules/typescript/lib/tsserver.js"));
			commands.add(new File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			commands.add("--tsserver-path");
			commands.add(new File(tsServer.getPath()).getAbsolutePath());
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}
	
	@Override
	public Object getInitializationOptions(URI rootUri) {
		Map<String, Object> options = new HashMap<>();
		List<TypeScriptPlugin> plugins = new ArrayList<>();
		try {
			plugins.add(new TypeScriptPlugin("@angular/language-service"));
			plugins.add(new TypeScriptPlugin("typescript-plugin-css-modules"));
			plugins.add(new TypeScriptPlugin("typescript-lit-html-plugin"));
			options.put("plugins", plugins.stream().map(TypeScriptPlugin::toMap).toArray());
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
		return options;
	}
	
}
