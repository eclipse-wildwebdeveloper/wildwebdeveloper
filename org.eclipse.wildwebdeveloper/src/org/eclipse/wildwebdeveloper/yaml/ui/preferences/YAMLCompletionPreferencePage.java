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
package org.eclipse.wildwebdeveloper.yaml.ui.preferences;

import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_COMPLETION;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_DISABLEDEFAULTPROPERTIES;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_SUGGEST_PARENTSKELETONSELECTEDFIRST;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.yaml.ui.Messages;

/**
 * YAML completion preference page.
 *
 */
public class YAMLCompletionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public YAMLCompletionPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(YAML_PREFERENCES_COMPLETION, Messages.YAMLCompletionPreferencePage_completion,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(YAML_PREFERENCES_SUGGEST_PARENTSKELETONSELECTEDFIRST,
				Messages.YAMLCompletionPreferencePage_suggest_parentSkeletonSelectedFirst, getFieldEditorParent()));
		addField(new BooleanFieldEditor(YAML_PREFERENCES_DISABLEDEFAULTPROPERTIES,
				Messages.YAMLCompletionPreferencePage_disableDefaultProperties, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
