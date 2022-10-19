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
package org.eclipse.wildwebdeveloper.html.ui.preferences;

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CLOSING_TAGS;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CREATE_QUOTES;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.html.ui.Messages;

/**
 * HTML main preference page.
 *
 */
public class HTMLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public HTMLPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(HTML_PREFERENCES_AUTO_CLOSING_TAGS, Messages.HTMLPreferencePage_autoClosingTags,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_AUTO_CREATE_QUOTES,
				Messages.HTMLPreferencePage_autoCreateQuotes, getFieldEditorParent()));

	}
}