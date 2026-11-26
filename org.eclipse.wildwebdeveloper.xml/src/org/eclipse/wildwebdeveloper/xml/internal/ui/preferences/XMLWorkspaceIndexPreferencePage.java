/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_EXCLUDE_DIRECTORIES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_HEAD_LIMITS;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * Preference page for the XML workspace Bloom index settings.
 */
public class XMLWorkspaceIndexPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLWorkspaceIndexPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.XMLWorkspaceIndexPreferencePage_description);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				XML_PREFERENCES_WORKSPACE_INDEX_ENABLED.preferenceId,
				Messages.XMLWorkspaceIndexPreferencePage_enabled,
				getFieldEditorParent()));

		addField(new ListEditor(
				XML_PREFERENCES_WORKSPACE_INDEX_EXCLUDE_DIRECTORIES.preferenceId,
				Messages.XMLWorkspaceIndexPreferencePage_excludeDirectories,
				getFieldEditorParent()) {
			@Override
			protected String createList(String[] items) {
				return String.join(",", items);
			}

			@Override
			protected String[] parseString(String stringList) {
				if (stringList == null || stringList.isBlank()) {
					return new String[0];
				}
				return stringList.split(",");
			}

			@Override
			protected String getNewInputObject() {
				InputDialog dlg = new InputDialog(
						getFieldEditorParent().getShell(),
						Messages.XMLWorkspaceIndexPreferencePage_excludeDirectories_inputTitle,
						Messages.XMLWorkspaceIndexPreferencePage_excludeDirectories_inputMessage,
						"", null);
				if (dlg.open() == Window.OK) {
					String val = dlg.getValue().trim();
					return val.isEmpty() ? null : val;
				}
				return null;
			}
		});

		addField(new StringFieldEditor(
				XML_PREFERENCES_WORKSPACE_INDEX_HEAD_LIMITS.preferenceId,
				Messages.XMLWorkspaceIndexPreferencePage_headLimits,
				getFieldEditorParent()) {
			{
				setEmptyStringAllowed(true);
			}
		});
	}
}
