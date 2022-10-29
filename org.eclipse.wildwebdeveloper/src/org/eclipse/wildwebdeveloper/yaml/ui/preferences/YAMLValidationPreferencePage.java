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

import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_CUSTOMTAGS;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_DISABLEADDITIONALPROPERTIES;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_STYLE_FLOWMAPPING;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_STYLE_FLOWSEQUENCE;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_VALIDATE;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_YAMLVERSION;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.yaml.ui.Messages;

/**
 * YAML validation preference page.
 *
 */
public class YAMLValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public YAMLValidationPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(YAML_PREFERENCES_VALIDATE, Messages.YAMLValidationPreferencePage_validate,
				getFieldEditorParent()));
		addField(new ComboFieldEditor(YAML_PREFERENCES_YAMLVERSION, Messages.YAMLValidationPreferencePage_yamlVersion,
				new String[][] { { "1.1", "1.1" }, { "1.2", "1.2" } }, getFieldEditorParent()));
		addField(new CustomTagsFieldEditor(YAML_PREFERENCES_CUSTOMTAGS,
				Messages.YAMLValidationPreferencePage_customTags, getFieldEditorParent()));
		addField(new BooleanFieldEditor(YAML_PREFERENCES_DISABLEADDITIONALPROPERTIES,
				Messages.YAMLValidationPreferencePage_disableAdditionalProperties, getFieldEditorParent()));
		addField(new ComboFieldEditor(YAML_PREFERENCES_STYLE_FLOWMAPPING,
				Messages.YAMLValidationPreferencePage_style_flowMapping,
				new String[][] { { "allow", "allow" }, { "forbid", "forbid" } }, getFieldEditorParent()));
		addField(new ComboFieldEditor(YAML_PREFERENCES_STYLE_FLOWSEQUENCE,
				Messages.YAMLValidationPreferencePage_style_flowSequence,
				new String[][] { { "allow", "allow" }, { "forbid", "forbid" } }, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
