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
package org.eclipse.wildwebdeveloper.css.ui.preferences.less;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * LESS preference server constants.
 *
 */
public class LESSPreferenceServerConstants {

	private static final String LESS_SECTION = "less";

	// public static final String LESS_PREFERENCES_CUSTOMDATA = "less.customData";

	// Completion settings
	public static final String LESS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION = "less.completion.triggerPropertyValueCompletion";
	public static final String LESS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON = "less.completion.completePropertyWithSemicolon";

	// Format settings
	// public static final String LESS_PREFERENCES_FORMAT_ENABLE =
	// "less.format.enable";
	public static final String LESS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS = "less.format.newlineBetweenSelectors";
	public static final String LESS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES = "less.format.newlineBetweenRules";
	public static final String LESS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR = "less.format.spaceAroundSelectorSeparator";
	public static final String LESS_PREFERENCES_FORMAT_BRACE_STYLE = "less.format.braceStyle";
	public static final String LESS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES = "less.format.preserveNewLines";
	public static final String LESS_PREFERENCES_FORMAT_MAXP_RESERVE_NEW_LINES = "less.format.maxPreserveNewLines";

	// Hover settings
	public static final String LESS_PREFERENCES_HOVER_DOCUMENTATION = "less.hover.documentation";
	public static final String LESS_PREFERENCES_HOVER_REFERENCES = "less.hover.references";

	// Validation settings
	public static final String LESS_PREFERENCES_VALIDATE = "less.validate";
	public static final String LESS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES = "less.lint.compatibleVendorPrefixes";
	public static final String LESS_PREFERENCES_LINT_VENDOR_PREFIX = "less.lint.vendorPrefix";
	public static final String LESS_PREFERENCES_LINT_DUPLICATEPROPERTIES = "less.lint.duplicateProperties";
	public static final String LESS_PREFERENCES_LINT_EMPTYRULES = "less.lint.emptyRules";
	public static final String LESS_PREFERENCES_LINT_IMPORTSTATEMENT = "less.lint.importStatement";
	public static final String LESS_PREFERENCES_LINT_BOXMODEL = "less.lint.boxModel";
	public static final String LESS_PREFERENCES_LINT_UNIVERSALSELECTOR = "less.lint.universalSelector";
	public static final String LESS_PREFERENCES_LINT_ZEROUNITS = "less.lint.zeroUnits";
	public static final String LESS_PREFERENCES_LINT_FONTFACEPROPERTIES = "less.lint.fontFaceProperties";
	public static final String LESS_PREFERENCES_LINT_HEXCOLORLENGTH = "less.lint.hexColorLength";
	public static final String LESS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION = "less.lint.argumentsInColorFunction";
	public static final String LESS_PREFERENCES_LINT_UNKNOWNPROPERTIES = "less.lint.unknownProperties";
	public static final String LESS_PREFERENCES_LINT_VALIDPROPERTIES = "less.lint.validProperties";
	public static final String LESS_PREFERENCES_LINT_IEHACK = "less.lint.ieHack";
	public static final String LESS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES = "less.lint.unknownVendorSpecificProperties";
	public static final String LESS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY = "less.lint.propertyIgnoredDueToDisplay";
	public static final String LESS_PREFERENCES_LINT_IMPORTANT = "less.lint.important";
	public static final String LESS_PREFERENCES_LINT_FLOAT = "less.lint.float";
	public static final String LESS_PREFERENCES_LINT_IDSELECTOR = "less.lint.idSelector";
	public static final String LESS_PREFERENCES_LINT_UNKNOWNATRULES = "less.lint.unknownAtRules";

	public static Settings getGlobalSettings() {
		Settings settings = new Settings(Activator.getDefault().getPreferenceStore());

		// Completion settings
		settings.fillAsBoolean(LESS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION);
		settings.fillAsBoolean(LESS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON);

		// Format settings
		settings.fillAsBoolean(LESS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS);
		settings.fillAsBoolean(LESS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES);
		settings.fillAsBoolean(LESS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR);
		settings.fillAsString(LESS_PREFERENCES_FORMAT_BRACE_STYLE);
		settings.fillAsBoolean(LESS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES);
		// Cannot implement this preference since it can be number/null
		// settings.fillAsInt(LESS_PREFERENCES_FORMAT_MAXPRESERVENEWLINES);

		// Hover settings
		settings.fillAsBoolean(LESS_PREFERENCES_HOVER_DOCUMENTATION);
		settings.fillAsBoolean(LESS_PREFERENCES_HOVER_REFERENCES);

		// Validation settings
		settings.fillAsBoolean(LESS_PREFERENCES_VALIDATE);
		settings.fillAsString(LESS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES);

		settings.fillAsString(LESS_PREFERENCES_LINT_VENDOR_PREFIX);
		settings.fillAsString(LESS_PREFERENCES_LINT_DUPLICATEPROPERTIES);
		settings.fillAsString(LESS_PREFERENCES_LINT_EMPTYRULES);
		settings.fillAsString(LESS_PREFERENCES_LINT_IMPORTSTATEMENT);
		settings.fillAsString(LESS_PREFERENCES_LINT_BOXMODEL);
		settings.fillAsString(LESS_PREFERENCES_LINT_UNIVERSALSELECTOR);
		settings.fillAsString(LESS_PREFERENCES_LINT_ZEROUNITS);
		settings.fillAsString(LESS_PREFERENCES_LINT_FONTFACEPROPERTIES);
		settings.fillAsString(LESS_PREFERENCES_LINT_HEXCOLORLENGTH);
		settings.fillAsString(LESS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION);
		settings.fillAsString(LESS_PREFERENCES_LINT_UNKNOWNPROPERTIES);
		settings.fillAsStringArray(LESS_PREFERENCES_LINT_VALIDPROPERTIES, ",");
		settings.fillAsString(LESS_PREFERENCES_LINT_IEHACK);
		settings.fillAsString(LESS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES);
		settings.fillAsString(LESS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY);
		settings.fillAsString(LESS_PREFERENCES_LINT_IMPORTANT);
		settings.fillAsString(LESS_PREFERENCES_LINT_FLOAT);
		settings.fillAsString(LESS_PREFERENCES_LINT_IDSELECTOR);
		settings.fillAsString(LESS_PREFERENCES_LINT_UNKNOWNATRULES);

		return settings;
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Server settings

		// STORE.setDefault(LESS_PREFERENCES_CUSTOMDATA, Arrays.asList(""));

		// - Completion
		store.setDefault(LESS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION, true);
		store.setDefault(LESS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON, true);

		// - Format
		store.setDefault(LESS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS, true);
		store.setDefault(LESS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES, true);
		store.setDefault(LESS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR, false);
		store.setDefault(LESS_PREFERENCES_FORMAT_BRACE_STYLE, "collapse");
		store.setDefault(LESS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES, true);
		// store.setDefault(LESS_PREFERENCES_FORMAT_MAXPRESERVENEWLINES, null);

		// - Hover
		store.setDefault(LESS_PREFERENCES_HOVER_DOCUMENTATION, true);
		store.setDefault(LESS_PREFERENCES_HOVER_REFERENCES, true);

		// - Validation
		store.setDefault(LESS_PREFERENCES_VALIDATE, true);
		store.setDefault(LESS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_VENDOR_PREFIX, "warning");
		store.setDefault(LESS_PREFERENCES_LINT_DUPLICATEPROPERTIES, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_EMPTYRULES, "warning");
		store.setDefault(LESS_PREFERENCES_LINT_IMPORTSTATEMENT, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_BOXMODEL, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_UNIVERSALSELECTOR, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_ZEROUNITS, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_FONTFACEPROPERTIES, "warning");
		store.setDefault(LESS_PREFERENCES_LINT_HEXCOLORLENGTH, "error");
		store.setDefault(LESS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION, "error");
		store.setDefault(LESS_PREFERENCES_LINT_UNKNOWNPROPERTIES, "warning");
		store.setDefault(LESS_PREFERENCES_LINT_VALIDPROPERTIES, "");
		store.setDefault(LESS_PREFERENCES_LINT_IEHACK, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY, "warning");
		store.setDefault(LESS_PREFERENCES_LINT_IMPORTANT, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_FLOAT, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_IDSELECTOR, "ignore");
		store.setDefault(LESS_PREFERENCES_LINT_UNKNOWNATRULES, "warning");

	}

	/**
	 * Returns true if the given section matches LESS settings and false otherwise.
	 * 
	 * @param section the section to check.
	 * 
	 * @return true if the given section matches LESS settings and false otherwise.
	 */
	public static boolean isMatchLessSection(String section) {
		return isMatchSection(section, LESS_SECTION);
	}
}
