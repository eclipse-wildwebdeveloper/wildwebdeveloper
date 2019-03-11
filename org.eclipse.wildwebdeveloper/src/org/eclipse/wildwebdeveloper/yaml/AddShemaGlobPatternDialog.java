/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.yaml;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddShemaGlobPatternDialog extends TitleAreaDialog {
	private Text schemaKeyText;
	private Text globPatternText;
	
	private String schemaKey;
	private String globPattern;
	
	public AddShemaGlobPatternDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Add Schema");
		setMessage("Associate YAML Schema");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createSchema(container);
        createGlobPattern(container);
		return area;
	}
	
	private void createSchema(Composite container) {
        Label lbtFirstName = new Label(container, SWT.NONE);
        lbtFirstName.setText("Schema");

        GridData dataFirstName = new GridData();
        dataFirstName.grabExcessHorizontalSpace = true;
        dataFirstName.horizontalAlignment = GridData.FILL;

        schemaKeyText = new Text(container, SWT.BORDER);
        schemaKeyText.setLayoutData(dataFirstName);
    }

    private void createGlobPattern(Composite container) {
        Label lbtLastName = new Label(container, SWT.NONE);
        lbtLastName.setText("Glob Pattern");

        GridData dataLastName = new GridData();
        dataLastName.grabExcessHorizontalSpace = true;
        dataLastName.horizontalAlignment = GridData.FILL;
        globPatternText = new Text(container, SWT.BORDER);
        globPatternText.setLayoutData(dataLastName);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
    
    private void saveInput() {
    	schemaKey = schemaKeyText.getText();
    	globPattern = globPatternText.getText();

    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    public String getSchemaKey() {
        return schemaKey;
    }

    public String getGlobPattern() {
        return globPattern;
    }

}
