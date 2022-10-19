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
package org.eclipse.wildwebdeveloper.html.ui.preferences;

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_VALIDATE_SCRIPTS;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_VALIDATE_STYLES;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.html.ui.Messages;

/**
 * HTML validation preference page.
 *
 */
public class HTMLValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public HTMLValidationPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(HTML_PREFERENCES_VALIDATE_SCRIPTS,
				Messages.HTMLValidationPreferencePage_validate_scripts, getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_VALIDATE_STYLES,
				Messages.HTMLValidationPreferencePage_validate_styles, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
