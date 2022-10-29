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
package org.eclipse.wildwebdeveloper.yaml.ui.preferences;

import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_MAXITEMSCOMPUTED;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_SCHEMASTORE_ENABLE;
import static org.eclipse.wildwebdeveloper.yaml.ui.preferences.YAMLPreferenceServerConstants.YAML_PREFERENCES_SCHEMASTORE_URL;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.SchemaAssociationsPreferencePage;
import org.eclipse.wildwebdeveloper.yaml.ui.Messages;

/**
 * YAML main preference page.
 *
 */
public class YAMLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public YAMLPreferencePage() {
		super(GRID);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Link catalogsLink = new Link(composite, SWT.NONE);
		catalogsLink.setText(Messages.YAMLPreferencePage_SchemaAssociationsLink);
		catalogsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getContainer() instanceof IWorkbenchPreferenceContainer container) {
					container.openPage(SchemaAssociationsPreferencePage.PAGE_ID, null);
				}
			}
		});
		super.createContents(composite);
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(YAML_PREFERENCES_SCHEMASTORE_ENABLE,
				Messages.YAMLPreferencePage_schemaStore_enable, getFieldEditorParent()));
		addField(new StringFieldEditor(YAML_PREFERENCES_SCHEMASTORE_URL, Messages.YAMLPreferencePage_schemaStore_url,
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(YAML_PREFERENCES_MAXITEMSCOMPUTED, Messages.YAMLPreferencePage_maxItemsComputed,
				getFieldEditorParent()));

	}
}