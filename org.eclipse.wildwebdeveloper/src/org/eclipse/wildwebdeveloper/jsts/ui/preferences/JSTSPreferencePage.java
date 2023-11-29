/*******************************************************************************
 * Copyright (c) 2022-2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *  Pierre-Yves Bigourdan - Allow configuring directory of ESLint package
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences;

import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSPreferenceServerConstants.ESLINT_PREFERENCES_NODE_PATH;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSPreferenceServerConstants.TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSPreferenceServerConstants.TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_ECLIPSE;
import static org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSPreferenceServerConstants.TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_PROJECT;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.jsts.ui.Messages;

/**
 * JS/TS main preference page.
 *
 */
public class JSTSPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public JSTSPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new ComboFieldEditor(TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION,
				Messages.JSTSPreferencePage_typeScriptVersion,
				new String[][] {
						{ Messages.JSTSPreferencePage_typeScriptVersion_eclipse,
								TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_ECLIPSE },
						{ Messages.JSTSPreferencePage_typeScriptVersion_project,
								TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_PROJECT } },
				parent));
		addField(new StringFieldEditor(ESLINT_PREFERENCES_NODE_PATH, Messages.JSTSPreferencePage_eslintNodePath, parent));
	}
}