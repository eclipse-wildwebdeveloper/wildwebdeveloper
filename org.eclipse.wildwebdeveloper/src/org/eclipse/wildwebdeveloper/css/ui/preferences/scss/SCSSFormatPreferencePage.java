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
package org.eclipse.wildwebdeveloper.css.ui.preferences.scss;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_FORMAT_BRACE_STYLE;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.css.ui.Messages;

/**
 * SCSS format preference page.
 *
 */
public class SCSSFormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SCSSFormatPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS,
				Messages.SCSSFormatPreferencePage_format_newlineBetweenSelectors, getFieldEditorParent()));
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES,
				Messages.SCSSFormatPreferencePage_format_newlineBetweenRules, getFieldEditorParent()));
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR,
				Messages.SCSSFormatPreferencePage_format_spaceAroundSelectorSeparator, getFieldEditorParent()));
		addField(new ComboFieldEditor(SCSS_PREFERENCES_FORMAT_BRACE_STYLE,
				Messages.SCSSFormatPreferencePage_format_braceStyle,
				new String[][] { { Messages.SCSSFormatPreferencePage_format_braceStyle_collapse, "collapse" },
						{ Messages.SCSSFormatPreferencePage_format_braceStyle_expand, "expand" } },
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES,
				Messages.SCSSFormatPreferencePage_format_preserveNewLines, getFieldEditorParent()));
//		addField(new StringFieldEditor(HTML_PREFERENCES_FORMAT_EXTRA_LINERS,
//				Messages.SCSSFormatPreferencePage_format_maxPreserveNewLines, getFieldEditorParent()));

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
