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
package org.eclipse.wildwebdeveloper.css.ui;

import org.eclipse.osgi.util.NLS;

/**
 * CSS, LESS, SCSS messages keys.
 *
 */
public class Messages extends NLS {

	// --------- Valid Properties FieldEditor
	public static String ValidPropertiesFieldEditor_inputDialog_title;
	public static String ValidPropertiesFieldEditor_inputDialog_description;
	
	// --------- CSS Completion preference page
	public static String CSSCompletionPreferencePage_completion_triggerPropertyValueCompletion;
	public static String CSSCompletionPreferencePage_completion_completePropertyWithSemicolon;

	// --------- CSS Format preference page
	public static String CSSFormatPreferencePage_format_newlineBetweenSelectors;
	public static String CSSFormatPreferencePage_format_newlineBetweenRules;
	public static String CSSFormatPreferencePage_format_spaceAroundSelectorSeparator;
	public static String CSSFormatPreferencePage_format_braceStyle;
	public static String CSSFormatPreferencePage_format_braceStyle_collapse;
	public static String CSSFormatPreferencePage_format_braceStyle_expand;
	public static String CSSFormatPreferencePage_format_preserveNewLines;
	public static String CSSFormatPreferencePage_format_maxPreserveNewLines;

	// --------- CSS Hover preference page
	public static String CSSHoverPreferencePage_hover_documentation;
	public static String CSSHoverPreferencePage_hover_references;

	// --------- CSS Validation preference page
	public static String CSSValidationPreferencePage_validate;
	public static String CSSValidationPreferencePage_lint_compatibleVendorPrefixes;
	public static String CSSValidationPreferencePage_lint_vendorPrefix;
	public static String CSSValidationPreferencePage_lint_duplicateProperties;
	public static String CSSValidationPreferencePage_lint_emptyRules;
	public static String CSSValidationPreferencePage_lint_importStatement;
	public static String CSSValidationPreferencePage_lint_boxModel;
	public static String CSSValidationPreferencePage_lint_universalSelector;
	public static String CSSValidationPreferencePage_lint_zeroUnits;
	public static String CSSValidationPreferencePage_lint_fontFaceProperties;
	public static String CSSValidationPreferencePage_lint_hexColorLength;
	public static String CSSValidationPreferencePage_lint_argumentsInColorFunction;
	public static String CSSValidationPreferencePage_lint_unknownProperties;
	public static String CSSValidationPreferencePage_lint_validProperties;
	public static String CSSValidationPreferencePage_lint_ieHack;
	public static String CSSValidationPreferencePage_lint_unknownVendorSpecificProperties;
	public static String CSSValidationPreferencePage_lint_propertyIgnoredDueToDisplay;
	public static String CSSValidationPreferencePage_lint_important;
	public static String CSSValidationPreferencePage_lint_float;
	public static String CSSValidationPreferencePage_lint_idSelector;
	public static String CSSValidationPreferencePage_lint_unknownAtRules;

	// --------- LESS Completion preference page
	public static String LESSCompletionPreferencePage_completion_triggerPropertyValueCompletion;
	public static String LESSCompletionPreferencePage_completion_completePropertyWithSemicolon;

	// --------- LESS Format preference page
	public static String LESSFormatPreferencePage_format_newlineBetweenSelectors;
	public static String LESSFormatPreferencePage_format_newlineBetweenRules;
	public static String LESSFormatPreferencePage_format_spaceAroundSelectorSeparator;
	public static String LESSFormatPreferencePage_format_braceStyle;
	public static String LESSFormatPreferencePage_format_braceStyle_collapse;
	public static String LESSFormatPreferencePage_format_braceStyle_expand;
	public static String LESSFormatPreferencePage_format_preserveNewLines;
	public static String LESSFormatPreferencePage_format_maxPreserveNewLines;

	// --------- LESS Hover preference page
	public static String LESSHoverPreferencePage_hover_documentation;
	public static String LESSHoverPreferencePage_hover_references;

	// --------- LESS Validation preference page
	public static String LESSValidationPreferencePage_validate;
	public static String LESSValidationPreferencePage_lint_compatibleVendorPrefixes;
	public static String LESSValidationPreferencePage_lint_vendorPrefix;
	public static String LESSValidationPreferencePage_lint_duplicateProperties;
	public static String LESSValidationPreferencePage_lint_emptyRules;
	public static String LESSValidationPreferencePage_lint_importStatement;
	public static String LESSValidationPreferencePage_lint_boxModel;
	public static String LESSValidationPreferencePage_lint_universalSelector;
	public static String LESSValidationPreferencePage_lint_zeroUnits;
	public static String LESSValidationPreferencePage_lint_fontFaceProperties;
	public static String LESSValidationPreferencePage_lint_hexColorLength;
	public static String LESSValidationPreferencePage_lint_argumentsInColorFunction;
	public static String LESSValidationPreferencePage_lint_unknownProperties;
	public static String LESSValidationPreferencePage_lint_validProperties;
	public static String LESSValidationPreferencePage_lint_ieHack;
	public static String LESSValidationPreferencePage_lint_unknownVendorSpecificProperties;
	public static String LESSValidationPreferencePage_lint_propertyIgnoredDueToDisplay;
	public static String LESSValidationPreferencePage_lint_important;
	public static String LESSValidationPreferencePage_lint_float;
	public static String LESSValidationPreferencePage_lint_idSelector;
	public static String LESSValidationPreferencePage_lint_unknownAtRules;

	// --------- SCSS Completion preference page
	public static String SCSSCompletionPreferencePage_completion_triggerPropertyValueCompletion;
	public static String SCSSCompletionPreferencePage_completion_completePropertyWithSemicolon;

	// --------- SCSS Format preference page
	public static String SCSSFormatPreferencePage_format_newlineBetweenSelectors;
	public static String SCSSFormatPreferencePage_format_newlineBetweenRules;
	public static String SCSSFormatPreferencePage_format_spaceAroundSelectorSeparator;
	public static String SCSSFormatPreferencePage_format_braceStyle;
	public static String SCSSFormatPreferencePage_format_braceStyle_collapse;
	public static String SCSSFormatPreferencePage_format_braceStyle_expand;
	public static String SCSSFormatPreferencePage_format_preserveNewLines;
	public static String SCSSFormatPreferencePage_format_maxPreserveNewLines;

	// --------- SCSS Hover preference page
	public static String SCSSHoverPreferencePage_hover_documentation;
	public static String SCSSHoverPreferencePage_hover_references;

	// --------- SCSS Validation preference page
	public static String SCSSValidationPreferencePage_validate;
	public static String SCSSValidationPreferencePage_lint_compatibleVendorPrefixes;
	public static String SCSSValidationPreferencePage_lint_vendorPrefix;
	public static String SCSSValidationPreferencePage_lint_duplicateProperties;
	public static String SCSSValidationPreferencePage_lint_emptyRules;
	public static String SCSSValidationPreferencePage_lint_importStatement;
	public static String SCSSValidationPreferencePage_lint_boxModel;
	public static String SCSSValidationPreferencePage_lint_universalSelector;
	public static String SCSSValidationPreferencePage_lint_zeroUnits;
	public static String SCSSValidationPreferencePage_lint_fontFaceProperties;
	public static String SCSSValidationPreferencePage_lint_hexColorLength;
	public static String SCSSValidationPreferencePage_lint_argumentsInColorFunction;
	public static String SCSSValidationPreferencePage_lint_unknownProperties;
	public static String SCSSValidationPreferencePage_lint_validProperties;
	public static String SCSSValidationPreferencePage_lint_ieHack;
	public static String SCSSValidationPreferencePage_lint_unknownVendorSpecificProperties;
	public static String SCSSValidationPreferencePage_lint_propertyIgnoredDueToDisplay;
	public static String SCSSValidationPreferencePage_lint_important;
	public static String SCSSValidationPreferencePage_lint_float;
	public static String SCSSValidationPreferencePage_lint_idSelector;
	public static String SCSSValidationPreferencePage_lint_unknownAtRules;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.css.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
