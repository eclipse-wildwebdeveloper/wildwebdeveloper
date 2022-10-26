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
package org.eclipse.wildwebdeveloper.css.ui.preferences;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * CSS preference server constants.
 *
 */
public class CSSPreferenceServerConstants {

	private static final String CSS_SECTION = "css";

	// public static final String CSS_PREFERENCES_CUSTOMDATA = "css.customData";

	// Completion settings
	public static final String CSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION = "css.completion.triggerPropertyValueCompletion";
	public static final String CSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON = "css.completion.completePropertyWithSemicolon";

	// Format settings
	// public static final String CSS_PREFERENCES_FORMAT_ENABLE =
	// "css.format.enable";
	public static final String CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS = "css.format.newlineBetweenSelectors";
	public static final String CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES = "css.format.newlineBetweenRules";
	public static final String CSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR = "css.format.spaceAroundSelectorSeparator";
	public static final String CSS_PREFERENCES_FORMAT_BRACE_STYLE = "css.format.braceStyle";
	public static final String CSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES = "css.format.preserveNewLines";
	public static final String CSS_PREFERENCES_FORMAT_MAXP_RESERVE_NEW_LINES = "css.format.maxPreserveNewLines";

	// Hover settings
	public static final String CSS_PREFERENCES_HOVER_DOCUMENTATION = "css.hover.documentation";
	public static final String CSS_PREFERENCES_HOVER_REFERENCES = "css.hover.references";

	// Validation settings
	public static final String CSS_PREFERENCES_VALIDATE = "css.validate";
	public static final String CSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES = "css.lint.compatibleVendorPrefixes";
	public static final String CSS_PREFERENCES_LINT_VENDOR_PREFIX = "css.lint.vendorPrefix";
	public static final String CSS_PREFERENCES_LINT_DUPLICATEPROPERTIES = "css.lint.duplicateProperties";
	public static final String CSS_PREFERENCES_LINT_EMPTYRULES = "css.lint.emptyRules";
	public static final String CSS_PREFERENCES_LINT_IMPORTSTATEMENT = "css.lint.importStatement";
	public static final String CSS_PREFERENCES_LINT_BOXMODEL = "css.lint.boxModel";
	public static final String CSS_PREFERENCES_LINT_UNIVERSALSELECTOR = "css.lint.universalSelector";
	public static final String CSS_PREFERENCES_LINT_ZEROUNITS = "css.lint.zeroUnits";
	public static final String CSS_PREFERENCES_LINT_FONTFACEPROPERTIES = "css.lint.fontFaceProperties";
	public static final String CSS_PREFERENCES_LINT_HEXCOLORLENGTH = "css.lint.hexColorLength";
	public static final String CSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION = "css.lint.argumentsInColorFunction";
	public static final String CSS_PREFERENCES_LINT_UNKNOWNPROPERTIES = "css.lint.unknownProperties";
	public static final String CSS_PREFERENCES_LINT_VALIDPROPERTIES = "css.lint.validProperties";
	public static final String CSS_PREFERENCES_LINT_IEHACK = "css.lint.ieHack";
	public static final String CSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES = "css.lint.unknownVendorSpecificProperties";
	public static final String CSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY = "css.lint.propertyIgnoredDueToDisplay";
	public static final String CSS_PREFERENCES_LINT_IMPORTANT = "css.lint.important";
	public static final String CSS_PREFERENCES_LINT_FLOAT = "css.lint.float";
	public static final String CSS_PREFERENCES_LINT_IDSELECTOR = "css.lint.idSelector";
	public static final String CSS_PREFERENCES_LINT_UNKNOWNATRULES = "css.lint.unknownAtRules";

	public static Settings getGlobalSettings() {
		Settings settings = new Settings(Activator.getDefault().getPreferenceStore());

		// Completion settings
		settings.fillAsBoolean(CSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION);
		settings.fillAsBoolean(CSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON);

		// Format settings
		settings.fillAsBoolean(CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS);
		settings.fillAsBoolean(CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES);
		settings.fillAsBoolean(CSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR);
		settings.fillAsString(CSS_PREFERENCES_FORMAT_BRACE_STYLE);
		settings.fillAsBoolean(CSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES);
		// Cannot implement this preference since it can be number/null
		// settings.fillAsInt(CSS_PREFERENCES_FORMAT_MAXPRESERVENEWLINES);

		// Hover settings
		settings.fillAsBoolean(CSS_PREFERENCES_HOVER_DOCUMENTATION);
		settings.fillAsBoolean(CSS_PREFERENCES_HOVER_REFERENCES);

		// Validation settings
		settings.fillAsBoolean(CSS_PREFERENCES_VALIDATE);
		settings.fillAsString(CSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES);

		settings.fillAsString(CSS_PREFERENCES_LINT_VENDOR_PREFIX);
		settings.fillAsString(CSS_PREFERENCES_LINT_DUPLICATEPROPERTIES);
		settings.fillAsString(CSS_PREFERENCES_LINT_EMPTYRULES);
		settings.fillAsString(CSS_PREFERENCES_LINT_IMPORTSTATEMENT);
		settings.fillAsString(CSS_PREFERENCES_LINT_BOXMODEL);
		settings.fillAsString(CSS_PREFERENCES_LINT_UNIVERSALSELECTOR);
		settings.fillAsString(CSS_PREFERENCES_LINT_ZEROUNITS);
		settings.fillAsString(CSS_PREFERENCES_LINT_FONTFACEPROPERTIES);
		settings.fillAsString(CSS_PREFERENCES_LINT_HEXCOLORLENGTH);
		settings.fillAsString(CSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION);
		settings.fillAsString(CSS_PREFERENCES_LINT_UNKNOWNPROPERTIES);
		settings.fillAsStringArray(CSS_PREFERENCES_LINT_VALIDPROPERTIES, ",");
		settings.fillAsString(CSS_PREFERENCES_LINT_IEHACK);
		settings.fillAsString(CSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES);
		settings.fillAsString(CSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY);
		settings.fillAsString(CSS_PREFERENCES_LINT_IMPORTANT);
		settings.fillAsString(CSS_PREFERENCES_LINT_FLOAT);
		settings.fillAsString(CSS_PREFERENCES_LINT_IDSELECTOR);
		settings.fillAsString(CSS_PREFERENCES_LINT_UNKNOWNATRULES);

		return settings;
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Server settings

		// STORE.setDefault(CSS_PREFERENCES_CUSTOMDATA, Arrays.asList(""));

		// - Completion
		store.setDefault(CSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION, true);
		store.setDefault(CSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON, true);

		// - Format
		store.setDefault(CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS, true);
		store.setDefault(CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES, true);
		store.setDefault(CSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR, false);
		store.setDefault(CSS_PREFERENCES_FORMAT_BRACE_STYLE, "collapse");
		store.setDefault(CSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES, true);
		// store.setDefault(CSS_PREFERENCES_FORMAT_MAXPRESERVENEWLINES, null);

		// - Hover
		store.setDefault(CSS_PREFERENCES_HOVER_DOCUMENTATION, true);
		store.setDefault(CSS_PREFERENCES_HOVER_REFERENCES, true);

		// - Validation
		store.setDefault(CSS_PREFERENCES_VALIDATE, true);
		store.setDefault(CSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_VENDOR_PREFIX, "warning");
		store.setDefault(CSS_PREFERENCES_LINT_DUPLICATEPROPERTIES, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_EMPTYRULES, "warning");
		store.setDefault(CSS_PREFERENCES_LINT_IMPORTSTATEMENT, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_BOXMODEL, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_UNIVERSALSELECTOR, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_ZEROUNITS, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_FONTFACEPROPERTIES, "warning");
		store.setDefault(CSS_PREFERENCES_LINT_HEXCOLORLENGTH, "error");
		store.setDefault(CSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION, "error");
		store.setDefault(CSS_PREFERENCES_LINT_UNKNOWNPROPERTIES, "warning");
		store.setDefault(CSS_PREFERENCES_LINT_VALIDPROPERTIES, "");
		store.setDefault(CSS_PREFERENCES_LINT_IEHACK, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY, "warning");
		store.setDefault(CSS_PREFERENCES_LINT_IMPORTANT, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_FLOAT, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_IDSELECTOR, "ignore");
		store.setDefault(CSS_PREFERENCES_LINT_UNKNOWNATRULES, "warning");

	}

	/**
	 * Returns true if the given section matches CSS settings and false otherwise.
	 * 
	 * @param section the section to check.
	 * 
	 * @return true if the given section matches CSS settings and false otherwise.
	 */
	public static boolean isMatchCssSection(String section) {
		return isMatchSection(section, CSS_SECTION);
	}
}
