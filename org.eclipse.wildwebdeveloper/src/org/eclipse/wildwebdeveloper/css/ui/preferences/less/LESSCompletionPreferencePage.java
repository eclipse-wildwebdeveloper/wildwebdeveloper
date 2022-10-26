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
package org.eclipse.wildwebdeveloper.css.ui.preferences.less;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.css.ui.Messages;

/**
 * LESS completion preference page.
 *
 */
public class LESSCompletionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LESSCompletionPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(LESS_PREFERENCES_COMPLETION_TRIGGER_PROPERTY_VALUE_COMPLETION,
				Messages.LESSCompletionPreferencePage_completion_triggerPropertyValueCompletion,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(LESS_PREFERENCES_COMPLETION_COMPLETE_PROPERTY_WITH_SEMICOLON,
				Messages.LESSCompletionPreferencePage_completion_completePropertyWithSemicolon, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
