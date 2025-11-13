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
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript;

import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSLanguagePreferences.JS;

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
 */
public class JavaScriptInlayHintPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public JavaScriptInlayHintPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.InlayHintPreferencePage_showInlayHintsFor_label);

		addField(new BooleanFieldEditor(JS.inlayHints_includeInlayEnumMemberValueHints,
				Messages.InlayHintPreferencePage_includeInlayEnumMemberValueHints, parent));
		addField(new BooleanFieldEditor(JS.inlayHints_includeInlayFunctionLikeReturnTypeHints,
				Messages.InlayHintPreferencePage_includeInlayFunctionLikeReturnTypeHints, parent));
		addField(new BooleanFieldEditor(JS.inlayHints_includeInlayFunctionParameterTypeHints,
				Messages.InlayHintPreferencePage_includeInlayFunctionParameterTypeHints, parent));
		addField(new BooleanFieldEditor(JS.inlayHints_includeInlayPropertyDeclarationTypeHints,
				Messages.InlayHintPreferencePage_includeInlayPropertyDeclarationTypeHints, parent));

		addField(new BooleanFieldEditor(JS.inlayHints_includeInlayVariableTypeHints,
				Messages.InlayHintPreferencePage_includeInlayVariableTypeHints, parent));
		addField(new IndentedBooleanFieldEditor(JS.inlayHints_includeInlayVariableTypeHintsWhenTypeMatchesName,
				Messages.InlayHintPreferencePage_includeInlayVariableTypeHintsWhenTypeMatchesName, parent));

		addField(new ComboFieldEditor(JS.inlayHints_includeInlayParameterNameHints,
				Messages.InlayHintPreferencePage_includeInlayParameterNameHints,
				new String[][] {
					{ Messages.InlayHintPreferencePage_includeInlayParameterNameHints_none, "none" },
					{ Messages.InlayHintPreferencePage_includeInlayParameterNameHints_literals,
						"literals" },
					{ Messages.InlayHintPreferencePage_includeInlayParameterNameHints_all, "all" } },
				parent));
		addField(new IndentedBooleanFieldEditor(JS.inlayHints_includeInlayParameterNameHintsWhenArgumentMatchesName,
				Messages.InlayHintPreferencePage_includeInlayParameterNameHintsWhenArgumentMatchesName, parent));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
