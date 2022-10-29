/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class SchemaAssociationsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID= "org.eclipse.wildwebdeveloper.SchemaAssociationsPreferencePage";
	
	private IPreferenceStore store;
	private Table schemaTable;

	@Override
	public void init(IWorkbench workbench) {
		store = doGetPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		Link pageTitle = new Link(parent, SWT.NONE);
		pageTitle.setLayoutData(new GridData(SWT.FILL));
		pageTitle.setText(SchemaAssociationsMessages.SchemaAssociations_PreferencePage_title);
		pageTitle.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			if (getContainer() instanceof IWorkbenchPreferenceContainer prefContainer) {
				prefContainer.openPage("org.eclipse.ui.preferencePages.ContentTypes", null);
			}
		}));

		schemaTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		schemaTable.setHeaderVisible(true);
		schemaTable.setLinesVisible(true);
		schemaTable.setLayoutData(new GridData(SWT.FILL));

		TableColumn contentTypeCol = new TableColumn(schemaTable, SWT.LEFT);
		contentTypeCol.setText(SchemaAssociationsMessages.ContentType);
		TableColumn contentTypeIdCol = new TableColumn(schemaTable, SWT.LEFT);
		contentTypeIdCol.setText(SchemaAssociationsMessages.ContentTypeId);
		TableColumn schemaLocationCol = new TableColumn(schemaTable, SWT.LEFT);
		schemaLocationCol.setText(SchemaAssociationsMessages.SchemaLocation);

		String schemaString = store.getString(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE);
		insertTableItems(schemaString);

		Composite buttonsBar = new Composite(parent, SWT.NONE);
		buttonsBar.setLayout(new RowLayout());
		buttonsBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

		Button addButton = new Button(buttonsBar, SWT.PUSH);
		addButton.setText(SchemaAssociationsMessages.Add);
		addButton.addListener(SWT.Selection, event -> {
			SchemaAssociationDialog dialog = new SchemaAssociationDialog(parent.getShell(), getSchemaAssociations());
			dialog.create();
			if (dialog.open() == Window.OK) {
				TableItem newItem = new TableItem(schemaTable, SWT.NONE);
				newItem.setText(0, dialog.getContentType());
				newItem.setText(1, dialog.getContentTypeId());
				newItem.setText(2, dialog.getSchemaLocation());
			}
		});

		Button editButton = new Button(buttonsBar, SWT.PUSH);
		editButton.setText(SchemaAssociationsMessages.Edit);
		editButton.addListener(SWT.Selection, event -> {
			TableItem[] selection = schemaTable.getSelection();
			if (selection.length == 0) {
				return;
			}
			TableItem selectedItem = selection[0];

			SchemaAssociation selectedAssociation = new SchemaAssociation(selectedItem.getText(0),
					selectedItem.getText(1), selectedItem.getText(2));
			SchemaAssociationDialog dialog = new SchemaAssociationDialog(parent.getShell(), getSchemaAssociations(),
					selectedAssociation);
			dialog.create();
			if (dialog.open() == Window.OK) {
				selectedItem.setText(0, dialog.getContentType());
				selectedItem.setText(1, dialog.getContentTypeId());
				selectedItem.setText(2, dialog.getSchemaLocation());
			}
		});

		Button removeButton = new Button(buttonsBar, SWT.PUSH);
		removeButton.setText(SchemaAssociationsMessages.Remove);
		removeButton.addListener(SWT.Selection, event -> {
			if (schemaTable.getSelectionCount() > 0) {
				schemaTable.remove(schemaTable.getSelectionIndex());
			}
		});

		contentTypeCol.pack();
		contentTypeIdCol.pack();
		schemaLocationCol.pack();

		Composite extensionPointNote = createNoteComposite(parent.getFont(), parent, WorkbenchMessages.Preference_note,
				SchemaAssociationsMessages.SchemaAssociations_PreferencePage_note);
		extensionPointNote.setLayoutData(new GridData(SWT.FILL));

		parent.layout();
		return new Composite(parent, SWT.NONE);
	}

	@Override
	public boolean performOk() {
		JsonObject schemaAssociation = new JsonObject();
		for (TableItem item : schemaTable.getItems()) {
			schemaAssociation.addProperty(item.getText(1), item.getText(2));
		}
		store.setValue(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE,
				schemaAssociation.toString());
		refreshTable();
		return true;
	}

	protected void refreshTable() {
		schemaTable.removeAll();
		String schemaString = store.getString(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE);
		insertTableItems(schemaString);
	}

	@Override
	protected void performDefaults() {
		schemaTable.removeAll();
		String defaultSchemaAssociations = store
				.getDefaultString(SchemaAssociationsPreferenceInitializer.SCHEMA_ASSOCIATIONS_PREFERENCE);
		insertTableItems(defaultSchemaAssociations);
		super.performDefaults();
	}

	private void insertTableItems(String schemaAssociationsString) {
		TreeMap<String, String> associations = new Gson().fromJson(schemaAssociationsString,
				new TypeToken<TreeMap<String, String>>() {
				}.getType());
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		for (String contentTypeId : associations.keySet()) {
			IContentType contentType = contentTypeManager.getContentType(contentTypeId);
			TableItem item = new TableItem(schemaTable, SWT.NONE);
			item.setText(0, contentType.getName());
			item.setText(1, contentTypeId);
			item.setText(2, associations.get(contentTypeId));
		}
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	private Set<SchemaAssociation> getSchemaAssociations() {
		Set<SchemaAssociation> associations = new HashSet<>();
		for (TableItem item : schemaTable.getItems()) {
			associations.add(new SchemaAssociation(item.getText(0), item.getText(1), item.getText(2)));
		}
		return associations;
	}

}
