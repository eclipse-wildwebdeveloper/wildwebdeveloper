/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_CODEMININGS_ENABLED;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * XML code lens preference page.
 *
 */
public class XMLCodeMiningsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLCodeMiningsPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(XML_PREFERENCES_CODEMININGS_ENABLED.preferenceId,
				Messages.XMLCodeMiningsPreferencePage_codeminings_enabled, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
