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

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_CONTENT_UNFORMATTED;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_EXTRA_LINERS;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_INDENT_HANDLE_BARS;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_INDENT_INNER_HTML;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_PRESERVE_NEW_LINES;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_TEMPLATING;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_UNFORMATTED;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_UNFORMATTED_CONTENT_DELIMITER;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_WRAP_ATTRIBUTES;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceServerConstants.HTML_PREFERENCES_FORMAT_WRAP_LINE_LENGTH;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.html.ui.Messages;

/**
 * HTML format preference page.
 *
 */
public class HTMLFormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public HTMLFormatPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new IntegerFieldEditor(HTML_PREFERENCES_FORMAT_WRAP_LINE_LENGTH,
				Messages.HTMLFormatPreferencePage_format_wrapLineLength, getFieldEditorParent()));
		addField(new StringFieldEditor(HTML_PREFERENCES_FORMAT_UNFORMATTED,
				Messages.HTMLFormatPreferencePage_format_unformatted, getFieldEditorParent()));
		addField(new StringFieldEditor(HTML_PREFERENCES_FORMAT_CONTENT_UNFORMATTED,
				Messages.HTMLFormatPreferencePage_format_contentUnformatted, getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_FORMAT_INDENT_INNER_HTML,
				Messages.HTMLFormatPreferencePage_format_indentInnerHtml, getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_FORMAT_PRESERVE_NEW_LINES,
				Messages.HTMLFormatPreferencePage_format_preserveNewLines, getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_FORMAT_INDENT_HANDLE_BARS,
				Messages.HTMLFormatPreferencePage_format_indentHandlebars, getFieldEditorParent()));
		addField(new StringFieldEditor(HTML_PREFERENCES_FORMAT_EXTRA_LINERS,
				Messages.HTMLFormatPreferencePage_format_extraLiners, getFieldEditorParent()));

		addField(new ComboFieldEditor(HTML_PREFERENCES_FORMAT_WRAP_ATTRIBUTES,
				Messages.HTMLFormatPreferencePage_format_wrapAttributes,
				new String[][] { { Messages.HTMLFormatPreferencePage_format_wrapAttributes_auto, "auto" },
						{ Messages.HTMLFormatPreferencePage_format_wrapAttributes_force, "force" },
						{ Messages.HTMLFormatPreferencePage_format_wrapAttributes_forcealign, "force-aligned" },
						{ Messages.HTMLFormatPreferencePage_format_wrapAttributes_forcemultiline,
								"force-expand-multiline" },
						{ Messages.HTMLFormatPreferencePage_format_wrapAttributes_alignedmultiple, "aligned-multiple" },
						{ Messages.HTMLFormatPreferencePage_format_wrapAttributes_preserve, "preserve" },
						{ Messages.HTMLFormatPreferencePage_format_wrapAttributes_preservealigned,
								"preserve-aligned" } },
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(HTML_PREFERENCES_FORMAT_TEMPLATING,
				Messages.HTMLFormatPreferencePage_format_templating, getFieldEditorParent()));
		addField(new StringFieldEditor(HTML_PREFERENCES_FORMAT_UNFORMATTED_CONTENT_DELIMITER,
				Messages.HTMLFormatPreferencePage_format_unformattedContentDelimiter, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
