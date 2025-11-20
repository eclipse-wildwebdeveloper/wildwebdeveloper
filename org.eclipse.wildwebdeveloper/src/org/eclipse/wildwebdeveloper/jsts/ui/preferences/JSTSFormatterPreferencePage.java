/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.jsts.ui.Messages;

/**
 * JavaScript / TypeScript Formatter preference page.
 */
public abstract class JSTSFormatterPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private final JSTSLanguagePreferences prefs;

	protected JSTSFormatterPreferencePage(JSTSLanguagePreferences prefs) {
		super(GRID);
		this.prefs = prefs;
	}

	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		// indentation settings
		addField(new IntegerFieldEditor(prefs.format_baseIndentSize, Messages.FormatterPreferencePage_baseIndentSize,
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(prefs.format_indentSize, Messages.FormatterPreferencePage_indentSize, parent));
		addField(new ComboFieldEditor(prefs.format_indentStyle,
				Messages.FormatterPreferencePage_indentStyle,
				new String[][] {
					{ "None: no indent", "0" },
					{ "Blok: indent statements inside blocks", "1" },
					{ "Smart: context-aware indendation", "2" } },
				parent));
		addField(new BooleanFieldEditor(prefs.format_convertTabsToSpaces, Messages.FormatterPreferencePage_convertTabsToSpaces, parent));

		// whitespace settings
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterCommaDelimiter,
				Messages.FormatterPreferencePage_insertSpaceAfterCommaDelimiter, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterConstructor,
				Messages.FormatterPreferencePage_insertSpaceAfterConstructor, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterFunctionKeywordForAnonymousFunctions,
				Messages.FormatterPreferencePage_insertSpaceAfterFunctionKeywordForAnonymousFunctions, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterKeywordsInControlFlowStatements,
				Messages.FormatterPreferencePage_insertSpaceAfterKeywordsInControlFlowStatements, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces,
				Messages.FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces,
				Messages.FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces,
				Messages.FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets,
				Messages.FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis,
				Messages.FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces,
				Messages.FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterSemicolonInForStatements,
				Messages.FormatterPreferencePage_insertSpaceAfterSemicolonInForStatements, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceAfterTypeAssertion,
				Messages.FormatterPreferencePage_insertSpaceAfterTypeAssertion, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceBeforeAndAfterBinaryOperators,
				Messages.FormatterPreferencePage_insertSpaceBeforeAndAfterBinaryOperators, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceBeforeFunctionParenthesis,
				Messages.FormatterPreferencePage_insertSpaceBeforeFunctionParenthesis, parent));
		addField(new BooleanFieldEditor(prefs.format_insertSpaceBeforeTypeAnnotation,
				Messages.FormatterPreferencePage_insertSpaceBeforeTypeAnnotation, parent));
		addField(new ComboFieldEditor(prefs.format_newLineCharacter,
				Messages.FormatterPreferencePage_newLineCharacter,
				new String[][] {
					{ "\\n (Linux/MacOS)", "\n" },
					{ "\\r\\n (Windows)", "\r\n" } },
				parent));
		addField(new BooleanFieldEditor(prefs.format_placeOpenBraceOnNewLineForControlBlocks,
				Messages.FormatterPreferencePage_placeOpenBraceOnNewLineForControlBlocks, parent));
		addField(new BooleanFieldEditor(prefs.format_placeOpenBraceOnNewLineForFunctions,
				Messages.FormatterPreferencePage_placeOpenBraceOnNewLineForFunctions, parent));
		addField(new ComboFieldEditor(prefs.format_semicolons,
				Messages.FormatterPreferencePage_semicolons,
				new String[][] {
					{ "Ignore: leave unchanged", "ignore" },
					{ "Insert: always insert", "insert" },
					{ "Remove: remove where possible", "remove" } },
				parent));
		addField(new BooleanFieldEditor(prefs.format_trimTrailingWhitespace,
				Messages.FormatterPreferencePage_trimTrailingWhitespace, parent));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
