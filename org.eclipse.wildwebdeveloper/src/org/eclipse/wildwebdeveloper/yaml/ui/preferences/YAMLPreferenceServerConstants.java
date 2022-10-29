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
package org.eclipse.wildwebdeveloper.yaml.ui.preferences;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.SchemaAssociationRegistry;
import org.eclipse.wildwebdeveloper.SchemaAssociationsPreferenceInitializer;
import org.eclipse.wildwebdeveloper.json.JSonLanguageServer;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * YAML preference server constants.
 *
 */
public class YAMLPreferenceServerConstants {

	private static final String YAML_SECTION = "yaml";

	public static final String YAML_PREFERENCES_SCHEMAS = "yaml.schemas";
	public static final String YAML_PREFERENCES_SCHEMASTORE_ENABLE = "yaml.schemaStore.enable";
	public static final String YAML_PREFERENCES_SCHEMASTORE_URL = "yaml.schemaStore.url";
	public static final String YAML_PREFERENCES_MAXITEMSCOMPUTED = "yaml.maxItemsComputed";

	// Completion settings
	public static final String YAML_PREFERENCES_COMPLETION = "yaml.completion";
	public static final String YAML_PREFERENCES_SUGGEST_PARENTSKELETONSELECTEDFIRST = "yaml.suggest.parentSkeletonSelectedFirst";
	public static final String YAML_PREFERENCES_DISABLEDEFAULTPROPERTIES = "yaml.disableDefaultProperties";

	// Format settings
	public static final String YAML_PREFERENCES_FORMAT_ENABLE = "yaml.format.enable";
	public static final String YAML_PREFERENCES_FORMAT_SINGLEQUOTE = "yaml.format.singleQuote";
	public static final String YAML_PREFERENCES_FORMAT_BRACKETSPACING = "yaml.format.bracketSpacing";
	public static final String YAML_PREFERENCES_FORMAT_PROSEWRAP = "yaml.format.proseWrap";
	public static final String YAML_PREFERENCES_FORMAT_PRINTWIDTH = "yaml.format.printWidth";

	// Hover settings
	public static final String YAML_PREFERENCES_HOVER = "yaml.hover";

	// Validation settings
	public static final String YAML_PREFERENCES_VALIDATE = "yaml.validate";
	public static final String YAML_PREFERENCES_YAMLVERSION = "yaml.yamlVersion";
	public static final String YAML_PREFERENCES_DISABLEADDITIONALPROPERTIES = "yaml.disableAdditionalProperties";
	public static final String YAML_PREFERENCES_CUSTOMTAGS = "yaml.customTags";
	public static final String YAML_PREFERENCES_STYLE_FLOWMAPPING = "yaml.style.flowMapping";
	public static final String YAML_PREFERENCES_STYLE_FLOWSEQUENCE = "yaml.style.flowSequence";

	public static Settings getGlobalSettings() {
		Settings settings = new Settings(getPreferenceStore());

		settings.fillSetting(YAML_PREFERENCES_SCHEMAS,
				getSchemaAssociations(Activator.getDefault().getPreferenceStore()));
		settings.fillAsBoolean(YAML_PREFERENCES_SCHEMASTORE_ENABLE);
		settings.fillAsString(YAML_PREFERENCES_SCHEMASTORE_URL);
		settings.fillAsInt(YAML_PREFERENCES_MAXITEMSCOMPUTED);

		// Completion settings
		settings.fillAsBoolean(YAML_PREFERENCES_COMPLETION);
		settings.fillAsBoolean(YAML_PREFERENCES_SUGGEST_PARENTSKELETONSELECTEDFIRST);
		settings.fillAsBoolean(YAML_PREFERENCES_DISABLEDEFAULTPROPERTIES);

		// Format settings
		settings.fillAsBoolean(YAML_PREFERENCES_FORMAT_ENABLE);
		settings.fillAsBoolean(YAML_PREFERENCES_FORMAT_SINGLEQUOTE);
		settings.fillAsBoolean(YAML_PREFERENCES_FORMAT_BRACKETSPACING);
		settings.fillAsString(YAML_PREFERENCES_FORMAT_PROSEWRAP);
		settings.fillAsInt(YAML_PREFERENCES_FORMAT_PRINTWIDTH);

		// Hover settings
		settings.fillAsBoolean(YAML_PREFERENCES_HOVER);

		// Validation settings
		settings.fillAsBoolean(YAML_PREFERENCES_VALIDATE);
		settings.fillAsString(YAML_PREFERENCES_YAMLVERSION);
		settings.fillAsStringArray(YAML_PREFERENCES_CUSTOMTAGS, ",");
		settings.fillAsBoolean(YAML_PREFERENCES_DISABLEADDITIONALPROPERTIES);
		settings.fillAsString(YAML_PREFERENCES_STYLE_FLOWMAPPING);
		settings.fillAsString(YAML_PREFERENCES_STYLE_FLOWSEQUENCE);

		return settings;
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Server settings
		store.setDefault(YAML_PREFERENCES_SCHEMASTORE_ENABLE, true);
		store.setDefault(YAML_PREFERENCES_SCHEMASTORE_URL, "https://www.schemastore.org/api/json/catalog.json");
		store.setDefault(YAML_PREFERENCES_MAXITEMSCOMPUTED, 5000);

		// - Completion
		store.setDefault(YAML_PREFERENCES_COMPLETION, true);
		store.setDefault(YAML_PREFERENCES_SUGGEST_PARENTSKELETONSELECTEDFIRST, false);
		store.setDefault(YAML_PREFERENCES_DISABLEDEFAULTPROPERTIES, false);

		// - Format
		store.setDefault(YAML_PREFERENCES_FORMAT_ENABLE, true);
		store.setDefault(YAML_PREFERENCES_FORMAT_SINGLEQUOTE, false);
		store.setDefault(YAML_PREFERENCES_FORMAT_BRACKETSPACING, true);
		store.setDefault(YAML_PREFERENCES_FORMAT_PROSEWRAP, "preserve");
		store.setDefault(YAML_PREFERENCES_FORMAT_PRINTWIDTH, 80);

		// - Hover
		store.setDefault(YAML_PREFERENCES_HOVER, true);

		// - Validation
		store.setDefault(YAML_PREFERENCES_VALIDATE, true);
		store.setDefault(YAML_PREFERENCES_YAMLVERSION, "1.2");
		store.setDefault(YAML_PREFERENCES_CUSTOMTAGS, "");
		store.setDefault(YAML_PREFERENCES_DISABLEADDITIONALPROPERTIES, false);
		store.setDefault(YAML_PREFERENCES_STYLE_FLOWMAPPING, "allow");
		store.setDefault(YAML_PREFERENCES_STYLE_FLOWSEQUENCE, "allow");
	}

	/**
	 * Returns true if the given section matches YAML settings and false otherwise.
	 * 
	 * @param section the section to check.
	 * 
	 * @return true if the given section matches YAML settings and false otherwise.
	 */
	public static boolean isMatchYamlSection(String section) {
		return isMatchSection(section, YAML_SECTION);
	}

	private static Map<String, Object> getSchemaAssociations(IPreferenceStore preferenceStore) {
		String schemaString = preferenceStore
				.getString(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE);

		Map<String, Object> contentTypeAssociations = new Gson().fromJson(schemaString,
				new TypeToken<HashMap<String, String>>() {
				}.getType());

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType yamlBaseContentType = contentTypeManager.getContentType("org.eclipse.wildwebdeveloper.yaml");

		Map<String, Object> associations = new HashMap<>();

		contentTypeAssociations.forEach((key, value) -> {
			IContentType contentType = contentTypeManager.getContentType(key);
			if (contentType != null && contentType.getBaseType().equals(yamlBaseContentType)) {
				String[] fileNames = contentType.getFileSpecs(IContentType.FILE_NAME_SPEC);
				for (String fileName : fileNames) {
					associations.put(value.toString(), fileName);
				}

				String[] filePatterns = contentType.getFileSpecs(IContentType.FILE_PATTERN_SPEC);
				for (String pattern : filePatterns) {
					associations.put(value.toString(), pattern);
				}

				String[] fileExtensions = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				for (String extension : fileExtensions) {
					associations.put(value.toString(), "*." + extension);
				}
			}
		});

		IConfigurationElement[] conf = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(JSonLanguageServer.SCHEMA_EXT);
		for (IConfigurationElement el : conf) {
			String url = el.getAttribute(JSonLanguageServer.URL_ATTR);
			String pattern = el.getAttribute(JSonLanguageServer.PATTERN_ATTR);
			if (!url.isBlank() && !pattern.isBlank()) {
				associations.put(SchemaAssociationRegistry.translate(url), pattern);
			}
		}

		return associations;
	}

	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
