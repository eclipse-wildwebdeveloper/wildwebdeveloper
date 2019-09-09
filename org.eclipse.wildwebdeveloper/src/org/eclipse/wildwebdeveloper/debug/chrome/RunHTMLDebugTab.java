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
package org.eclipse.wildwebdeveloper.debug.chrome;

import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.getSelectedFile;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.getSelectedProject;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.pathOrEmpty;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugAdapterLaunchShortcut;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.Messages;

public class RunHTMLDebugTab extends AbstractLaunchConfigurationTab {

	private Text programPathText;
	private Text argumentsText;
	private Text workingDirectoryText;
	private Button verboseConsoleOutput;
	Composite resComposite;
	private final AbstractDebugAdapterLaunchShortcut shortcut = new ChromeRunDebugLaunchShortcut(); // contains many
																									// utilities

	public RunHTMLDebugTab() {
	}

	@Override
	public void createControl(Composite parent) {
		resComposite = new Composite(parent, SWT.NONE);
		resComposite.setLayout(new GridLayout(3, false));
		new Label(resComposite, SWT.NONE).setText(Messages.FirefoxDebugTab_File);
		this.programPathText = new Text(resComposite, SWT.BORDER);
		this.programPathText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		ControlDecoration decoration = new ControlDecoration(programPathText, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		decoration.setImage(fieldDecoration.getImage());
		this.programPathText.addModifyListener(event -> {
			setDirty(true);
			File file = new File(programPathText.getText());
			if (!file.isFile()) {
				String errorMessage = Messages.RunProgramTab_error_unknownFile;
				setErrorMessage(errorMessage);
				decoration.setDescriptionText(errorMessage);
				decoration.show();
			} else if (!shortcut.canLaunch(file)) {
				String errorMessage = "Not a html file";
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
			updateLaunchConfigurationDialog();
		});
		Button filePath = new Button(resComposite, SWT.PUSH);
		filePath.setText("Browse...");
		filePath.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			FileDialog filePathDialog = new FileDialog(resComposite.getShell());
			filePathDialog.setFilterPath(workingDirectoryText.getText());
			filePathDialog.setText("Select a .html file to debug");
			String path = filePathDialog.open();
			if (path != null) {
				programPathText.setText(path);
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		}));

		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_argument);
		this.argumentsText = new Text(resComposite, SWT.BORDER);
		GridData argsGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		argsGD.horizontalSpan = 2;
		this.argumentsText.setLayoutData(argsGD);
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
		Button workingDirectoryButton = new Button(resComposite, SWT.PUSH);
		workingDirectoryButton.setText("Browse...");
		workingDirectoryButton.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			DirectoryDialog workingDirectoryDialog = new DirectoryDialog(resComposite.getShell());
			workingDirectoryDialog.setFilterPath(workingDirectoryText.getText());
			workingDirectoryDialog.setText("Select folder to watch for changes");
			String path = workingDirectoryDialog.open();
			if (path != null) {
				workingDirectoryText.setText(path);
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		}));

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
			this.programPathText
					.setText(configuration.getAttribute(AbstractDebugDelegate.PROGRAM, defaultSelectedFile)); // $NON-NLS-1$
			this.argumentsText.setText(configuration.getAttribute(AbstractDebugDelegate.ARGUMENTS, "")); //$NON-NLS-1$
			this.workingDirectoryText.setText(
					configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, pathOrEmpty(getSelectedProject()))); // $NON-NLS-1$
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(AbstractDebugDelegate.PROGRAM, this.programPathText.getText());
		configuration.setAttribute(AbstractDebugDelegate.ARGUMENTS, this.argumentsText.getText());
		configuration.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, this.workingDirectoryText.getText());
	
	}

	@Override
	public String getName() {
		return Messages.RunProgramTab_title;
	}

}
