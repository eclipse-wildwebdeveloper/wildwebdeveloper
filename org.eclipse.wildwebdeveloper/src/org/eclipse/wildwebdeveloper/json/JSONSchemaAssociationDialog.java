/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
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
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class JSONSchemaAssociationDialog extends TitleAreaDialog {

	private final static String JSON_BASE_TYPE = "org.eclipse.wildwebdeveloper.json"; //$NON-NLS-1$

	private Combo contentTypeData;
	private Text schemaLocationData;
	private Button okButton;

	private JSONSchemaAssociation preSelectedAssociation;
	private Set<JSONSchemaAssociation> existingAssociations;

	private JSONSchemaAssociation currentAssociation;

	public JSONSchemaAssociationDialog(Shell parentShell, Set<JSONSchemaAssociation> existingAssociations) {
		super(parentShell);
		this.existingAssociations = existingAssociations;
	}

	public JSONSchemaAssociationDialog(Shell parentShell, Set<JSONSchemaAssociation> existingAssociations,
			JSONSchemaAssociation preSelectedAssociation) {
		this(parentShell, existingAssociations);
		this.preSelectedAssociation = preSelectedAssociation;
	}

	@Override
	public void create() {
		super.create();
		if (preSelectedAssociation == null) {
			setTitle(JSONMessages.SchemaAssociationDialog_Add_title);
			setMessage(JSONMessages.SchemaAssociationDialog_Add_subtitle);
		} else {
			setTitle(JSONMessages.SchemaAssociationDialog_Edit_title);
			setMessage(JSONMessages.SchemaAssociationDialog_Edit_subtitle);
		}
		validateDialog();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentArea = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(parentArea, SWT.FILL);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = layout.marginHeight = 10;
		container.setLayout(layout);

		createContentTypeEditor(container);
		createSchemaLocationEditor(container);

		return parentArea;
	}

	private void createContentTypeEditor(Composite container) {
		Label contentTypeLabel = new Label(container, SWT.NONE);
		contentTypeLabel.setText(JSONMessages.ContentType + ":");
		contentTypeLabel.setToolTipText(JSONMessages.ContentTypeId_Tooltip);

		contentTypeData = new Combo(container, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		fillContentTypeCombo(contentTypeData);
		contentTypeData.setLayoutData(gridData);
		if (preSelectedAssociation == null) { // AddSchema
			contentTypeData.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validateDialog();
				}
			});
		} else { // EditSchema
			contentTypeData.setText(getTextFromSchemaAssociation(preSelectedAssociation));
			contentTypeData.setEnabled(false);
		}
	}

	private void fillContentTypeCombo(Combo combo) {
		// Retrieve existing contentTypes with base type JSON
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		for (IContentType contentType : contentTypeManager.getAllContentTypes()) {
			if (contentType.getBaseType() != null && contentType.getBaseType().getId().equals(JSON_BASE_TYPE)) {
				combo.add(getTextFromContentType(contentType));
			}
		}
		// Re-sort combo items
		String[] items = combo.getItems();
		Arrays.sort(items);
		combo.setItems(items);
	}

	private void createSchemaLocationEditor(Composite container) {
		Label schemaLocationLabel = new Label(container, SWT.NONE);
		schemaLocationLabel.setText(JSONMessages.SchemaLocation + ":");
		schemaLocationLabel.setToolTipText(JSONMessages.SchemaLocation_Tooltip);

		schemaLocationData = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		schemaLocationData.setLayoutData(gridData);
		if (preSelectedAssociation != null) {
			schemaLocationData.setText(preSelectedAssociation.getSchemaLocation());
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
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private boolean validateDialog() {
		currentAssociation = null;

		if (contentTypeData.getText().trim().length() <= 0) {
			setErrorMessage(JSONMessages.SchemaAssociationDialog_Error_ContentType_required);
			return updateOkButton(false);
		}

		Set<JSONSchemaAssociation> list = existingAssociations;
		for (JSONSchemaAssociation e : list) {
			if (!e.equals(preSelectedAssociation) && getContentTypeIdFromText().equals(e.getContentTypeId())) {
				setErrorMessage(JSONMessages.SchemaAssociationDialog_Error_ContentType_already_exists);
				return updateOkButton(false);
			}
		}

		if (schemaLocationData.getText().trim().length() <= 0) {
			setErrorMessage(JSONMessages.SchemaAssociationDialog_Error_SchemaLocation_required);
			return updateOkButton(false);
		}

		String schemaLocation;
		try {
			URL locationURL = new URL(schemaLocationData.getText());
			if (locationURL.getProtocol().equals("file")) {
				File file = new File(locationURL.getPath());
				if (file.exists()) {
					schemaLocation = "file://" + file.getAbsolutePath();
				} else {
					setErrorMessage(JSONMessages.SchemaAssociationDialog_Error_SchemaLocation_invalid);
					return updateOkButton(false);
				}
			} else {
				schemaLocation = locationURL.toURI().toString();
			}
		} catch (MalformedURLException | URISyntaxException | NullPointerException e) {
			setErrorMessage(JSONMessages.SchemaAssociationDialog_Error_SchemaLocation_invalid);
			return updateOkButton(false);
		}

		currentAssociation = new JSONSchemaAssociation(getContentTypeFromText(), getContentTypeIdFromText(),
				schemaLocation);
		setErrorMessage(null);
		return updateOkButton(true);
	}

	private boolean updateOkButton(boolean enabled) {
		if (okButton != null) {
			okButton.setEnabled(enabled);
			return true;
		}
		return false;
	}

	private String getTextFromSchemaAssociation(JSONSchemaAssociation schemaAssociation) {
		return schemaAssociation.getContentTypeId() + " (" + schemaAssociation.getContentType() + ")";
	}

	private String getTextFromContentType(IContentType contentType) {
		return contentType.getId() + " (" + contentType.getName() + ")";
	}

	private String getContentTypeIdFromText() {
		return contentTypeData.getText().split(" ")[0];
	}

	private String getContentTypeFromText() {
		return contentTypeData.getText().split("[\\(\\)]")[1];
	}

	public String getContentType() {
		return currentAssociation.getContentType();
	}

	public String getContentTypeId() {
		return currentAssociation.getContentTypeId();
	}

	public String getSchemaLocation() {
		return currentAssociation.getSchemaLocation();
	}

}
