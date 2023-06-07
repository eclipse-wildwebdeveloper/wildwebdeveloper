/*******************************************************************************
 * Copyright (c) 2023 Dawid Pakuła and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Dawid Pakuła <zulus@w3des.net> - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class VueLanguageServer extends ProcessStreamConnectionProvider {


	public VueLanguageServer() {

		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator
					.toFileURL(getClass().getResource("/node_modules/@vue/language-server/bin/vue-language-server.js"));
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
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = super.createProcessBuilder();
		builder.environment().put("VUE_NONPOLLING_WATCHER", Boolean.toString(true));
		return builder;
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {

		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/node_modules/typescript/lib"));
			System.out.println(new File(url.getPath()).getAbsolutePath());
			return Collections.singletonMap("typescript", Collections.singletonMap("tsdk", new File(url.getPath()).getAbsolutePath()));
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public String toString() {
		return "VUE Language Server: " + super.toString();
	}
}
