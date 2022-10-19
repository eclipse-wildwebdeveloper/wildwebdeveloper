/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceClientConstants.XML_PREFERENCES_COMPLETION_AUTO_CLOSE_TAGS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * XML main preference page.
 *
 */
public class XMLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLPreferencePage() {
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
		catalogsLink.setText(Messages.XMLPreferencePage_XMLCatalogsLink);
		catalogsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getContainer() instanceof IWorkbenchPreferenceContainer container) {
					container.openPage(
							"org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLCatalogPreferencePage", null);
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
		addField(new BooleanFieldEditor(XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId,
				Messages.XMLPreferencePage_downloadExternalResources_enabled, getFieldEditorParent()));
		addField(new BooleanFieldEditor(XML_PREFERENCES_COMPLETION_AUTO_CLOSE_TAGS,
				Messages.XMLPreferencePage_completion_autoCloseTags, getFieldEditorParent()));

	}
}