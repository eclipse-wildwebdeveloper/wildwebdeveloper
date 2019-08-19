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
package org.eclipse.wildwebdeveloper.debug;


import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wildwebdeveloper.Activator;

public class RunFirefoxDebugTab extends AbstractLaunchConfigurationTab {

	private Text programPathText;
	private Text argumentsText;
	private Text workingDirectoryText;
	private Button reloadOnChange;
	private Button filePath;
	private Button workingDirectory;

	@Override
	public void createControl(Composite parent) {
		Composite resComposite = new Composite(parent, SWT.NONE);
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
			} else if (!file.getName().endsWith(".html")) { //$NON-NLS-1$
				String errorMessage = "Not a html file";
				setErrorMessage(errorMessage);
				decoration.setDescriptionText(errorMessage);
				decoration.show();
			}
			else if (!file.canRead()) {
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
		filePath = new Button( resComposite, SWT.PUSH);
		filePath.setText("Browse...");
		
		FileDialog filePathDialog = new FileDialog(resComposite.getShell());
		filePathDialog.setFilterPath(getSelectedProjectPath());
		filePathDialog.setText("Select a .html file to debug");
		filePath.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = filePathDialog.open();
				if (path != null) {
					programPathText.setText(path);
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}
		});
		
		
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
		workingDirectory = new Button( resComposite, SWT.PUSH);
		workingDirectory.setText("Browse...");
		FileDialog workingDirectoryDialog = new FileDialog(resComposite.getShell());
		workingDirectoryDialog.setFilterPath(getSelectedProjectPath());
		workingDirectoryDialog.setText("Select folder to watch for changes");
		workingDirectory.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = workingDirectoryDialog.open();
				if (path != null) {
					workingDirectoryText.setText(path);
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
				
			}
		});
		
		reloadOnChange = new Button(resComposite, SWT.CHECK);
		reloadOnChange.setText("Reload on change: ");
		reloadOnChange.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}
		});
		setControl(resComposite);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Nothing to do
		}

	public String getSelectedFilePath() {
		try {
			IStructuredSelection currentSelection = (IStructuredSelection) Activator.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getSelectionService().getSelection();
			Object sel = currentSelection.getFirstElement();
			IFile file = Platform.getAdapterManager().getAdapter(sel, IFile.class);
			if (file == null && sel instanceof IAdaptable) {
					file = ((IAdaptable) sel).getAdapter(IFile.class);
				}
			if (file != null) {
				return file.getRawLocation().makeAbsolute().toOSString();
			}

		} catch (Exception e) {
			// TODO: Log this exception
			e.printStackTrace();
		}
		return "";
	}
	
	public String getSelectedProjectPath() {
		try {
			IStructuredSelection currentSelection = (IStructuredSelection) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getSelectionService().getSelection();
			Object sel = currentSelection.getFirstElement();
			IFile file = Platform.getAdapterManager().getAdapter(sel, IFile.class);
			if (file != null) {
				return file.getProject().getRawLocation().toOSString();	
			} else {
				IProject project = Platform.getAdapterManager().getAdapter(sel, IProject.class);
				return project.getRawLocation().toOSString();
			}
			
		} catch (Exception e) {
			// TODO: Log this exception
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			this.programPathText.setText(configuration.getAttribute(FirefoxRunDABDebugDelegate.FILE, getSelectedFilePath())); // $NON-NLS-1$
			this.argumentsText.setText(configuration.getAttribute(NodeRunDAPDebugDelegate.ARGUMENTS, "")); //$NON-NLS-1$
			this.workingDirectoryText.setText(configuration.getAttribute(FirefoxRunDABDebugDelegate.WORKING_DIRECTORY, getSelectedProjectPath())); //$NON-NLS-1$
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(FirefoxRunDABDebugDelegate.FILE, this.programPathText.getText());
		configuration.setAttribute(NodeRunDAPDebugDelegate.ARGUMENTS, this.argumentsText.getText());
		configuration.setAttribute(FirefoxRunDABDebugDelegate.WORKING_DIRECTORY, this.workingDirectoryText.getText());
		configuration.setAttribute(FirefoxRunDABDebugDelegate.RELOAD_ON_CHANGE, reloadOnChange.getSelection());
	}

	@Override
	public String getName() {
		return Messages.RunProgramTab_title;
	}

}
