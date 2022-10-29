/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.yaml;

import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.isMatchYamlSection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;
import org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants;

/**
 * YAML language client implementation.
 * 
 */
public class YamlLanguageClientImpl extends LanguageClientImpl {

	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
		return CompletableFuture.supplyAsync(() -> {
			List<Object> settings = new ArrayList<>();
			for (ConfigurationItem item : params.getItems()) {
				String section = item.getSection();
				if (isMatchYamlSection(section)) {
					// See https://github.com/redhat-developer/yaml-language-server/blob/c4b56b155eae1b8aa53817b7caef7dd1032b93ff/src/languageserver/handlers/settingsHandlers.ts#L42
					// 'yaml' section, returns the yaml settings
					Settings yamlSettings = YAMLPreferenceServerConstants.getGlobalSettings();
					settings.add(yamlSettings.findSettings(section.split("[.]")));
				} else {
					// TODO manage another section like http, [yaml], editor, files
					// See https://github.com/redhat-developer/yaml-language-server/blob/c4b56b155eae1b8aa53817b7caef7dd1032b93ff/src/languageserver/handlers/settingsHandlers.ts#L43
					// Unkwown section
					settings.add(null);
				}
			}
			return settings;
		});
	}

}
