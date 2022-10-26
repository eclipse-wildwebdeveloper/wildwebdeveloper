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
package org.eclipse.wildwebdeveloper.css.ui.preferences.less;

import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_BOXMODEL;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_DUPLICATEPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_EMPTYRULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_FLOAT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_FONTFACEPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_HEXCOLORLENGTH;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_IDSELECTOR;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_IEHACK;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_IMPORTANT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_IMPORTSTATEMENT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_UNIVERSALSELECTOR;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_UNKNOWNATRULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_UNKNOWNPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_VALIDPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_VENDOR_PREFIX;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_LINT_ZEROUNITS;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants.LESS_PREFERENCES_VALIDATE;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.css.ui.Messages;
import org.eclipse.wildwebdeveloper.css.ui.preferences.ValidPropertiesFieldEditor;

/**
 * LESS validation preference page.
 *
 */
public class LESSValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LESSValidationPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(LESS_PREFERENCES_VALIDATE, Messages.LESSValidationPreferencePage_validate,
				getFieldEditorParent()));
		addSeverityField(LESS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES,
				Messages.LESSValidationPreferencePage_lint_compatibleVendorPrefixes);
		addSeverityField(LESS_PREFERENCES_LINT_VENDOR_PREFIX, Messages.LESSValidationPreferencePage_lint_vendorPrefix);
		addSeverityField(LESS_PREFERENCES_LINT_DUPLICATEPROPERTIES,
				Messages.LESSValidationPreferencePage_lint_duplicateProperties);
		addSeverityField(LESS_PREFERENCES_LINT_EMPTYRULES, Messages.LESSValidationPreferencePage_lint_emptyRules);
		addSeverityField(LESS_PREFERENCES_LINT_IMPORTSTATEMENT,
				Messages.LESSValidationPreferencePage_lint_importStatement);
		addSeverityField(LESS_PREFERENCES_LINT_BOXMODEL, Messages.LESSValidationPreferencePage_lint_boxModel);
		addSeverityField(LESS_PREFERENCES_LINT_UNIVERSALSELECTOR,
				Messages.LESSValidationPreferencePage_lint_universalSelector);
		addSeverityField(LESS_PREFERENCES_LINT_ZEROUNITS, Messages.LESSValidationPreferencePage_lint_zeroUnits);
		addSeverityField(LESS_PREFERENCES_LINT_FONTFACEPROPERTIES,
				Messages.LESSValidationPreferencePage_lint_fontFaceProperties);
		addSeverityField(LESS_PREFERENCES_LINT_HEXCOLORLENGTH, Messages.LESSValidationPreferencePage_lint_hexColorLength);
		addSeverityField(LESS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION,
				Messages.LESSValidationPreferencePage_lint_argumentsInColorFunction);
		addSeverityField(LESS_PREFERENCES_LINT_UNKNOWNPROPERTIES,
				Messages.LESSValidationPreferencePage_lint_unknownProperties);
		addField(new ValidPropertiesFieldEditor(LESS_PREFERENCES_LINT_VALIDPROPERTIES,
				Messages.LESSValidationPreferencePage_lint_validProperties, getFieldEditorParent()));
		addSeverityField(LESS_PREFERENCES_LINT_IEHACK, Messages.LESSValidationPreferencePage_lint_ieHack);
		addSeverityField(LESS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES,
				Messages.LESSValidationPreferencePage_lint_unknownVendorSpecificProperties);
		addSeverityField(LESS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY,
				Messages.LESSValidationPreferencePage_lint_propertyIgnoredDueToDisplay);
		addSeverityField(LESS_PREFERENCES_LINT_IMPORTANT, Messages.LESSValidationPreferencePage_lint_important);
		addSeverityField(LESS_PREFERENCES_LINT_FLOAT, Messages.LESSValidationPreferencePage_lint_float);
		addSeverityField(LESS_PREFERENCES_LINT_IDSELECTOR, Messages.LESSValidationPreferencePage_lint_idSelector);
		addSeverityField(LESS_PREFERENCES_LINT_UNKNOWNATRULES, Messages.LESSValidationPreferencePage_lint_unknownAtRules);
	}

	private void addSeverityField(String name, String labelText) {
		addField(new ComboFieldEditor(name, labelText,
				new String[][] { { Action.removeMnemonics(IDialogConstants.IGNORE_LABEL), "ignore" },
						{ JFaceResources.getString("warning"), "warning" },
						{ JFaceResources.getString("error"), "error" } },
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
