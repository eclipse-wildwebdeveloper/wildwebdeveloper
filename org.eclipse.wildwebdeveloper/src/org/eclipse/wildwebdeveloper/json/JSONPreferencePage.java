/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.wildwebdeveloper.Activator;

public class JSONPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		parent.setLayoutData(new GridData(SWT.NONE, SWT.TOP, true, false));

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Link jsonSchemasLink = new Link(container, SWT.NONE);
		jsonSchemasLink.setText(JSONMessages.JSON_PreferencePage_title);
		jsonSchemasLink.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			if (getContainer() instanceof IWorkbenchPreferenceContainer) {
				((IWorkbenchPreferenceContainer) getContainer())
						.openPage("org.eclipse.wildwebdeveloper.json.JSONSchemaPreferencePage", null);
			}
		}));

		return parent;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

}
