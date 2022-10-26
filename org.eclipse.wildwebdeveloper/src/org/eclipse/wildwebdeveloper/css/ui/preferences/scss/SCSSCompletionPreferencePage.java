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
package org.eclipse.wildwebdeveloper.css.ui.preferences.scss;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.css.ui.Messages;

/**
 * SCSS completion preference page.
 *
 */
public class SCSSCompletionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SCSSCompletionPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION,
				Messages.SCSSCompletionPreferencePage_completion_triggerPropertyValueCompletion,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON,
				Messages.SCSSCompletionPreferencePage_completion_completePropertyWithSemicolon, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
