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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class JSONSchemaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

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

		Label pageTitle = new Label(parent, SWT.NONE);
		pageTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pageTitle.setText(JSONMessages.JSON_Schema_PreferencePage_title);

		schemaTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		schemaTable.setHeaderVisible(true);
		schemaTable.setLinesVisible(true);
		schemaTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn filePatternCol = new TableColumn(schemaTable, SWT.LEFT);
		filePatternCol.setText(JSONMessages.FilePattern);
		TableColumn schemaLocationCol = new TableColumn(schemaTable, SWT.LEFT);
		schemaLocationCol.setText(JSONMessages.SchemaLocation);

		String schemaString = store.getString(JSONPreferenceInitializer.JSON_SCHEMA_PREFERENCE);
		insertTableItems(schemaString);

		Composite buttonsBar = new Composite(parent, SWT.NONE);
		buttonsBar.setLayout(new RowLayout());
		buttonsBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

		Button addButton = new Button(buttonsBar, SWT.PUSH);
		addButton.setText(JSONMessages.Add);
		addButton.addListener(SWT.Selection, event -> {
			JSONSchemaEntryDialog dialog = new JSONSchemaEntryDialog(parent.getShell(), getJSONSchemaEntries());
			dialog.create();
			if (dialog.open() == Window.OK) {
				TableItem newItem = new TableItem(schemaTable, SWT.NONE);
				newItem.setText(0, dialog.getFilePattern());
				newItem.setText(1, dialog.getSchemaLocation());
			}
		});

		Button editButton = new Button(buttonsBar, SWT.PUSH);
		editButton.setText(JSONMessages.Edit);
		editButton.addListener(SWT.Selection, event -> {
			TableItem[] selection = schemaTable.getSelection();
			if (selection.length == 0) {
				return;
			}
			TableItem selectedItem = selection[0];

			JSONSchemaEntry selectedEntry = new JSONSchemaEntry(selectedItem.getText(0), selectedItem.getText(1));
			JSONSchemaEntryDialog dialog = new JSONSchemaEntryDialog(parent.getShell(), getJSONSchemaEntries(),
					selectedEntry);
			dialog.create();
			if (dialog.open() == Window.OK) {
				selectedItem.setText(0, dialog.getFilePattern());
				selectedItem.setText(1, dialog.getSchemaLocation());
			}
		});

		Button removeButton = new Button(buttonsBar, SWT.PUSH);
		removeButton.setText(JSONMessages.Remove);
		removeButton.addListener(SWT.Selection, event -> schemaTable.remove(schemaTable.getSelectionIndex()));

		filePatternCol.pack();
		schemaLocationCol.pack();
		parent.layout();
		return new Composite(parent, SWT.NONE);
	}

	@Override
	public boolean performOk() {
		JsonObject schemaJson = new JsonObject();
		for (TableItem item : schemaTable.getItems()) {
			schemaJson.addProperty(item.getText(0), item.getText(1));
		}
		store.setValue(JSONPreferenceInitializer.JSON_SCHEMA_PREFERENCE, schemaJson.toString());
		refreshTable();
		return true;
	}

	protected void refreshTable() {
		schemaTable.removeAll();
		String schemaString = store.getString(JSONPreferenceInitializer.JSON_SCHEMA_PREFERENCE);
		insertTableItems(schemaString);
	}

	@Override
	protected void performDefaults() {
		schemaTable.removeAll();
		String defaultSchemaString = store.getDefaultString(JSONPreferenceInitializer.JSON_SCHEMA_PREFERENCE);
		insertTableItems(defaultSchemaString);
		super.performDefaults();
	}

	private void insertTableItems(String schemaString) {
		TreeMap<String, String> schemas = new Gson().fromJson(schemaString, new TypeToken<TreeMap<String, String>>(){}.getType());
		for (String s : schemas.keySet()) {
			TableItem item = new TableItem(schemaTable, SWT.NONE);
			item.setText(0, s);
			item.setText(1, schemas.get(s));
		}
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	private Set<JSONSchemaEntry> getJSONSchemaEntries() {
		Set<JSONSchemaEntry> entries = new HashSet<>();
		for (TableItem item : schemaTable.getItems()) {
			entries.add(new JSONSchemaEntry(item.getText(0), item.getText(1)));
		}
		return entries;
	}

}
