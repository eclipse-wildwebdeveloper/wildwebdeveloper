/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.npm;

import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.getSelectedFile;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.getSelectedProject;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.pathOrEmpty;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugAdapterLaunchShortcut;
import org.eclipse.wildwebdeveloper.debug.AbstractHTMLDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;

public class NpmLaunchTab extends AbstractLaunchConfigurationTab {

	private Text programPathText;
	protected Composite resComposite;
	protected AbstractDebugAdapterLaunchShortcut shortcut = new NpmLaunchShortcut(); // contains many utilities
	private Combo argumentsCombo;

	private File packageJSONFile;
	private File defaultSelectedFile;

	public NpmLaunchTab() {
	}

	@Override
	public void createControl(Composite parent) {
		resComposite = new Composite(parent, SWT.NONE);
		resComposite.setLayout(new GridLayout(2, false));

		new Label(resComposite, SWT.NONE).setText(Messages.NPMLaunchTab_argumentLabel);
		argumentsCombo = new Combo(resComposite, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER);
		this.argumentsCombo.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		addComboItems(argumentsCombo, "install", "update", "ci", "pack", "run", "run-script", "start", "restart", "test");
		argumentsCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}));
		argumentsCombo.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});

		new Label(resComposite, SWT.NONE).setText(Messages.NPMLaunchTab_programPathLabel);
		Composite filePathComposite = new Composite(resComposite, SWT.NONE);
		filePathComposite.setLayout(new GridLayout(2, false));
		filePathComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		this.programPathText = new Text(filePathComposite, SWT.BORDER);
		this.programPathText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		ControlDecoration decoration = new ControlDecoration(programPathText, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		decoration.setImage(fieldDecoration.getImage());
		this.programPathText.addModifyListener(event -> {
			setDirty(true);
			try {
				File file = new File(VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programPathText.getText()));
				if (!file.isFile()) {
					String errorMessage = org.eclipse.wildwebdeveloper.debug.Messages.RunProgramTab_error_unknownFile;
					setErrorMessage(errorMessage);
					decoration.setDescriptionText(errorMessage);
					decoration.show();
				} else if (!shortcut.canLaunch(file)) {
					String errorMessage = Messages.NPMLaunchTab_notPackageJSONFile;
					setErrorMessage(errorMessage);
					decoration.setDescriptionText(errorMessage);
					decoration.show();
				} else if (!file.canRead()) {
					String errorMessage = org.eclipse.wildwebdeveloper.debug.Messages.RunProgramTab_error_nonReadableFile;
					setErrorMessage(errorMessage);
					decoration.setDescriptionText(errorMessage);
					decoration.show();
				} else {
					setErrorMessage(null);
					decoration.hide();
				}
			} catch (CoreException ex) {
				setErrorMessage(ex.getMessage());
				decoration.setDescriptionText(ex.getMessage());
				decoration.show();
			}
			updateLaunchConfigurationDialog();
		});

		Button filePath = new Button(filePathComposite, SWT.PUSH);
		filePath.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));
		filePath.setText(org.eclipse.wildwebdeveloper.debug.Messages.AbstractRunHTMLDebugTab_browse);
		filePath.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			FileDialog filePathDialog = new FileDialog(resComposite.getShell());
			filePathDialog.setFilterPath(getSelectedProject() == null ? null : getSelectedProject().getAbsolutePath());
			filePathDialog.setText(Messages.NPMLaunchTab_selectPackageJSON);
			String path = filePathDialog.open();
			if (path != null) {
				packageJSONFile = new File(path);
				programPathText.setText(packageJSONFile.getAbsolutePath());
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		}));

		setControl(resComposite);
	}

	public static void addComboItems(Combo combo, String... commands) {
		for (String command : commands) {
			combo.add(command);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Nothing to do
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			defaultSelectedFile = getSelectedFile(shortcut::canLaunch);
			String defaultSelectedFilePath = pathOrEmpty(defaultSelectedFile);
			this.programPathText
					.setText(configuration.getAttribute(LaunchConstants.PROGRAM, defaultSelectedFilePath));
			this.argumentsCombo.setText(configuration.getAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, "install")); //$NON-NLS-1$
		} catch (CoreException e) {
			ILog.get().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String workingDirectory = pathOrEmpty(getSelectedProject());
		if (this.packageJSONFile != null) {
			workingDirectory = pathOrEmpty(this.packageJSONFile.getParentFile());
		} else if (defaultSelectedFile != null) {
			workingDirectory = pathOrEmpty(defaultSelectedFile.getParentFile());
		}

		String programPath = this.programPathText.getText();
		configuration.setAttribute(LaunchConstants.PROGRAM, programPath);
		configuration.setAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, this.argumentsCombo.getText());
		configuration.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDirectory);
		configuration.setMappedResources(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new File(programPath).toURI()));
	}

	@Override
	public String getName() {
		return org.eclipse.wildwebdeveloper.debug.Messages.RunProgramTab_title;
	}

}
