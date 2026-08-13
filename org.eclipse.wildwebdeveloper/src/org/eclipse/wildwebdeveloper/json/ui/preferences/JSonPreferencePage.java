/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Lars Vogel (Vogella GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json.ui.preferences;

import static org.eclipse.wildwebdeveloper.json.ui.preferences.JSonPreferenceServerConstants.JSON_PREFERENCES_MAXITEMSCOMPUTED;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.json.ui.Messages;

/**
 * JSON main preference page.
 *
 */
public class JSonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public JSonPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		IntegerFieldEditor maxItemsComputed = new IntegerFieldEditor(JSON_PREFERENCES_MAXITEMSCOMPUTED,
				Messages.JSonPreferencePage_maxItemsComputed, getFieldEditorParent());
		maxItemsComputed.setValidRange(0, Integer.MAX_VALUE);
		addField(maxItemsComputed);
	}

}
