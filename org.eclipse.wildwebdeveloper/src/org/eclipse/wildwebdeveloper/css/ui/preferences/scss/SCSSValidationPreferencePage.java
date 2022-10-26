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

import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_BOXMODEL;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_DUPLICATEPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_EMPTYRULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_FLOAT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_FONTFACEPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_HEXCOLORLENGTH;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_IDSELECTOR;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_IEHACK;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_IMPORTANT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_IMPORTSTATEMENT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_UNIVERSALSELECTOR;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_UNKNOWNATRULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_UNKNOWNPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_VALIDPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_VENDOR_PREFIX;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_LINT_ZEROUNITS;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants.SCSS_PREFERENCES_VALIDATE;

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
 * SCSS validation preference page.
 *
 */
public class SCSSValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SCSSValidationPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(SCSS_PREFERENCES_VALIDATE, Messages.SCSSValidationPreferencePage_validate,
				getFieldEditorParent()));
		addSeverityField(SCSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES,
				Messages.SCSSValidationPreferencePage_lint_compatibleVendorPrefixes);
		addSeverityField(SCSS_PREFERENCES_LINT_VENDOR_PREFIX, Messages.SCSSValidationPreferencePage_lint_vendorPrefix);
		addSeverityField(SCSS_PREFERENCES_LINT_DUPLICATEPROPERTIES,
				Messages.SCSSValidationPreferencePage_lint_duplicateProperties);
		addSeverityField(SCSS_PREFERENCES_LINT_EMPTYRULES, Messages.SCSSValidationPreferencePage_lint_emptyRules);
		addSeverityField(SCSS_PREFERENCES_LINT_IMPORTSTATEMENT,
				Messages.SCSSValidationPreferencePage_lint_importStatement);
		addSeverityField(SCSS_PREFERENCES_LINT_BOXMODEL, Messages.SCSSValidationPreferencePage_lint_boxModel);
		addSeverityField(SCSS_PREFERENCES_LINT_UNIVERSALSELECTOR,
				Messages.SCSSValidationPreferencePage_lint_universalSelector);
		addSeverityField(SCSS_PREFERENCES_LINT_ZEROUNITS, Messages.SCSSValidationPreferencePage_lint_zeroUnits);
		addSeverityField(SCSS_PREFERENCES_LINT_FONTFACEPROPERTIES,
				Messages.SCSSValidationPreferencePage_lint_fontFaceProperties);
		addSeverityField(SCSS_PREFERENCES_LINT_HEXCOLORLENGTH, Messages.SCSSValidationPreferencePage_lint_hexColorLength);
		addSeverityField(SCSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION,
				Messages.SCSSValidationPreferencePage_lint_argumentsInColorFunction);
		addSeverityField(SCSS_PREFERENCES_LINT_UNKNOWNPROPERTIES,
				Messages.SCSSValidationPreferencePage_lint_unknownProperties);
		addField(new ValidPropertiesFieldEditor(SCSS_PREFERENCES_LINT_VALIDPROPERTIES,
				Messages.SCSSValidationPreferencePage_lint_validProperties, getFieldEditorParent()));
		addSeverityField(SCSS_PREFERENCES_LINT_IEHACK, Messages.SCSSValidationPreferencePage_lint_ieHack);
		addSeverityField(SCSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES,
				Messages.SCSSValidationPreferencePage_lint_unknownVendorSpecificProperties);
		addSeverityField(SCSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY,
				Messages.SCSSValidationPreferencePage_lint_propertyIgnoredDueToDisplay);
		addSeverityField(SCSS_PREFERENCES_LINT_IMPORTANT, Messages.SCSSValidationPreferencePage_lint_important);
		addSeverityField(SCSS_PREFERENCES_LINT_FLOAT, Messages.SCSSValidationPreferencePage_lint_float);
		addSeverityField(SCSS_PREFERENCES_LINT_IDSELECTOR, Messages.SCSSValidationPreferencePage_lint_idSelector);
		addSeverityField(SCSS_PREFERENCES_LINT_UNKNOWNATRULES, Messages.SCSSValidationPreferencePage_lint_unknownAtRules);
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
