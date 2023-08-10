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
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_PRESERVE_SPACE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * XML formatting preference page.
 *
 */
public class XMLFormattingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLFormattingPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		// Settings for general
		addField(new IntegerFieldEditor(XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH.preferenceId,
				Messages.XMLFormattingPreferencePage_format_maxLineWidth, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING.preferenceId,
				Messages.XMLFormattingPreferencePage_format_grammarAwareFormatting, getFieldEditorParent()));

		// Settings for tag elements
		addField(new ComboFieldEditor(XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS.preferenceId,
				Messages.XMLFormattingPreferencePage_format_emptyElements,
				new String[][] { { Action.removeMnemonics(IDialogConstants.IGNORE_LABEL), "ignore" },
						{ Messages.XMLFormattingPreferencePage_format_emptyElements_collapse, "collapse" },
						{ Messages.XMLFormattingPreferencePage_format_emptyElements_expand, "expand" } },
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG.preferenceId,
				Messages.XMLFormattingPreferencePage_format_spaceBeforeEmptyCloseTag, getFieldEditorParent()));

		// Settings for attributes
		addField(new ComboFieldEditor(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES.preferenceId,
				Messages.XMLFormattingPreferencePage_format_splitAttributes,
				new String[][] { 
					{ Messages.XMLFormattingPreferencePage_format_splitAttribute_spreserve, "preserve" }, 
					{ Messages.XMLFormattingPreferencePage_format_splitAttributes_splitNewLine, "splitNewLine" }, 
					{ Messages.XMLFormattingPreferencePage_format_splitAttributes_alignWithFirstAttr, "alignWithFirstAttr" } 
				}, getFieldEditorParent()));
		addField(new IntegerFieldEditor(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE.preferenceId,
				Messages.XMLFormattingPreferencePage_format_splitAttributesIndentSize, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE.preferenceId,
				Messages.XMLFormattingPreferencePage_format_closingBracketNewLine, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS.preferenceId,
				Messages.XMLFormattingPreferencePage_format_preserveAttributeLineBreaks, getFieldEditorParent()));
		addField(new ComboFieldEditor(XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT.preferenceId,
				Messages.XMLFormattingPreferencePage_format_xsiSchemaLocationSplit,
				new String[][] {
						{ Messages.XMLFormattingPreferencePage_format_xsiSchemaLocationSplit_onElement, "onElement" },
						{ Messages.XMLFormattingPreferencePage_format_xsiSchemaLocationSplit_onPair, "onPair" },
						{ Messages.XMLFormattingPreferencePage_format_xsiSchemaLocationSplit_none, "none" } },
				getFieldEditorParent()));

		// Format settings for Text content
		addField(new IntegerFieldEditor(XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES.preferenceId,
				Messages.XMLFormattingPreferencePage_format_preservedNewlines, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES.preferenceId,
				Messages.XMLFormattingPreferencePage_format_joinContentLines, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE.preferenceId,
				Messages.XMLFormattingPreferencePage_format_insertFinalNewline, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES.preferenceId,
				Messages.XMLFormattingPreferencePage_format_trimFinalNewlines, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE.preferenceId,
				Messages.XMLFormattingPreferencePage_format_trimTrailingWhitespace, getFieldEditorParent()));
		addField(new PreserveSpaceFieldEditor(XML_PREFERENCES_FORMAT_PRESERVE_SPACE.preferenceId,
				Messages.XMLFormattingPreferencePage_format_preserveSpace, getFieldEditorParent()));

		// Settings for comments
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES.preferenceId,
				Messages.XMLFormattingPreferencePage_format_joinCommentLines, getFieldEditorParent()));

		// Settings for CDATA
		addField(new BooleanFieldEditor(XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES.preferenceId,
				Messages.XMLFormattingPreferencePage_format_joinCDATALines, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
