/*******************************************************************************
 * Copyright (c) 2022-2023 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo ZERR (Red Hat Inc.) - initial implementation
 * Pierre-Yves Bigourdan - Allow using TypeScript version specified by project
 * Pierre-Yves Bigourdan - Allow configuring directory of ESLint package
 * Sebastian Thomschke (Vegard IT GmbH) - add CodeLensPreferencePage, FormatterPreferencePage keys
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui;

import org.eclipse.osgi.util.NLS;

/**
 * JS/TS messages keys.
 */
public class Messages extends NLS {

	public static String JSTSPreferencePage_typeScriptVersion;
	public static String JSTSPreferencePage_typeScriptVersion_eclipse;
	public static String JSTSPreferencePage_typeScriptVersion_project;

	public static String JSTSPreferencePage_eslintNodePath;

	// JavaScript / TypeScript Inlay Hints preference page
	public static String InlayHintPreferencePage_showInlayHintsFor_label;
	public static String InlayHintPreferencePage_includeInlayEnumMemberValueHints;
	public static String InlayHintPreferencePage_includeInlayFunctionLikeReturnTypeHints;
	public static String InlayHintPreferencePage_includeInlayFunctionParameterTypeHints;
	public static String InlayHintPreferencePage_includeInlayParameterNameHints;
	public static String InlayHintPreferencePage_includeInlayParameterNameHints_none;
	public static String InlayHintPreferencePage_includeInlayParameterNameHints_literals;
	public static String InlayHintPreferencePage_includeInlayParameterNameHints_all;
	public static String InlayHintPreferencePage_includeInlayParameterNameHintsWhenArgumentMatchesName;
	public static String InlayHintPreferencePage_includeInlayPropertyDeclarationTypeHints;
	public static String InlayHintPreferencePage_includeInlayVariableTypeHints;
	public static String InlayHintPreferencePage_includeInlayVariableTypeHintsWhenTypeMatchesName;

	// JavaScript / TypeScript Code Lens preference page
	public static String CodeLensPreferencePage_showCodeLensFor_label;
	public static String CodeLensPreferencePage_implementationsCodeLens_enabled;
	public static String CodeLensPreferencePage_referencesCodeLens_enabled;
	public static String CodeLensPreferencePage_referencesCodeLens_showOnAllFunctions;

	public static String FormatterPreferencePage_baseIndentSize;
	public static String FormatterPreferencePage_convertTabsToSpaces;
	public static String FormatterPreferencePage_indentSize;
	public static String FormatterPreferencePage_indentStyle;
	public static String FormatterPreferencePage_insertSpaceAfterCommaDelimiter;
	public static String FormatterPreferencePage_insertSpaceAfterConstructor;
	public static String FormatterPreferencePage_insertSpaceAfterFunctionKeywordForAnonymousFunctions;
	public static String FormatterPreferencePage_insertSpaceAfterKeywordsInControlFlowStatements;
	public static String FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingEmptyBraces;
	public static String FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingJsxExpressionBraces;
	public static String FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingNonemptyBraces;
	public static String FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets;
	public static String FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis;
	public static String FormatterPreferencePage_insertSpaceAfterOpeningAndBeforeClosingTemplateStringBraces;
	public static String FormatterPreferencePage_insertSpaceAfterSemicolonInForStatements;
	public static String FormatterPreferencePage_insertSpaceAfterTypeAssertion;
	public static String FormatterPreferencePage_insertSpaceBeforeAndAfterBinaryOperators;
	public static String FormatterPreferencePage_insertSpaceBeforeFunctionParenthesis;
	public static String FormatterPreferencePage_insertSpaceBeforeTypeAnnotation;
	public static String FormatterPreferencePage_newLineCharacter;
	public static String FormatterPreferencePage_placeOpenBraceOnNewLineForControlBlocks;
	public static String FormatterPreferencePage_placeOpenBraceOnNewLineForFunctions;
	public static String FormatterPreferencePage_semicolons;
	public static String FormatterPreferencePage_trimTrailingWhitespace;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.jsts.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
