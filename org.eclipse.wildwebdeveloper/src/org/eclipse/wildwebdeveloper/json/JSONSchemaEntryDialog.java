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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class JSONSchemaEntryDialog extends TitleAreaDialog {

	private Text filePatternData;
	private Text schemaLocationData;
	private Button okButton;

	private JSONSchemaEntry selectedEntry;
	private Set<JSONSchemaEntry> existingEntries;

	private String filePattern = "";
	private String schemaLocation = "";

	public JSONSchemaEntryDialog(Shell parentShell, Set<JSONSchemaEntry> existingEntries) {
		super(parentShell);
		this.existingEntries = existingEntries;
	}

	public JSONSchemaEntryDialog(Shell parentShell, Set<JSONSchemaEntry> existingEntries,
			JSONSchemaEntry selectedEntry) {
		this(parentShell, existingEntries);
		this.selectedEntry = selectedEntry;
	}

	@Override
	public void create() {
		super.create();
		if (selectedEntry == null) {
			setTitle(JSONMessages.EntryDialog_AddSchema_title);
			setMessage(JSONMessages.EntryDialog_AddSchema_subtitle);
		} else {
			setTitle(JSONMessages.EntryDialog_EditSchema_title);
			setMessage(JSONMessages.EntryDialog_EditSchema_subtitle);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentArea = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(parentArea, SWT.FILL);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = layout.marginHeight = 10;
		container.setLayout(layout);

		createFilePatternEditor(container);
		createSchemaFilePathEditor(container);

		return parentArea;
	}

	private void createFilePatternEditor(Composite container) {
		Label filePatternLabel = new Label(container, SWT.NONE);
		filePatternLabel.setText(JSONMessages.FilePattern + ":");
		filePatternLabel.setToolTipText(JSONMessages.FilePattern_Tooltip);

		filePatternData = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		filePatternData.setLayoutData(gridData);
		if (selectedEntry != null) {
			filePatternData.setText(selectedEntry.getFilePattern());
		}
		filePatternData.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateDialog();
			}
		});
	}

	private void createSchemaFilePathEditor(Composite container) {
		Label urlLabel = new Label(container, SWT.NONE);
		urlLabel.setText(JSONMessages.SchemaLocation + ":");
		urlLabel.setToolTipText(JSONMessages.SchemaLocation_Tooltip);

		schemaLocationData = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		schemaLocationData.setLayoutData(gridData);
		if (selectedEntry != null) {
			schemaLocationData.setText(selectedEntry.getSchemaLocation());
		}
		schemaLocationData.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateDialog();
			}
		});

		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setText(JSONMessages.Browse);
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
				dialog.setFilterPath(System.getProperty("user.home"));
				dialog.setFilterExtensions(new String[] { "*.json" });

				String result = dialog.open();
				if (result == null || result.trim().length() == 0) {
					return;
				}
				try {
					schemaLocationData.setText("file://" + new File(result).getAbsolutePath());
				} catch (NullPointerException e) {
					return;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(selectedEntry != null);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private boolean validateDialog() {
		filePattern = "";
		schemaLocation = "";

		if (filePatternData.getText().trim().length() <= 0) {
			setErrorMessage(JSONMessages.EntryDialog_Error_FilePattern_required);
			return updateButton(false);
		}

		Set<JSONSchemaEntry> list = existingEntries;
		for (JSONSchemaEntry e : list) {
			if (!e.equals(selectedEntry) && filePatternData.getText().equals(e.getFilePattern())) {
				setErrorMessage(JSONMessages.EntryDialog_Error_FilePattern_already_exists);
				return updateButton(false);
			}
		}

		if (schemaLocationData.getText().trim().length() <= 0) {
			setErrorMessage(JSONMessages.EntryDialog_Error_SchemaLocation_required);
			return updateButton(false);
		}

		try {
			URL locationURL = new URL(schemaLocationData.getText());
			if (locationURL.getProtocol().equals("file")) {
				File file = new File(locationURL.getPath());
				if (file.exists()) {
					schemaLocation = "file://" + file.getAbsolutePath();
				} else {
					setErrorMessage(JSONMessages.EntryDialog_Error_SchemaLocation_invalid);
					return updateButton(false);
				}
			} else {
				schemaLocation = locationURL.toURI().toString();
			}
		} catch (MalformedURLException | URISyntaxException | NullPointerException e) {
			setErrorMessage(JSONMessages.EntryDialog_Error_SchemaLocation_invalid);
			return updateButton(false);
		}

		filePattern = filePatternData.getText();
		setErrorMessage(null);
		return updateButton(true);
	}

	private boolean updateButton(boolean enabled) {
		if (okButton != null) {
			okButton.setEnabled(enabled);
			return true;
		}
		return false;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}
}
