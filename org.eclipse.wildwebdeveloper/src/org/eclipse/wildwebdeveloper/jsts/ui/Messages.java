/*******************************************************************************
 * Copyright (c) 2022-2023 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *  Pierre-Yves Bigourdan - Allow using TypeScript version specified by project
 *  Pierre-Yves Bigourdan - Allow configuring directory of ESLint package
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui;

import org.eclipse.osgi.util.NLS;

/**
 * JS/TS messages keys.
 *
 */
public class Messages extends NLS {

	public static String JSTSPreferencePage_typeScriptVersion;
	public static String JSTSPreferencePage_typeScriptVersion_eclipse;
	public static String JSTSPreferencePage_typeScriptVersion_project;

	public static String JSTSPreferencePage_eslintNodePath;

	// --------- TypeScript Inlay Hints preference page
	public static String TypeScriptInlayHintPreferencePage_showInlayHintsFor_label;
	public static String TypeScriptInlayHintPreferencePage_includeInlayEnumMemberValueHints;
	public static String TypeScriptInlayHintPreferencePage_includeInlayFunctionLikeReturnTypeHints;
	public static String TypeScriptInlayHintPreferencePage_includeInlayFunctionParameterTypeHints;
	public static String TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints;
	public static String TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints_none;
	public static String TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints_literals;
	public static String TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints_all;
	public static String TypeScriptInlayHintPreferencePage_includeInlayParameterNameHintsWhenArgumentMatchesName;
	public static String TypeScriptInlayHintPreferencePage_includeInlayPropertyDeclarationTypeHints;
	public static String TypeScriptInlayHintPreferencePage_includeInlayVariableTypeHints;
	public static String TypeScriptInlayHintPreferencePage_includeInlayVariableTypeHintsWhenTypeMatchesName;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.jsts.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
