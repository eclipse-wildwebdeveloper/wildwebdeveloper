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
package org.eclipse.wildwebdeveloper.html.ui.preferences;

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CLOSING_TAGS;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CREATE_QUOTES;
import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * HTML preference server constants.
 *
 */
public class HTMLPreferenceServerConstants {

	private static final String HTML_SECTION = "html";

	// Completion settings
	public static final String HTML_PREFERENCES_SUGGEST_HTML5 = "html.suggest.html5";
	public static final String HTML_PREFERENCES_COMPLETION_ATTRIBUTE_DEFAULT_VALUE = "html.completion.attributeDefaultValue";

	// Format settings
	public static final String HTML_PREFERENCES_FORMAT_WRAP_LINE_LENGTH = "html.format.wrapLineLength";
	public static final String HTML_PREFERENCES_FORMAT_UNFORMATTED = "html.format.unformatted";
	public static final String HTML_PREFERENCES_FORMAT_CONTENT_UNFORMATTED = "html.format.contentUnformatted";
	public static final String HTML_PREFERENCES_FORMAT_INDENT_INNER_HTML = "html.format.indentInnerHtml";
	public static final String HTML_PREFERENCES_FORMAT_PRESERVE_NEW_LINES = "html.format.preserveNewLines";
	public static final String HTML_PREFERENCES_FORMAT_MAX_PRESERVE_NEW_LINES = "html.format.maxPreserveNewLines";
	public static final String HTML_PREFERENCES_FORMAT_INDENT_HANDLE_BARS = "html.format.indentHandlebars";
	public static final String HTML_PREFERENCES_FORMAT_EXTRA_LINERS = "html.format.extraLiners";
	public static final String HTML_PREFERENCES_FORMAT_WRAP_ATTRIBUTES = "html.format.wrapAttributes";
	public static final String HTML_PREFERENCES_FORMAT_WRAP_ATTRIBUTES_INDENT_SIZE = "html.format.wrapAttributesIndentSize";
	public static final String HTML_PREFERENCES_FORMAT_TEMPLATING = "html.format.templating";
	public static final String HTML_PREFERENCES_FORMAT_UNFORMATTED_CONTENT_DELIMITER = "html.format.unformattedContentDelimiter";

	// Hover settings
	public static final String HTML_PREFERENCES_HOVER_DOCUMENTATION = "html.hover.documentation";
	public static final String HTML_PREFERENCES_HOVER_REFERENCES = "html.hover.references";

	// Validation settings
	public static final String HTML_PREFERENCES_VALIDATE_SCRIPTS = "html.validate.scripts";
	public static final String HTML_PREFERENCES_VALIDATE_STYLES = "html.validate.styles";

	public static Settings getGlobalSettings() {
		Settings settings = new Settings(Activator.getDefault().getPreferenceStore());

		// Completion settings
		settings.fillAsString(HTML_PREFERENCES_COMPLETION_ATTRIBUTE_DEFAULT_VALUE);
		settings.fillAsBoolean(HTML_PREFERENCES_SUGGEST_HTML5);

		// Format settings
		settings.fillAsInt(HTML_PREFERENCES_FORMAT_WRAP_LINE_LENGTH);
		settings.fillAsString(HTML_PREFERENCES_FORMAT_UNFORMATTED);
		settings.fillAsString(HTML_PREFERENCES_FORMAT_CONTENT_UNFORMATTED);
		settings.fillAsBoolean(HTML_PREFERENCES_FORMAT_INDENT_INNER_HTML);
		settings.fillAsBoolean(HTML_PREFERENCES_FORMAT_PRESERVE_NEW_LINES);
		// Cannot implement this preference since it can be number/null
		// fillIntSetting(HTML_PREFERENCES_FORMAT_MAXPRESERVENEWLINES);
		settings.fillAsBoolean(HTML_PREFERENCES_FORMAT_INDENT_HANDLE_BARS);
		settings.fillAsString(HTML_PREFERENCES_FORMAT_EXTRA_LINERS);
		settings.fillAsString(HTML_PREFERENCES_FORMAT_WRAP_ATTRIBUTES);
		// Cannot implement this preference since it can be number/null
		// STORE.setDefault(HTML_PREFERENCES_FORMAT_WRAPATTRIBUTESINDENTSIZE, null);
		settings.fillAsBoolean(HTML_PREFERENCES_FORMAT_TEMPLATING);
		settings.fillAsString(HTML_PREFERENCES_FORMAT_UNFORMATTED_CONTENT_DELIMITER);

		// Hover settings
		settings.fillAsBoolean(HTML_PREFERENCES_HOVER_DOCUMENTATION);
		settings.fillAsBoolean(HTML_PREFERENCES_HOVER_REFERENCES);

		// Validation settings
		settings.fillAsBoolean(HTML_PREFERENCES_VALIDATE_SCRIPTS);
		settings.fillAsBoolean(HTML_PREFERENCES_VALIDATE_STYLES);

		return settings;
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Client settings
		store.setDefault(HTML_PREFERENCES_AUTO_CLOSING_TAGS, true);
		store.setDefault(HTML_PREFERENCES_AUTO_CREATE_QUOTES, true);

		// Server settings

		// - Completion
		store.setDefault(HTML_PREFERENCES_COMPLETION_ATTRIBUTE_DEFAULT_VALUE, "doublequotes");
		store.setDefault(HTML_PREFERENCES_SUGGEST_HTML5, true);

		// - Format
		store.setDefault(HTML_PREFERENCES_FORMAT_WRAP_LINE_LENGTH, 120);
		store.setDefault(HTML_PREFERENCES_FORMAT_UNFORMATTED, "wbr");
		store.setDefault(HTML_PREFERENCES_FORMAT_CONTENT_UNFORMATTED, "pre,code,textarea");
		store.setDefault(HTML_PREFERENCES_FORMAT_INDENT_INNER_HTML, false);
		store.setDefault(HTML_PREFERENCES_FORMAT_PRESERVE_NEW_LINES, true);
		// STORE.setDefault(HTML_PREFERENCES_FORMAT_MAXPRESERVENEWLINES, null);
		store.setDefault(HTML_PREFERENCES_FORMAT_INDENT_HANDLE_BARS, false);
		store.setDefault(HTML_PREFERENCES_FORMAT_EXTRA_LINERS, "head, body, /html");
		store.setDefault(HTML_PREFERENCES_FORMAT_WRAP_ATTRIBUTES, "auto");
		// STORE.setDefault(HTML_PREFERENCES_FORMAT_WRAPATTRIBUTESINDENTSIZE, null);
		store.setDefault(HTML_PREFERENCES_FORMAT_TEMPLATING, false);
		store.setDefault(HTML_PREFERENCES_FORMAT_UNFORMATTED_CONTENT_DELIMITER, "");

		// - Hover
		store.setDefault(HTML_PREFERENCES_HOVER_DOCUMENTATION, true);
		store.setDefault(HTML_PREFERENCES_HOVER_REFERENCES, true);

		// - Validation
		store.setDefault(HTML_PREFERENCES_VALIDATE_SCRIPTS, true);
		store.setDefault(HTML_PREFERENCES_VALIDATE_STYLES, true);
	}

	/**
	 * Returns true if the given section matches HTML settings and false otherwise.
	 * 
	 * @param section the section to check.
	 * 
	 * @return true if the given section matches HTML settings and false otherwise.
	 */
	public static boolean isMatchHtmlSection(String section) {
		return isMatchSection(section, HTML_SECTION);
	}
}
