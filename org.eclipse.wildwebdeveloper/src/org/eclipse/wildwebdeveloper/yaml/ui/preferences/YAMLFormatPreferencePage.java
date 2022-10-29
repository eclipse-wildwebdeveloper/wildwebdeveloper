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

import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_FORMAT_BRACKETSPACING;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_FORMAT_ENABLE;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_FORMAT_PRINTWIDTH;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_FORMAT_PROSEWRAP;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_FORMAT_SINGLEQUOTE;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.yaml.ui.Messages;

/**
 * HTML format preference page.
 *
 */
public class YAMLFormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public YAMLFormatPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(YAML_PREFERENCES_FORMAT_ENABLE, Messages.YAMLFormatPreferencePage_format_enable,
				getFieldEditorParent()));
		addField(new StringFieldEditor(YAML_PREFERENCES_FORMAT_SINGLEQUOTE,
				Messages.YAMLFormatPreferencePage_format_singleQuote, getFieldEditorParent()));
		addField(new StringFieldEditor(YAML_PREFERENCES_FORMAT_BRACKETSPACING,
				Messages.YAMLFormatPreferencePage_format_bracketSpacing, getFieldEditorParent()));
		addField(new ComboFieldEditor(YAML_PREFERENCES_FORMAT_PROSEWRAP,
				Messages.YAMLFormatPreferencePage_format_proseWrap,
				new String[][] { { Messages.YAMLFormatPreferencePage_format_proseWrap_preserve, "preserve" },
						{ Messages.YAMLFormatPreferencePage_format_proseWrap_never, "never" },
						{ Messages.YAMLFormatPreferencePage_format_proseWrap_always, "always" } },
				getFieldEditorParent()));

		addField(new IntegerFieldEditor(YAML_PREFERENCES_FORMAT_PRINTWIDTH,
				Messages.YAMLFormatPreferencePage_format_printWidth, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
