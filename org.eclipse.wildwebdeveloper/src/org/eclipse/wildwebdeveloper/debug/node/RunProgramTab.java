/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.node;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.Messages;

public class RunProgramTab extends AbstractLaunchConfigurationTab {

	private Text programPathText;
	private Text argumentsText;
	private Text workingDirectoryText;
	private final NodeRunDebugLaunchShortcut shortcut = new NodeRunDebugLaunchShortcut();

	@Override
	public void createControl(Composite parent) {
		Composite resComposite = new Composite(parent, SWT.NONE);
		resComposite.setLayout(new GridLayout(2, false));
		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_program);
		this.programPathText = new Text(resComposite, SWT.BORDER);
		this.programPathText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		ControlDecoration decoration = new ControlDecoration(programPathText, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
		        FieldDecorationRegistry.DEC_ERROR);
		decoration.setImage(fieldDecoration.getImage());
		this.programPathText.addModifyListener(event -> {
			setDirty(true);
			File file;
			try {
				file = new File(VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programPathText.getText()));
				if (!file.isFile()) {
					String errorMessage = Messages.RunProgramTab_error_unknownFile;
					setErrorMessage(errorMessage);
					decoration.setDescriptionText(errorMessage);
					decoration.show();
				} else if (!shortcut.canLaunch(file)){ //$NON-NLS-1$
					String errorMessage = Messages.RunProgramTab_error_notJSFile;
					setErrorMessage(errorMessage);
					decoration.setDescriptionText(errorMessage);
					decoration.show();
				} else if (!file.canRead()) {
					String errorMessage = Messages.RunProgramTab_error_nonReadableFile;
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
		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_argument);
		this.argumentsText = new Text(resComposite, SWT.BORDER);
		this.argumentsText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		this.argumentsText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_workingDirectory);
		this.workingDirectoryText = new Text(resComposite, SWT.BORDER);
		this.workingDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		this.workingDirectoryText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		setControl(resComposite);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Nothing to do
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String defaultSelectedFile = pathOrEmpty(getSelectedFile(shortcut::canLaunch));
			this.programPathText.setText(configuration.getAttribute(LaunchConstants.PROGRAM, defaultSelectedFile)); //$NON-NLS-1$
			this.argumentsText.setText(configuration.getAttribute(NodeRunDAPDebugDelegate.ARGUMENTS, "")); //$NON-NLS-1$
			this.workingDirectoryText.setText(configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, pathOrEmpty(getSelectedProject()))); //$NON-NLS-1$
		} catch (CoreException e) {
			ILog.get().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String programPath = this.programPathText.getText();
		configuration.setAttribute(LaunchConstants.PROGRAM, programPath);
		configuration.setAttribute(NodeRunDAPDebugDelegate.ARGUMENTS, this.argumentsText.getText());
		configuration.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, this.workingDirectoryText.getText());
		configuration.setMappedResources(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new File(programPath).toURI()));
	}

	@Override
	public String getName() {
		return Messages.RunProgramTab_title;
	}

}
