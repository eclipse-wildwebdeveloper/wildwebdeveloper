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
package org.eclipse.wildwebdeveloper.yaml.ui;

import org.eclipse.osgi.util.NLS;

/**
 * YAML messages keys.
 *
 */
public class Messages extends NLS {

	// --------- YAML Main preference page
	public static String YAMLPreferencePage_SchemaAssociationsLink;
	public static String YAMLPreferencePage_schemaStore_enable;
	public static String YAMLPreferencePage_schemaStore_url;
	public static String YAMLPreferencePage_maxItemsComputed;

	// --------- YAML Completion preference page
	public static String YAMLCompletionPreferencePage_completion;
	public static String YAMLCompletionPreferencePage_suggest_parentSkeletonSelectedFirst;
	public static String YAMLCompletionPreferencePage_disableDefaultProperties;

	// --------- YAML Format preference page
	public static String YAMLFormatPreferencePage_format_enable;
	public static String YAMLFormatPreferencePage_format_singleQuote;
	public static String YAMLFormatPreferencePage_format_bracketSpacing;
	public static String YAMLFormatPreferencePage_format_proseWrap;
	public static String YAMLFormatPreferencePage_format_proseWrap_preserve;
	public static String YAMLFormatPreferencePage_format_proseWrap_never;
	public static String YAMLFormatPreferencePage_format_proseWrap_always;
	public static String YAMLFormatPreferencePage_format_printWidth;

	// --------- YAML Hover preference page
	public static String YAMLHoverPreferencePage_hover;

	// --------- YAML Validation preference page
	public static String YAMLValidationPreferencePage_validate;
	public static String YAMLValidationPreferencePage_yamlVersion;
	public static String YAMLValidationPreferencePage_customTags;
	public static String CustomTagsFieldEditor_inputDialog_title;
	public static String CustomTagsFieldEditor_inputDialog_description;
	public static String YAMLValidationPreferencePage_disableAdditionalProperties;
	public static String YAMLValidationPreferencePage_style_flowMapping;
	public static String YAMLValidationPreferencePage_style_flowSequence;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.yaml.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
