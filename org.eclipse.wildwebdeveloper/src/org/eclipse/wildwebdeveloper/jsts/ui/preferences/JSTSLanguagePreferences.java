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
 * Angelo ZERR (Red Hat Inc.) - initial implementation
 * Sebastian Thomschke (Vegard IT GmbH) - add Formatter and Code Lens prefs handling
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences;

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * JS/TS preferences.
 * See https://github.com/typescript-language-server/typescript-language-server/blob/master/docs/configuration.md
 */
public class JSTSLanguagePreferences {

	public static final JSTSLanguagePreferences JS = new JSTSLanguagePreferences("javascript");
	public static final JSTSLanguagePreferences TS = new JSTSLanguagePreferences("typescript");

	// Formatting preferences keys
	public final String format_baseIndentSize;
	public final String format_convertTabsToSpaces;
	public final String format_indentSize;
	public final String format_indentStyle;
	public final String format_tabSize;

	public final String format_insertSpaceAfterCommaDelimiter;
	public final String format_insertSpaceAfterConstructor;
	public final String format_insertSpaceAfterFunctionKeywordForAnonymousFunctions;
	public final String format_insertSpaceAfterKeywordsInControlFlowStatements;
	public final String format_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces;
	public final String format_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces;
	public final String format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces;
	public final String format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets;
	public final String format_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis;
	public final String format_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces;
	public final String format_insertSpaceAfterSemicolonInForStatements;
	public final String format_insertSpaceAfterTypeAssertion;
	public final String format_insertSpaceBeforeAndAfterBinaryOperators;
	public final String format_insertSpaceBeforeFunctionParenthesis;
	public final String format_insertSpaceBeforeTypeAnnotation;
	public final String format_newLineCharacter;
	public final String format_placeOpenBraceOnNewLineForControlBlocks;
	public final String format_placeOpenBraceOnNewLineForFunctions;
	public final String format_semicolons;
	public final String format_trimTrailingWhitespace;

	// Inlay Hint preferences keys
	public final String inlayHints_includeInlayEnumMemberValueHints;
	public final String inlayHints_includeInlayFunctionLikeReturnTypeHints;
	public final String inlayHints_includeInlayFunctionParameterTypeHints;
	public final String inlayHints_includeInlayParameterNameHints;
	public final String inlayHints_includeInlayParameterNameHintsWhenArgumentMatchesName;
	public final String inlayHints_includeInlayPropertyDeclarationTypeHints;
	public final String inlayHints_includeInlayVariableTypeHints;
	public final String inlayHints_includeInlayVariableTypeHintsWhenTypeMatchesName;

	// Code Lens preferences keys
	public final String implementationsCodeLens_enabled;
	public final String referencesCodeLens_enabled;
	public final String referencesCodeLens_showOnAllFunctions;

	private final String section;

	private JSTSLanguagePreferences(String section) {
		this.section = section;

		// Formatting preferences keys
		format_baseIndentSize = section + ".format.baseIndentSize";
		format_convertTabsToSpaces = section + ".format.convertTabsToSpaces";
		format_indentSize = section + ".format.indentSize";
		format_indentStyle = section + ".format.indentStyle";
		format_tabSize = section + ".format.tabSize";

		format_insertSpaceAfterCommaDelimiter = section + ".format.insertSpaceAfterCommaDelimiter";
		format_insertSpaceAfterConstructor = section + ".format.insertSpaceAfterConstructor";
		format_insertSpaceAfterFunctionKeywordForAnonymousFunctions = section
				+ ".format.insertSpaceAfterFunctionKeywordForAnonymousFunctions";
		format_insertSpaceAfterKeywordsInControlFlowStatements = section + ".format.insertSpaceAfterKeywordsInControlFlowStatements";
		format_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces = section + ".format.insertSpaceAfterOpeningAndBeforeClosingEmptyBraces";
		format_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces = section
				+ ".format.insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces";
		format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces = section
				+ ".format.insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces";
		format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets = section
				+ ".format.insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets";
		format_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis = section
				+ ".format.insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis";
		format_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces = section
				+ ".format.insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces";
		format_insertSpaceAfterSemicolonInForStatements = section + ".format.insertSpaceAfterSemicolonInForStatements";
		format_insertSpaceAfterTypeAssertion = section + ".format.insertSpaceAfterTypeAssertion";
		format_insertSpaceBeforeAndAfterBinaryOperators = section + ".format.insertSpaceBeforeAndAfterBinaryOperators";
		format_insertSpaceBeforeFunctionParenthesis = section + ".format.insertSpaceBeforeFunctionParenthesis";
		format_insertSpaceBeforeTypeAnnotation = section + ".format.insertSpaceBeforeTypeAnnotation";
		format_newLineCharacter = section + ".format.newLineCharacter";
		format_placeOpenBraceOnNewLineForControlBlocks = section + ".format.placeOpenBraceOnNewLineForControlBlocks";
		format_placeOpenBraceOnNewLineForFunctions = section + ".format.placeOpenBraceOnNewLineForFunctions";
		format_semicolons = section + ".format.semicolons";
		format_trimTrailingWhitespace = section + ".format.trimTrailingWhitespace";

		// Inlay Hint preferences keys
		inlayHints_includeInlayEnumMemberValueHints = section + ".inlayHints.includeInlayEnumMemberValueHints";
		inlayHints_includeInlayFunctionLikeReturnTypeHints = section + ".inlayHints.includeInlayFunctionLikeReturnTypeHints";
		inlayHints_includeInlayFunctionParameterTypeHints = section + ".inlayHints.includeInlayFunctionParameterTypeHints";
		inlayHints_includeInlayParameterNameHints = section + ".inlayHints.includeInlayParameterNameHints";
		inlayHints_includeInlayParameterNameHintsWhenArgumentMatchesName = section
				+ ".inlayHints.includeInlayParameterNameHintsWhenArgumentMatchesName";
		inlayHints_includeInlayPropertyDeclarationTypeHints = section + ".inlayHints.includeInlayPropertyDeclarationTypeHints";
		inlayHints_includeInlayVariableTypeHints = section + ".inlayHints.includeInlayVariableTypeHints";
		inlayHints_includeInlayVariableTypeHintsWhenTypeMatchesName = section
				+ ".inlayHints.includeInlayVariableTypeHintsWhenTypeMatchesName";

		// Code Lens preferences keys
		implementationsCodeLens_enabled = section + ".implementationsCodeLens.enabled";
		referencesCodeLens_enabled = section + ".referencesCodeLens.enabled";
		referencesCodeLens_showOnAllFunctions = section + ".referencesCodeLens.showOnAllFunctions";
	}

	public Settings getGlobalSettings() {
		Settings settings = new Settings(Activator.getDefault().getPreferenceStore());

		// Formatting preferences
		settings.fillAsInt(format_baseIndentSize);
		settings.fillAsBoolean(format_convertTabsToSpaces);
		settings.fillAsInt(format_indentSize);
		settings.fillAsInt(format_indentStyle);
		settings.fillSetting(format_tabSize,
				// Using: Preferences > General > Editors > Text Editors > Displayed tab width
				EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));

		settings.fillAsBoolean(format_insertSpaceAfterCommaDelimiter);
		settings.fillAsBoolean(format_insertSpaceAfterConstructor);
		settings.fillAsBoolean(format_insertSpaceAfterFunctionKeywordForAnonymousFunctions);
		settings.fillAsBoolean(format_insertSpaceAfterKeywordsInControlFlowStatements);
		settings.fillAsBoolean(format_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces);
		settings.fillAsBoolean(format_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces);
		settings.fillAsBoolean(format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces);
		settings.fillAsBoolean(format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets);
		settings.fillAsBoolean(format_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis);
		settings.fillAsBoolean(format_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces);
		settings.fillAsBoolean(format_insertSpaceAfterSemicolonInForStatements);
		settings.fillAsBoolean(format_insertSpaceAfterTypeAssertion);
		settings.fillAsBoolean(format_insertSpaceBeforeAndAfterBinaryOperators);
		settings.fillAsBoolean(format_insertSpaceBeforeFunctionParenthesis);
		settings.fillAsBoolean(format_insertSpaceBeforeTypeAnnotation);
		settings.fillAsString(format_newLineCharacter);
		settings.fillAsBoolean(format_placeOpenBraceOnNewLineForControlBlocks);
		settings.fillAsBoolean(format_placeOpenBraceOnNewLineForFunctions);
		settings.fillAsString(format_semicolons);
		settings.fillAsString(format_trimTrailingWhitespace);

		// Inlay Hint preferences
		settings.fillAsBoolean(inlayHints_includeInlayEnumMemberValueHints);
		settings.fillAsBoolean(inlayHints_includeInlayFunctionLikeReturnTypeHints);
		settings.fillAsBoolean(inlayHints_includeInlayFunctionParameterTypeHints);
		settings.fillAsString(inlayHints_includeInlayParameterNameHints);
		settings.fillAsBoolean(inlayHints_includeInlayParameterNameHintsWhenArgumentMatchesName);
		settings.fillAsBoolean(inlayHints_includeInlayPropertyDeclarationTypeHints);
		settings.fillAsBoolean(inlayHints_includeInlayVariableTypeHints);
		settings.fillAsBoolean(inlayHints_includeInlayVariableTypeHintsWhenTypeMatchesName);

		// Code Lens preferences
		settings.fillAsBoolean(implementationsCodeLens_enabled);
		settings.fillAsBoolean(referencesCodeLens_enabled);
		settings.fillAsBoolean(referencesCodeLens_showOnAllFunctions);
		return settings;
	}

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Formatting preferences
		// https://github.com/microsoft/TypeScript/blob/efca03ffed10dccede4fbc8dd8a624374e5424d9/src/services/types.ts#L1212
		store.setDefault(format_baseIndentSize, 0);
		store.setDefault(format_convertTabsToSpaces, true);
		store.setDefault(format_indentSize, 4);
		store.setDefault(format_indentStyle, "2"); // 0=None|1=Block|2=Smart
		store.setDefault(format_insertSpaceAfterCommaDelimiter, true);
		store.setDefault(format_insertSpaceAfterConstructor, false);
		store.setDefault(format_insertSpaceAfterFunctionKeywordForAnonymousFunctions, false);
		store.setDefault(format_insertSpaceAfterKeywordsInControlFlowStatements, true);
		store.setDefault(format_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces, false);
		store.setDefault(format_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces, false);
		store.setDefault(format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces, true);
		store.setDefault(format_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets, false);
		store.setDefault(format_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis, false);
		store.setDefault(format_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces, false);
		store.setDefault(format_insertSpaceAfterSemicolonInForStatements, false);
		store.setDefault(format_insertSpaceAfterTypeAssertion, false);
		store.setDefault(format_insertSpaceBeforeAndAfterBinaryOperators, true);
		store.setDefault(format_insertSpaceBeforeFunctionParenthesis, false);
		store.setDefault(format_insertSpaceBeforeTypeAnnotation, false);
		store.setDefault(format_newLineCharacter, System.lineSeparator());
		store.setDefault(format_placeOpenBraceOnNewLineForControlBlocks, false);
		store.setDefault(format_placeOpenBraceOnNewLineForFunctions, false);
		store.setDefault(format_semicolons, "ignore"); // ignore|insert|remove
		// store.setDefault(format_tabSize, 4);
		store.setDefault(format_trimTrailingWhitespace, true);

		// Inlay Hint preferences
		store.setDefault(inlayHints_includeInlayParameterNameHints, false);
		store.setDefault(inlayHints_includeInlayFunctionLikeReturnTypeHints, false);
		store.setDefault(inlayHints_includeInlayFunctionParameterTypeHints, false);
		store.setDefault(inlayHints_includeInlayParameterNameHints, "none"); // none|literals|all
		store.setDefault(inlayHints_includeInlayParameterNameHintsWhenArgumentMatchesName, false);
		store.setDefault(inlayHints_includeInlayPropertyDeclarationTypeHints, false);
		store.setDefault(inlayHints_includeInlayVariableTypeHints, false);
		store.setDefault(inlayHints_includeInlayVariableTypeHintsWhenTypeMatchesName, false);

		// Code Lens preferences
		store.setDefault(implementationsCodeLens_enabled, false);
		store.setDefault(referencesCodeLens_enabled, false);
		store.setDefault(referencesCodeLens_showOnAllFunctions, false);
	}

	/**
	 * Returns true if the given section matches JavaScript settings and false otherwise.
	 *
	 * @param section the section to check.
	 *
	 * @return true if the given section matches JavaScript settings and false otherwise.
	 */
	public boolean isMatchTypeScriptSection(String section) {
		return isMatchSection(section, this.section);
	}
}
