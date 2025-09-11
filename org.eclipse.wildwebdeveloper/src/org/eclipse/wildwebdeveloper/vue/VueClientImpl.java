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

import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.isMatchCssSection;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.isMatchHtmlSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.client.DefaultLanguageClient;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

public class VueClientImpl extends DefaultLanguageClient implements VueLanguageServerExtention {

	@Override
	public void projectLoadingFinish(Object object) {
		// TODO should this set some state because only now stuff will work like hover..
		// or maybe even after projectLanguageService "enabled" call
		logMessage(new MessageParams(MessageType.Info, "Vue project loading finished"));
	}

	@Override
	public void projectLoadingStart(Object object) {
		logMessage(new MessageParams(MessageType.Info, "Vue project loading started"));
	}

	@Override
	public void projectLanguageService(Map<String, Object> data) {
		logMessage(new MessageParams(MessageType.Info,
				"Language Service is "
						+ (((Boolean) data.get("languageServiceEnabled")).booleanValue() ? "" : "not yet ")
						+ "enabled for project " + data.get("projectName")));
	}

	@Override
	public void tsserverRequest(Object[] params) {
		logMessage(new MessageParams(MessageType.Info, "Forward TS message " + params[0]));
	}
	
	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
		return CompletableFuture.supplyAsync(() -> {
			// The HTML language server asks for a given uri, the settings for 'css',
			// 'javascript', 'html'
			// See
			// https://github.com/microsoft/vscode/blob/7bd27b4287b49e61a1cb49e18f370260144c8685/extensions/html-language-features/server/src/htmlServer.ts#L123
			List<Object> settings = new ArrayList<>();
			for (ConfigurationItem item : params.getItems()) {
				String section = item.getSection();
				if (isMatchHtmlSection(section)) {
					// 'html' section, returns the html settings
					Settings htmlSettings = HTMLPreferenceServerConstants.getGlobalSettings();
					settings.add(htmlSettings.findSettings(section.split("[.]")));
				} else if (isMatchCssSection(section)) {
					// 'css' section, returns the css settings
					Settings cssSettings = CSSPreferenceServerConstants.getGlobalSettings();
					settings.add(cssSettings.findSettings(section.split("[.]")));
				} else if (section.equals("vue.suggest.defineAssignment")) {
					settings.add(true);
				} else if (section.equals("vue.suggest.propNameCasing")) {
					settings.add("preferKebabCase");
				} else if (section.equals("vue.suggest.componentNameCasing")) {
					settings.add("preferPascalCase");
 				} else {
					// TODO match javascript section once those preferences will be
					// implemented.
					settings.add(null);
				}
			}
			return settings;
		});
	}

}
