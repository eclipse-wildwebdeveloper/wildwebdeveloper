/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.html.ui;

import org.eclipse.osgi.util.NLS;

/**
 * HTML messages keys.
 *
 */
public class Messages extends NLS {

	// --------- HTML Main preference page
	public static String HTMLPreferencePage_autoClosingTags;
	public static String HTMLPreferencePage_autoCreateQuotes;

	// --------- HTML Completion preference page
	public static String HTMLCompletionPreferencePage_completion_attributeDefaultValue;
	public static String HTMLCompletionPreferencePage_completion_attributeDefaultValue_doublequotes;
	public static String HTMLCompletionPreferencePage_completion_attributeDefaultValue_singlequotes;
	public static String HTMLCompletionPreferencePage_completion_attributeDefaultValue_empty;
	public static String HTMLCompletionPreferencePage_suggest_html5;

	// --------- HTML Format preference page
	public static String HTMLFormatPreferencePage_format_wrapLineLength;
	public static String HTMLFormatPreferencePage_format_unformatted;
	public static String HTMLFormatPreferencePage_format_contentUnformatted;
	public static String HTMLFormatPreferencePage_format_indentInnerHtml;
	public static String HTMLFormatPreferencePage_format_preserveNewLines;
	public static String HTMLFormatPreferencePage_format_maxPreserveNewLines;
	public static String HTMLFormatPreferencePage_format_indentHandlebars;
	public static String HTMLFormatPreferencePage_format_extraLiners;
	public static String HTMLFormatPreferencePage_format_wrapAttributes;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_auto;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_force;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_forcealign;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_forcemultiline;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_alignedmultiple;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_preserve;
	public static String HTMLFormatPreferencePage_format_wrapAttributes_preservealigned;
	public static String HTMLFormatPreferencePage_format_wrapAttributesIndentSize;
	public static String HTMLFormatPreferencePage_format_templating;
	public static String HTMLFormatPreferencePage_format_unformattedContentDelimiter;

	// --------- HTML Hover preference page
	public static String HTMLHoverPreferencePage_hover_documentation;
	public static String HTMLHoverPreferencePage_hover_references;

	// --------- HTML Validation preference page
	public static String HTMLValidationPreferencePage_validate_scripts;
	public static String HTMLValidationPreferencePage_validate_styles;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.html.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
