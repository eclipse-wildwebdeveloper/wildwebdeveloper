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

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_COMPLETION_ATTRIBUTE_DEFAULT_VALUE;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_SUGGEST_HTML5;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.html.ui.Messages;

/**
 * HTML completion preference page.
 *
 */
public class HTMLCompletionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public HTMLCompletionPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new ComboFieldEditor(HTML_PREFERENCES_COMPLETION_ATTRIBUTE_DEFAULT_VALUE,
				Messages.HTMLCompletionPreferencePage_completion_attributeDefaultValue,
				new String[][] {
						{ Messages.HTMLCompletionPreferencePage_completion_attributeDefaultValue_doublequotes,
								"doublequotes" },
						{ Messages.HTMLCompletionPreferencePage_completion_attributeDefaultValue_singlequotes,
								"singlequotes" },
						{ Messages.HTMLCompletionPreferencePage_completion_attributeDefaultValue_empty, "empty" } },
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_SUGGEST_HTML5,
				Messages.HTMLCompletionPreferencePage_suggest_html5, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
