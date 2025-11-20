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
 * Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.node;

import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.*;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.Messages;
import org.eclipse.wildwebdeveloper.util.FileUtils;

public class RunProgramTab extends AbstractLaunchConfigurationTab {

	private Text programPathText;
	private Text argumentsText;
	private Text workingDirectoryText;
	private final NodeRunDebugLaunchShortcut shortcut = new NodeRunDebugLaunchShortcut();

	@Override
	public void createControl(Composite parent) {
		Composite resComposite = new Composite(parent, SWT.NONE);
		resComposite.setLayout(new GridLayout(2, false));
		var programLabel = new Label(resComposite, SWT.NONE);
		programLabel.setText(Messages.RunProgramTab_program);
		var programLabelGD = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
		programLabel.setLayoutData(programLabelGD);
		Composite programComposite = new Composite(resComposite, SWT.NONE);
		programComposite.setLayout(new GridLayout(1, false));
		programComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		var programGL = (GridLayout) programComposite.getLayout();
		programGL.marginHeight = 0;
		programGL.marginWidth = 0;
		this.programPathText = new Text(programComposite, SWT.BORDER);
		this.programPathText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		ControlDecoration decoration = new ControlDecoration(programPathText, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_ERROR);
		decoration.setImage(fieldDecoration.getImage());
		this.programPathText.addModifyListener(event -> {
			setDirty(true);
			File file;
			try {
				file = new File(VariablesPlugin.getDefault().getStringVariableManager() //
						.performStringSubstitution(programPathText.getText()));
				if (!file.isFile()) {
					String errorMessage = Messages.RunProgramTab_error_unknownFile;
					setErrorMessage(errorMessage);
					decoration.setDescriptionText(errorMessage);
					decoration.show();
				} else if (!shortcut.canLaunch(file)) { //$NON-NLS-1$
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

		// Buttons row for program
		var programButtons = new Composite(programComposite, SWT.NONE);
		var programButtonsGL = new GridLayout(3, false);
		programButtonsGL.marginHeight = 0;
		programButtonsGL.marginWidth = 0;
		programButtons.setLayout(programButtonsGL);
		var programButtonsGD = new GridData(SWT.END, SWT.CENTER, true, false);
		programButtons.setLayoutData(programButtonsGD);

		// Workspace button for program
		var programWorkspaceButton = new Button(programButtons, SWT.PUSH);
		programWorkspaceButton.setText(Messages.AbstractRunHTMLDebugTab_browse_workspace);
		programWorkspaceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			var dialog = new ElementTreeSelectionDialog(resComposite.getShell(), new WorkbenchLabelProvider(),
					new WorkbenchContentProvider());
			dialog.setTitle(Messages.RunProgramTab_program);
			dialog.setMessage(Messages.RunProgramTab_program);
			dialog.setValidator(selection -> {
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
				}
				for (Object f : selection) {
					if (!(f instanceof IFile)) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Must select a file"); //$NON-NLS-1$
					}
				}
				return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
			});
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
			dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
			if (dialog.open() == IDialogConstants.OK_ID) {
				var resource = (IResource) dialog.getFirstResult();
				if (resource != null) {
					String arg = resource.getFullPath().toString();
					String fileLoc = VariablesPlugin.getDefault().getStringVariableManager() //
							.generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
					programPathText.setText(fileLoc);
				}
			}
		}));

		// Filesystem button for program
		var programFilesystemButton = new Button(programButtons, SWT.PUSH);
		programFilesystemButton.setText(Messages.AbstractRunHTMLDebugTab_browse);
		programFilesystemButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			var filePathDialog = new FileDialog(resComposite.getShell());
			filePathDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
			String path = filePathDialog.open();
			if (path != null) {
				programPathText.setText(path);
			}
		}));

		// Variables button for program
		var programVariablesButton = new Button(programButtons, SWT.PUSH);
		programVariablesButton.setText(Messages.AbstractRunHTMLDebugTab_variables);
		programVariablesButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			var dialog = new StringVariableSelectionDialog(resComposite.getShell());
			if (dialog.open() == IDialogConstants.OK_ID) {
				String expr = dialog.getVariableExpression();
				if (expr != null) {
					programPathText.insert(expr);
				}
			}
		}));

		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_argument);
		this.argumentsText = new Text(resComposite, SWT.BORDER);
		this.argumentsText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		this.argumentsText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		var workingLabel = new Label(resComposite, SWT.NONE);
		workingLabel.setText(Messages.RunProgramTab_workingDirectory);
		var workingLabelGD = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
		workingLabel.setLayoutData(workingLabelGD);
		Composite workingComposite = new Composite(resComposite, SWT.NONE);
		workingComposite.setLayout(new GridLayout(1, false));
		workingComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		var workingGL = (GridLayout) workingComposite.getLayout();
		workingGL.marginHeight = 0;
		workingGL.marginWidth = 0;
		this.workingDirectoryText = new Text(workingComposite, SWT.BORDER);
		this.workingDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		this.workingDirectoryText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});

		// Buttons row for working directory
		var workingButtons = new Composite(workingComposite, SWT.NONE);
		var workingButtonsGL = new GridLayout(3, false);
		workingButtonsGL.marginHeight = 0;
		workingButtonsGL.marginWidth = 0;
		workingButtons.setLayout(workingButtonsGL);
		var workingButtonsGD = new GridData(SWT.END, SWT.CENTER, true, false);
		workingButtons.setLayoutData(workingButtonsGD);

		// Workspace button for working directory
		var workingDirWorkspaceButton = new Button(workingButtons, SWT.PUSH);
		workingDirWorkspaceButton.setText(Messages.AbstractRunHTMLDebugTab_browse_workspace);
		workingDirWorkspaceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			var dialog = new ElementTreeSelectionDialog(resComposite.getShell(), new WorkbenchLabelProvider(),
					new WorkbenchContentProvider());
			dialog.setTitle(Messages.RunProgramTab_workingDirectory);
			dialog.setMessage(Messages.RunProgramTab_workingDirectory);
			dialog.setValidator(selection -> {
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
				}
				for (Object f : selection) {
					if (!(f instanceof IProject || f instanceof IFolder)) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Must select a project or a folder"); //$NON-NLS-1$
					}
				}
				return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
			});
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
			dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
			if (dialog.open() == IDialogConstants.OK_ID) {
				var resource = (IResource) dialog.getFirstResult();
				if (resource != null) {
					String arg = resource.getFullPath().toString();
					String fileLoc = VariablesPlugin.getDefault().getStringVariableManager() //
							.generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
					workingDirectoryText.setText(fileLoc);
				}
			}
		}));

		// Filesystem button for working directory
		var workingDirFilesystemButton = new Button(workingButtons, SWT.PUSH);
		workingDirFilesystemButton.setText(Messages.AbstractRunHTMLDebugTab_browse);
		workingDirFilesystemButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			DirectoryDialog workingDirectoryDialog = new DirectoryDialog(resComposite.getShell());
			workingDirectoryDialog.setFilterPath(workingDirectoryText.getText());
			String path = workingDirectoryDialog.open();
			if (path != null) {
				workingDirectoryText.setText(path);
			}
		}));

		// Variables button for working directory
		var workingDirVariablesButton = new Button(workingButtons, SWT.PUSH);
		workingDirVariablesButton.setText(Messages.AbstractRunHTMLDebugTab_variables);
		workingDirVariablesButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			var dialog = new StringVariableSelectionDialog(resComposite.getShell());
			if (dialog.open() == IDialogConstants.OK_ID) {
				String expr = dialog.getVariableExpression();
				if (expr != null) {
					workingDirectoryText.insert(expr);
				}
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
			this.programPathText.setText(configuration.getAttribute(LaunchConstants.PROGRAM, defaultSelectedFile)); //$NON-NLS-1$
			this.argumentsText.setText(configuration.getAttribute(NodeRunDAPDebugDelegate.ARGUMENTS, "")); //$NON-NLS-1$
			this.workingDirectoryText
					.setText(configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, pathOrEmpty(getSelectedProject()))); //$NON-NLS-1$
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
		configuration.setMappedResources(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(FileUtils.toUri(programPath)));
	}

	@Override
	public String getName() {
		return Messages.RunProgramTab_title;
	}

}
