/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.css;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.isMatchCssSection;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.isMatchLessSection;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.isMatchScssSection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * CSS language client implementation.
 * 
 */
public class CSSLanguageClient extends LanguageClientImpl {

	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
		return CompletableFuture.supplyAsync(() -> {
			// The CSS language server asks for a given uri, the settings for 'css',
			// 'less', 'scss'
			// See
			// https://github.com/microsoft/vscode/blob/7bd27b4287b49e61a1cb49e18f370260144c8685/extensions/css-language-features/server/src/cssServer.ts#L156
			List<Object> settings = new ArrayList<>();
			for (ConfigurationItem item : params.getItems()) {
				String section = item.getSection();
				if (isMatchCssSection(section)) {
					// 'css' section, returns the css settings
					Settings cssSettings = CSSPreferenceServerConstants.getGlobalSettings();
					settings.add(cssSettings.findSettings(section.split("[.]")));
				} else if (isMatchLessSection(section)) {
					// 'less' section, returns the less settings
					Settings cssSettings = LESSPreferenceServerConstants.getGlobalSettings();
					settings.add(cssSettings.findSettings(section.split("[.]")));
				} else if (isMatchScssSection(section)) {
					// 'scss' section, returns the scss settings
					Settings cssSettings = SCSSPreferenceServerConstants.getGlobalSettings();
					settings.add(cssSettings.findSettings(section.split("[.]")));
				} else {
					// Unkwown section
					settings.add(null);
				}
			}
			return settings;
		});
	}
}
