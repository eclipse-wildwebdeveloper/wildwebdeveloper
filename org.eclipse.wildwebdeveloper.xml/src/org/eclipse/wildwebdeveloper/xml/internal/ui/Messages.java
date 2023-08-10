/*******************************************************************************
 * Copyright (c) 2020, 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	// --------- XML Preference page
	public static String XMLPreferencePage_XMLCatalogsLink;
	public static String XMLPreferencePage_downloadExternalResources_enabled;
	public static String XMLPreferencePage_completion_autoCloseTags;

	// --------- XML Catalog preference page
	public static String XMLCatalogPreferencePage_Entries;
	public static String XMLCatalogPreferencePage_Edit;
	public static String XMLCatalogPreferencePage_OpenInEditorTitle;
	public static String XMLCatalogPreferencePage_OpenInEditorMessage;
	public static String XMLCatalogPreferencePage_OpenInEditorApplyAndEdit;

	// --------- XML CodeLens preference page
	public static String XMLCodelensPreferencePage_codelens_enabled;

	// --------- XML Formatting preference page
	public static String XMLFormattingPreferencePage_format_maxLineWidth;
	public static String XMLFormattingPreferencePage_format_grammarAwareFormatting;
	public static String XMLFormattingPreferencePage_format_emptyElements;
	public static String XMLFormattingPreferencePage_format_emptyElements_collapse;
	public static String XMLFormattingPreferencePage_format_emptyElements_expand;
	public static String XMLFormattingPreferencePage_format_spaceBeforeEmptyCloseTag;
	public static String XMLFormattingPreferencePage_format_splitAttributes;
	public static String XMLFormattingPreferencePage_format_splitAttribute_spreserve;
	public static String XMLFormattingPreferencePage_format_splitAttributes_splitNewLine;
	public static String XMLFormattingPreferencePage_format_splitAttributes_alignWithFirstAttr;
	public static String XMLFormattingPreferencePage_format_splitAttributesIndentSize;
	public static String XMLFormattingPreferencePage_format_preserveAttributeLineBreaks;
	public static String XMLFormattingPreferencePage_format_closingBracketNewLine;
	public static String XMLFormattingPreferencePage_format_xsiSchemaLocationSplit;
	public static String XMLFormattingPreferencePage_format_xsiSchemaLocationSplit_onElement;
	public static String XMLFormattingPreferencePage_format_xsiSchemaLocationSplit_onPair;
	public static String XMLFormattingPreferencePage_format_xsiSchemaLocationSplit_none;
	public static String XMLFormattingPreferencePage_format_preservedNewlines;
	public static String XMLFormattingPreferencePage_format_joinContentLines;
	public static String XMLFormattingPreferencePage_format_insertFinalNewline;
	public static String XMLFormattingPreferencePage_format_trimFinalNewlines;
	public static String XMLFormattingPreferencePage_format_trimTrailingWhitespace;
	public static String XMLFormattingPreferencePage_format_preserveSpace;
	public static String XMLFormattingPreferencePage_format_joinCommentLines;
	public static String XMLFormattingPreferencePage_format_joinCDATALines;
	
	public static String PreserveSpaceFieldEditor_inputDialog_description;
	public static String PreserveSpaceFieldEditor_inputDialog_title;

	// --------- XML Validation preference page
	public static String XMLValidationPreferencePage_validation_enabled;
	public static String XMLValidationPreferencePage_validation_namespaces_enabled;
	public static String XMLValidationPreferencePage_validation_schema_enabled;
	public static String XMLValidationPreferencePage_validation_disallowDocTypeDecl;
	public static String XMLValidationPreferencePage_validation_resolveExternalEntities;
	public static String XMLValidationPreferencePage_validation_noGrammar;

	// -------- XML Validation preference page settings
	public static String XMLValidationPreferencePage_validation_namespace_option_onNamespaceEncountered;
	public static String XMLValidationPreferencePage_validation_schema_option_onValidSchema;
	public static String XMLValidationPreferencePage_validation_noGrammar_option_hint;
  
	// --------- Associate Grammar Dialog
	public static String AssociateGrammarDialog_title;
	public static String AssociateGrammarDialog_grammar_field;
	public static String AssociateGrammarDialog_bindingType_field;
	public static String AssociateGrammarDialog_validation_grammar_file_required;
	public static String AssociateGrammarDialog_validation_grammar_file_notExists;
	public static String AssociateGrammarDialog_validation_grammar_file_invalid_fileExtension;
	public static String AssociateGrammarDialog_validation_grammar_file_invalid_association_for_relaxng;

	// --------- Buttons
	public static String Add_button;
	public static String Remove_button;
	public static String Browse_button;
	
	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.xml.internal.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
