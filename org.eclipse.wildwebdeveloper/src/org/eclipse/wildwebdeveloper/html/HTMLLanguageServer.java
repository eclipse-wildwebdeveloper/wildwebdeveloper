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
import org.eclipse.core.runtime.ILog;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.ui.preferences.ProcessStreamConnectionProviderWithPreference;

/**
 * HTML language server.
 *
 */
public class HTMLLanguageServer extends ProcessStreamConnectionProviderWithPreference {

	private static final String HTML_LANGUAGE_SERVER_ID = "org.eclipse.wildwebdeveloper.html";

	private static final String[] SUPPORTED_SECTIONS = { "html", "css", "javascript" };

	public HTMLLanguageServer() {
		super(HTML_LANGUAGE_SERVER_ID, Activator.getDefault().getPreferenceStore(), SUPPORTED_SECTIONS);
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator.toFileURL(
					getClass().getResource("/node_modules/vscode-html-languageserver/dist/node/htmlServerMain.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
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
	protected Object createSettings() {
		// In HTML language server case, we don't need to get the settings when client
		// call didChangeConfiguration.
		// because HTML language server call configuration with a given uri to get
		// settings for a given uri.
		// The didChangeConfiguration call will just 'reset all document settings'
		// See
		// https://github.com/microsoft/vscode/blob/7bd27b4287b49e61a1cb49e18f370260144c8685/extensions/html-language-features/server/src/htmlServer.ts#L246
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return "HTML Language Server: " + super.toString();
	}

}
