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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class VueLanguageServer extends ProcessStreamConnectionProvider {
	private static String tsserverPath = null;
	private static String vuePath = null;

	public VueLanguageServer() {

		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			if (vuePath == null || tsserverPath == null) {
				resolvePaths();
			}
			commands.add(vuePath);
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	private void resolvePaths() throws IOException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/node_modules/@vue/language-server/bin/vue-language-server.js"));
		vuePath = new File(url.getPath()).getAbsolutePath();
		
		url = FileLocator.toFileURL(getClass().getResource("/node_modules/typescript/lib"));
		tsserverPath = new File(url.getPath()).getAbsolutePath();
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = super.createProcessBuilder();
		builder.environment().put("VUE_NONPOLLING_WATCHER", Boolean.toString(true));
		return builder;
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		Map<String, Object> options = new HashMap<>();
		setWorkingDirectory(rootUri.getRawPath());
		
		options.put("typescript", Collections.singletonMap("tsdk", tsserverPath));
		options.put("diagnosticModel", 0);
		options.put("additionalExtensions", new String[] {});
		
		Map<String, Object> legend = new HashMap<>();
		legend.put("tokenTypes", new String[] {"component"} );
		legend.put("tokenModifiers", new String[] {} );
		options.put("semanticTokensLegend", legend);
		
		Map<String, Object> vue = new HashMap<>();
		vue.put("hybridMode", false);
		
		options.put("vue", vue);
		
		return options;
	}

	@Override
	public String toString() {
		return "VUE Language Server: " + super.toString();
	}

	
}
