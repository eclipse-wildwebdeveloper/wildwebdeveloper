package org.eclipse.wildwebdeveloper.css;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.isMatchCssSection;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.isMatchLessSection;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.isMatchScssSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientConfigurationProvider;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

public class CSSLanguageClientConfigurationProvider implements LanguageClientConfigurationProvider {

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

	@Override
	public void collectFormatting(FormattingOptions formattingOptions, TextDocumentIdentifier identifier,
			String languageId) {
		Settings settings = getSettings(languageId);
		if (settings != null) {
			Map<String, Object> result = (Map<String, Object>) settings.findSettings(languageId, "format");
			if (result != null) {
				for (Entry<String, Object> entry : result.entrySet()) {
					if (entry.getValue() instanceof String) {
						formattingOptions.putString(entry.getKey(), (String) entry.getValue());
					} else if (entry.getValue() instanceof Boolean) {
						formattingOptions.putBoolean(entry.getKey(), (Boolean) entry.getValue());
					} else if (entry.getValue() instanceof Number) {
						formattingOptions.putNumber(entry.getKey(), (Number) entry.getValue());
					}
				}
			}
		}
	}

	private static Settings getSettings(String languageId) {
		if (isMatchCssSection(languageId)) {
			return CSSPreferenceServerConstants.getGlobalSettings();
		}
		if (isMatchLessSection(languageId)) {
			return LESSPreferenceServerConstants.getGlobalSettings();
		}
		if (isMatchScssSection(languageId)) {
			return SCSSPreferenceServerConstants.getGlobalSettings();
		}
		return null;
	}

}
