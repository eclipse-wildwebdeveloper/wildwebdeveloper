/*******************************************************************************
 * Copyright (c) 2021, 2022 Red Hat Inc. and others.
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

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_NO_GRAMMAR;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
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
				new String[][] { 
					{ Action.removeMnemonics(IDEWorkbenchMessages.Always), "always" }, 
					{ Action.removeMnemonics(IDEWorkbenchMessages.Never), "never" },
					{ Messages.XMLValidationPreferencePage_validation_namespace_option_onNamespaceEncountered, "onNamespaceEncountered" } 
				},
				getFieldEditorParent()));
		addField(new ComboFieldEditor(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.preferenceId,
				Messages.XMLValidationPreferencePage_validation_schema_enabled,
				new String[][] { 
					{ Action.removeMnemonics(IDEWorkbenchMessages.Always), "always" }, 
					{ Action.removeMnemonics(IDEWorkbenchMessages.Never), "never" }, 
					{ Messages.XMLValidationPreferencePage_validation_schema_option_onValidSchema, "onValidSchema" } 
				},
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.preferenceId,
				Messages.XMLValidationPreferencePage_validation_disallowDocTypeDecl, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.preferenceId,
				Messages.XMLValidationPreferencePage_validation_resolveExternalEntities, getFieldEditorParent()));
		addField(new ComboFieldEditor(XML_PREFERENCES_VALIDATION_NO_GRAMMAR.preferenceId,
				Messages.XMLValidationPreferencePage_validation_noGrammar,
				new String[][] { 
					{ Action.removeMnemonics(IDialogConstants.IGNORE_LABEL), "ignore" },
					{ Messages.XMLValidationPreferencePage_validation_noGrammar_option_hint, "hint" }, 
					{ JFaceResources.getString("info"), "info" },
					{ JFaceResources.getString("warning"), "warning" }, 
					{ JFaceResources.getString("error"), "error" } 
				},
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
