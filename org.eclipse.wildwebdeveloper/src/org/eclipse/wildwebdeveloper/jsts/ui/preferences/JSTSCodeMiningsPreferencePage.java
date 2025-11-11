/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.jsts.ui.Messages;

/**
 * JavaScript / TypeScript Code Lens preference page.
 */
public abstract class JSTSCodeMiningsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private final JSTSLanguagePreferences prefs;

	protected JSTSCodeMiningsPreferencePage(JSTSLanguagePreferences prefs) {
		super(GRID);
		this.prefs = prefs;
	}

	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.CodeMiningsPreferencePage_showCodeMiningsFor_label);

		addField(new BooleanFieldEditor(prefs.implementationsCodeLens_enabled,
				Messages.CodeMiningsPreferencePage_implementationsCodeMinings_enabled, parent));
		addField(new BooleanFieldEditor(prefs.referencesCodeLens_enabled,
				Messages.CodeMiningsPreferencePage_referencesCodeMinings_enabled, parent));
		addField(new BooleanFieldEditor(prefs.referencesCodeLens_showOnAllFunctions,
				Messages.CodeMiningsPreferencePage_referencesCodeMinings_showOnAllFunctions, parent));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
