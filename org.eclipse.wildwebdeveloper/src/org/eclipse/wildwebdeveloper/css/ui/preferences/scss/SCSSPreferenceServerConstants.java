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
package org.eclipse.wildwebdeveloper.css.ui.preferences.scss;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * SCSS preference server constants.
 *
 */
public class SCSSPreferenceServerConstants {

	private static final String SCSS_SECTION = "scss";

	// public static final String SCSS_PREFERENCES_CUSTOMDATA = "scss.customData";

	// Completion settings
	public static final String SCSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION = "scss.completion.triggerPropertyValueCompletion";
	public static final String SCSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON = "scss.completion.completePropertyWithSemicolon";

	// Format settings
	// public static final String SCSS_PREFERENCES_FORMAT_ENABLE =
	// "scss.format.enable";
	public static final String SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS = "scss.format.newlineBetweenSelectors";
	public static final String SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES = "scss.format.newlineBetweenRules";
	public static final String SCSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR = "scss.format.spaceAroundSelectorSeparator";
	public static final String SCSS_PREFERENCES_FORMAT_BRACE_STYLE = "scss.format.braceStyle";
	public static final String SCSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES = "scss.format.preserveNewLines";
	public static final String SCSS_PREFERENCES_FORMAT_MAXP_RESERVE_NEW_LINES = "scss.format.maxPreserveNewLines";

	// Hover settings
	public static final String SCSS_PREFERENCES_HOVER_DOCUMENTATION = "scss.hover.documentation";
	public static final String SCSS_PREFERENCES_HOVER_REFERENCES = "scss.hover.references";

	// Validation settings
	public static final String SCSS_PREFERENCES_VALIDATE = "scss.validate";
	public static final String SCSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES = "scss.lint.compatibleVendorPrefixes";
	public static final String SCSS_PREFERENCES_LINT_VENDOR_PREFIX = "scss.lint.vendorPrefix";
	public static final String SCSS_PREFERENCES_LINT_DUPLICATEPROPERTIES = "scss.lint.duplicateProperties";
	public static final String SCSS_PREFERENCES_LINT_EMPTYRULES = "scss.lint.emptyRules";
	public static final String SCSS_PREFERENCES_LINT_IMPORTSTATEMENT = "scss.lint.importStatement";
	public static final String SCSS_PREFERENCES_LINT_BOXMODEL = "scss.lint.boxModel";
	public static final String SCSS_PREFERENCES_LINT_UNIVERSALSELECTOR = "scss.lint.universalSelector";
	public static final String SCSS_PREFERENCES_LINT_ZEROUNITS = "scss.lint.zeroUnits";
	public static final String SCSS_PREFERENCES_LINT_FONTFACEPROPERTIES = "scss.lint.fontFaceProperties";
	public static final String SCSS_PREFERENCES_LINT_HEXCOLORLENGTH = "scss.lint.hexColorLength";
	public static final String SCSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION = "scss.lint.argumentsInColorFunction";
	public static final String SCSS_PREFERENCES_LINT_UNKNOWNPROPERTIES = "scss.lint.unknownProperties";
	public static final String SCSS_PREFERENCES_LINT_VALIDPROPERTIES = "scss.lint.validProperties";
	public static final String SCSS_PREFERENCES_LINT_IEHACK = "scss.lint.ieHack";
	public static final String SCSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES = "scss.lint.unknownVendorSpecificProperties";
	public static final String SCSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY = "scss.lint.propertyIgnoredDueToDisplay";
	public static final String SCSS_PREFERENCES_LINT_IMPORTANT = "scss.lint.important";
	public static final String SCSS_PREFERENCES_LINT_FLOAT = "scss.lint.float";
	public static final String SCSS_PREFERENCES_LINT_IDSELECTOR = "scss.lint.idSelector";
	public static final String SCSS_PREFERENCES_LINT_UNKNOWNATRULES = "scss.lint.unknownAtRules";

	public static Settings getGlobalSettings() {
		Settings settings = new Settings(Activator.getDefault().getPreferenceStore());

		// Completion settings
		settings.fillAsBoolean(SCSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION);
		settings.fillAsBoolean(SCSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON);

		// Format settings
		settings.fillAsBoolean(SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS);
		settings.fillAsBoolean(SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES);
		settings.fillAsBoolean(SCSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR);
		settings.fillAsString(SCSS_PREFERENCES_FORMAT_BRACE_STYLE);
		settings.fillAsBoolean(SCSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES);
		// Cannot implement this preference since it can be number/null
		// settings.fillAsInt(SCSS_PREFERENCES_FORMAT_MAXPRESERVENEWLINES);

		// Hover settings
		settings.fillAsBoolean(SCSS_PREFERENCES_HOVER_DOCUMENTATION);
		settings.fillAsBoolean(SCSS_PREFERENCES_HOVER_REFERENCES);

		// Validation settings
		settings.fillAsBoolean(SCSS_PREFERENCES_VALIDATE);
		settings.fillAsString(SCSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES);

		settings.fillAsString(SCSS_PREFERENCES_LINT_VENDOR_PREFIX);
		settings.fillAsString(SCSS_PREFERENCES_LINT_DUPLICATEPROPERTIES);
		settings.fillAsString(SCSS_PREFERENCES_LINT_EMPTYRULES);
		settings.fillAsString(SCSS_PREFERENCES_LINT_IMPORTSTATEMENT);
		settings.fillAsString(SCSS_PREFERENCES_LINT_BOXMODEL);
		settings.fillAsString(SCSS_PREFERENCES_LINT_UNIVERSALSELECTOR);
		settings.fillAsString(SCSS_PREFERENCES_LINT_ZEROUNITS);
		settings.fillAsString(SCSS_PREFERENCES_LINT_FONTFACEPROPERTIES);
		settings.fillAsString(SCSS_PREFERENCES_LINT_HEXCOLORLENGTH);
		settings.fillAsString(SCSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION);
		settings.fillAsString(SCSS_PREFERENCES_LINT_UNKNOWNPROPERTIES);
		settings.fillAsStringArray(SCSS_PREFERENCES_LINT_VALIDPROPERTIES, ",");
		settings.fillAsString(SCSS_PREFERENCES_LINT_IEHACK);
		settings.fillAsString(SCSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES);
		settings.fillAsString(SCSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY);
		settings.fillAsString(SCSS_PREFERENCES_LINT_IMPORTANT);
		settings.fillAsString(SCSS_PREFERENCES_LINT_FLOAT);
		settings.fillAsString(SCSS_PREFERENCES_LINT_IDSELECTOR);
		settings.fillAsString(SCSS_PREFERENCES_LINT_UNKNOWNATRULES);

		return settings;
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Server settings

		// STORE.setDefault(SCSS_PREFERENCES_CUSTOMDATA, Arrays.asList(""));

		// - Completion
		store.setDefault(SCSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION, true);
		store.setDefault(SCSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON, true);

		// - Format
		store.setDefault(SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS, true);
		store.setDefault(SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES, true);
		store.setDefault(SCSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR, false);
		store.setDefault(SCSS_PREFERENCES_FORMAT_BRACE_STYLE, "collapse");
		store.setDefault(SCSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES, true);
		// store.setDefault(SCSS_PREFERENCES_FORMAT_MAXPRESERVENEWLINES, null);

		// - Hover
		store.setDefault(SCSS_PREFERENCES_HOVER_DOCUMENTATION, true);
		store.setDefault(SCSS_PREFERENCES_HOVER_REFERENCES, true);

		// - Validation
		store.setDefault(SCSS_PREFERENCES_VALIDATE, true);
		store.setDefault(SCSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_VENDOR_PREFIX, "warning");
		store.setDefault(SCSS_PREFERENCES_LINT_DUPLICATEPROPERTIES, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_EMPTYRULES, "warning");
		store.setDefault(SCSS_PREFERENCES_LINT_IMPORTSTATEMENT, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_BOXMODEL, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_UNIVERSALSELECTOR, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_ZEROUNITS, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_FONTFACEPROPERTIES, "warning");
		store.setDefault(SCSS_PREFERENCES_LINT_HEXCOLORLENGTH, "error");
		store.setDefault(SCSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION, "error");
		store.setDefault(SCSS_PREFERENCES_LINT_UNKNOWNPROPERTIES, "warning");
		store.setDefault(SCSS_PREFERENCES_LINT_VALIDPROPERTIES, "");
		store.setDefault(SCSS_PREFERENCES_LINT_IEHACK, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY, "warning");
		store.setDefault(SCSS_PREFERENCES_LINT_IMPORTANT, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_FLOAT, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_IDSELECTOR, "ignore");
		store.setDefault(SCSS_PREFERENCES_LINT_UNKNOWNATRULES, "warning");

	}

	/**
	 * Returns true if the given section matches SCSS settings and false otherwise.
	 * 
	 * @param section the section to check.
	 * 
	 * @return true if the given section matches SCSS settings and false otherwise.
	 */
	public static boolean isMatchScssSection(String section) {
		return isMatchSection(section, SCSS_SECTION);
	}
}
