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

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_NO_GRAMMAR;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * XML validation preference page.
 *
 */
public class XMLValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLValidationPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(XML_PREFERENCES_VALIDATION_ENABLED.preferenceId,
				Messages.XMLValidationPreferencePage_validation_enabled, getFieldEditorParent()));
		addField(new ComboFieldEditor(XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.preferenceId,
				Messages.XMLValidationPreferencePage_validation_namespaces_enabled,
				new String[][] { { "Always", "always" }, { "Never", "never" },
						{ "On namespace encountered", "onNamespaceEncountered" } },
				getFieldEditorParent()));
		addField(new ComboFieldEditor(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.preferenceId,
				Messages.XMLValidationPreferencePage_validation_schema_enabled,
				new String[][] { { "Always", "always" }, { "Never", "never" }, { "In valid schema", "onValidSchema" } },
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.preferenceId,
				Messages.XMLValidationPreferencePage_validation_disallowDocTypeDecl, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.preferenceId,
				Messages.XMLValidationPreferencePage_validation_resolveExternalEntities, getFieldEditorParent()));
		addField(new ComboFieldEditor(XML_PREFERENCES_VALIDATION_NO_GRAMMAR.preferenceId,
				Messages.XMLValidationPreferencePage_validation_noGrammar,
				new String[][] { { "Ignore", "ignore" }, { "Hint", "hint" }, { "Info", "info" },
						{ "Warning", "warning" }, { "Error", "error" } },
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
