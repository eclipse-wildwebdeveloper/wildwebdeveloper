package org.eclipse.wildwebdeveloper.yaml;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class YAMLSchemaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

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
		
		Label schemaLabel = new Label(parent, SWT.NONE);
		schemaLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		schemaLabel.setText("Edit yaml.schemas");
		
		schemaTable = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		schemaTable.setHeaderVisible(true);
		schemaTable.setLinesVisible(true);
		schemaTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn schemaCol = new TableColumn(schemaTable, SWT.NONE);
		schemaCol.setText("Schema");
		
		TableColumn globPatternCol = new TableColumn(schemaTable, SWT.NONE);
		globPatternCol.setText("Glob Pattern");
		
		String schemaStr = store.getString(YAMLPreferenceInitializer.YAML_SCHEMA_PREFERENCE);
		Map<String, String> schemas = new Gson().fromJson(schemaStr, new TypeToken<HashMap<String, String>>() {}.getType());
		for (String s : schemas.keySet()) {
			TableItem item = new TableItem(schemaTable, SWT.NONE);
			item.setText(0, s);
			item.setText(1, schemas.get(s));
		}
		
		Composite buttonsBar = new Composite(parent, SWT.NONE);
		buttonsBar.setLayout(new RowLayout());
		buttonsBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		Button addButton = new Button(buttonsBar, SWT.PUSH);
		addButton.setText("Add");
		addButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				AddShemaGlobPatternDialog dialog = new AddShemaGlobPatternDialog(parent.getShell());
				dialog.create();
				if (dialog.open() == Window.OK) {
					TableItem newItem = new TableItem(schemaTable, SWT.NONE);
					newItem.setText(0, dialog.getSchemaKey());
					newItem.setText(1, dialog.getGlobPattern());
				}
			}
		});
		
		Button removeButton = new Button(buttonsBar, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				schemaTable.remove(schemaTable.getSelectionIndex());
			}
		});
		
		schemaCol.pack();
		globPatternCol.pack();
		parent.layout();

		return new Composite(parent, SWT.NONE);
	}

	@Override
	public boolean performOk() {
		JsonObject schemaJson = new JsonObject();
		for (TableItem item : schemaTable.getItems()) {
			schemaJson.addProperty(item.getText(0), item.getText(1));
		}
		store.setValue(YAMLPreferenceInitializer.YAML_SCHEMA_PREFERENCE, schemaJson.toString());
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

}
