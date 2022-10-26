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
package org.eclipse.wildwebdeveloper.css.ui.preferences;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_FORMAT_BRACE_STYLE;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.css.ui.Messages;

/**
 * CSS format preference page.
 *
 */
public class CSSFormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CSSFormatPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_SELECTORS,
				Messages.CSSFormatPreferencePage_format_newlineBetweenSelectors, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CSS_PREFERENCES_FORMAT_NEW_LINE_BETWEEN_RULES,
				Messages.CSSFormatPreferencePage_format_newlineBetweenRules, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CSS_PREFERENCES_FORMAT_SPACE_AROUND_SELECTOR_SEPARATOR,
				Messages.CSSFormatPreferencePage_format_spaceAroundSelectorSeparator, getFieldEditorParent()));
		addField(new ComboFieldEditor(CSS_PREFERENCES_FORMAT_BRACE_STYLE,
				Messages.CSSFormatPreferencePage_format_braceStyle,
				new String[][] { { Messages.CSSFormatPreferencePage_format_braceStyle_collapse, "collapse" },
						{ Messages.CSSFormatPreferencePage_format_braceStyle_expand, "expand" } },
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(CSS_PREFERENCES_FORMAT_PRESERVE_NEW_LINES,
				Messages.CSSFormatPreferencePage_format_preserveNewLines, getFieldEditorParent()));
//		addField(new StringFieldEditor(HTML_PREFERENCES_FORMAT_EXTRA_LINERS,
//				Messages.CSSFormatPreferencePage_format_maxPreserveNewLines, getFieldEditorParent()));

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
