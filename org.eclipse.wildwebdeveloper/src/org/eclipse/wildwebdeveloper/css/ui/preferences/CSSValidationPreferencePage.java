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

import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_BOXMODEL;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_DUPLICATEPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_EMPTYRULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_FLOAT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_FONTFACEPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_HEXCOLORLENGTH;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_IDSELECTOR;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_IEHACK;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_IMPORTANT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_IMPORTSTATEMENT;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_UNIVERSALSELECTOR;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_UNKNOWNATRULES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_UNKNOWNPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_VALIDPROPERTIES;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_VENDOR_PREFIX;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_LINT_ZEROUNITS;
import static org.eclipse.wildwebdeveloper.css.ui.preferences.CSSPreferenceServerConstants.CSS_PREFERENCES_VALIDATE;

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

/**
 * CSS validation preference page.
 *
 */
public class CSSValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CSSValidationPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(CSS_PREFERENCES_VALIDATE, Messages.CSSValidationPreferencePage_validate,
				getFieldEditorParent()));
		addSeverityField(CSS_PREFERENCES_LINT_COMPATIBLE_VENDOR_PREFIXES,
				Messages.CSSValidationPreferencePage_lint_compatibleVendorPrefixes);
		addSeverityField(CSS_PREFERENCES_LINT_VENDOR_PREFIX, Messages.CSSValidationPreferencePage_lint_vendorPrefix);
		addSeverityField(CSS_PREFERENCES_LINT_DUPLICATEPROPERTIES,
				Messages.CSSValidationPreferencePage_lint_duplicateProperties);
		addSeverityField(CSS_PREFERENCES_LINT_EMPTYRULES, Messages.CSSValidationPreferencePage_lint_emptyRules);
		addSeverityField(CSS_PREFERENCES_LINT_IMPORTSTATEMENT,
				Messages.CSSValidationPreferencePage_lint_importStatement);
		addSeverityField(CSS_PREFERENCES_LINT_BOXMODEL, Messages.CSSValidationPreferencePage_lint_boxModel);
		addSeverityField(CSS_PREFERENCES_LINT_UNIVERSALSELECTOR,
				Messages.CSSValidationPreferencePage_lint_universalSelector);
		addSeverityField(CSS_PREFERENCES_LINT_ZEROUNITS, Messages.CSSValidationPreferencePage_lint_zeroUnits);
		addSeverityField(CSS_PREFERENCES_LINT_FONTFACEPROPERTIES,
				Messages.CSSValidationPreferencePage_lint_fontFaceProperties);
		addSeverityField(CSS_PREFERENCES_LINT_HEXCOLORLENGTH, Messages.CSSValidationPreferencePage_lint_hexColorLength);
		addSeverityField(CSS_PREFERENCES_LINT_ARGUMENTSINCOLORFUNCTION,
				Messages.CSSValidationPreferencePage_lint_argumentsInColorFunction);
		addSeverityField(CSS_PREFERENCES_LINT_UNKNOWNPROPERTIES,
				Messages.CSSValidationPreferencePage_lint_unknownProperties);
		addField(new ValidPropertiesFieldEditor(CSS_PREFERENCES_LINT_VALIDPROPERTIES,
				Messages.CSSValidationPreferencePage_lint_validProperties, getFieldEditorParent()));
		addSeverityField(CSS_PREFERENCES_LINT_IEHACK, Messages.CSSValidationPreferencePage_lint_ieHack);
		addSeverityField(CSS_PREFERENCES_LINT_UNKNOWNVENDORSPECIFICPROPERTIES,
				Messages.CSSValidationPreferencePage_lint_unknownVendorSpecificProperties);
		addSeverityField(CSS_PREFERENCES_LINT_PROPERTYIGNOREDDUETODISPLAY,
				Messages.CSSValidationPreferencePage_lint_propertyIgnoredDueToDisplay);
		addSeverityField(CSS_PREFERENCES_LINT_IMPORTANT, Messages.CSSValidationPreferencePage_lint_important);
		addSeverityField(CSS_PREFERENCES_LINT_FLOAT, Messages.CSSValidationPreferencePage_lint_float);
		addSeverityField(CSS_PREFERENCES_LINT_IDSELECTOR, Messages.CSSValidationPreferencePage_lint_idSelector);
		addSeverityField(CSS_PREFERENCES_LINT_UNKNOWNATRULES, Messages.CSSValidationPreferencePage_lint_unknownAtRules);
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
