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
package org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript;

import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_ENUM_MEMBER_VALUE_HINTS;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_LIKE_RETURN_TYPE_HINTS;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_PARAMETER_TYPE_HINTS;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS_WHEN_ARGUMENT_MATCHES_NAME;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PROPERTY_DECLARATION_TYPE_HINTS;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_VARIABLE_TYPE_HINTS;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript.JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_VARIABLE_TYPE_HINTS_WHEN_TYPE_MATCHES_NAME;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.jsts.ui.Messages;
import org.eclipse.wildwebdeveloper.ui.preferences.IndentedBooleanFieldEditor;

/**
 * JavaScript Inlay Hint preference page.
 *
 */
public class JavaScriptInlayHintPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public JavaScriptInlayHintPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.TypeScriptInlayHintPreferencePage_showInlayHintsFor_label);

		addField(new BooleanFieldEditor(JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_ENUM_MEMBER_VALUE_HINTS,
				Messages.TypeScriptInlayHintPreferencePage_includeInlayEnumMemberValueHints, parent));
		addField(
				new BooleanFieldEditor(JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_LIKE_RETURN_TYPE_HINTS,
						Messages.TypeScriptInlayHintPreferencePage_includeInlayFunctionLikeReturnTypeHints, parent));
		addField(new BooleanFieldEditor(JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_PARAMETER_TYPE_HINTS,
				Messages.TypeScriptInlayHintPreferencePage_includeInlayFunctionParameterTypeHints, parent));
		addField(
				new BooleanFieldEditor(JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PROPERTY_DECLARATION_TYPE_HINTS,
						Messages.TypeScriptInlayHintPreferencePage_includeInlayPropertyDeclarationTypeHints, parent));

		addField(new BooleanFieldEditor(JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_VARIABLE_TYPE_HINTS,
				Messages.TypeScriptInlayHintPreferencePage_includeInlayVariableTypeHints, parent));
		addField(new IndentedBooleanFieldEditor(
				JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_VARIABLE_TYPE_HINTS_WHEN_TYPE_MATCHES_NAME,
				Messages.TypeScriptInlayHintPreferencePage_includeInlayVariableTypeHintsWhenTypeMatchesName, parent));

		addField(new ComboFieldEditor(JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS,
				Messages.TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints,
				new String[][] {
						{ Messages.TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints_none, "none" },
						{ Messages.TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints_literals,
								"literals" },
						{ Messages.TypeScriptInlayHintPreferencePage_includeInlayParameterNameHints_all, "all" } },
				parent));
		addField(new IndentedBooleanFieldEditor(
				JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS_WHEN_ARGUMENT_MATCHES_NAME,
				Messages.TypeScriptInlayHintPreferencePage_includeInlayParameterNameHintsWhenArgumentMatchesName,
				parent));
	}


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
