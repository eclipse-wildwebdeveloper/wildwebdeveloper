/*******************************************************************************
 * Copyright (c) 2018, 2023 Red Hat Inc. and others.
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.getSelectedFile;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.getSelectedProject;
import static org.eclipse.wildwebdeveloper.debug.SelectionUtils.pathOrEmpty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

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
import org.eclipse.wildwebdeveloper.debug.chrome.ChromeRunDAPDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.chrome.ChromeRunDebugLaunchShortcut;

public abstract class AbstractRunHTMLDebugTab extends AbstractLaunchConfigurationTab {
	
	private Text programPathText;
	private Text argumentsText;
	private Text workingDirectoryText;
	protected Composite resComposite;
	private Text urlText;
	private ControlDecoration urlDecoration;
	private Text webRootText;
	private ControlDecoration webRootDecoration;
	private Button webRootProjectSelectButton;
	private Button webRootFilesystemSelectButton;
	protected AbstractDebugAdapterLaunchShortcut shortcut = new ChromeRunDebugLaunchShortcut();
	private Button filePath;
	private ControlDecoration fileDecoration;
	private Button fileRadio;
	private Button urlRadio;

	public AbstractRunHTMLDebugTab() {
	}

	@Override
	public void createControl(Composite parent) {
		resComposite = new Composite(parent, SWT.NONE);
		resComposite.setLayout(new GridLayout(4, false));
		
		fileRadio = createRadioButton(resComposite, Messages.FirefoxDebugTab_File); 
		fileRadio.setToolTipText(Messages.AbstractRunHTMLDebugTab_fileRadioToolTip);
		fileRadio.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		fileRadio.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			urlText.setEnabled(false);
			webRootText.setEnabled(false);
			webRootProjectSelectButton.setEnabled(false);
			webRootFilesystemSelectButton.setEnabled(false);
			programPathText.setEnabled(true);
			filePath.setEnabled(true);
			validateProgramPathAndURL();
			updateLaunchConfigurationDialog();
		}));
		
		this.programPathText = new Text(resComposite, SWT.BORDER);
		this.programPathText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		fileDecoration = new ControlDecoration(programPathText, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		fileDecoration.setImage(fieldDecoration.getImage());
		this.programPathText.addModifyListener(event -> {
			validateProgramPathAndURL();
			updateLaunchConfigurationDialog();
		});
		filePath = new Button(resComposite, SWT.PUSH);
		filePath.setText(Messages.AbstractRunHTMLDebugTab_browse);
		filePath.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			FileDialog filePathDialog = new FileDialog(resComposite.getShell());
			filePathDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
			filePathDialog.setText("Select a .html file to debug"); //$NON-NLS-1$
			String path = filePathDialog.open();
			if (path != null) {
				programPathText.setText(path);
			}
		}));
		
		urlRadio = createRadioButton(resComposite, "URL: ");
		urlRadio.setToolTipText(Messages.RunFirefoxDebugTab_URL_Note);
		urlRadio.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		urlRadio.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			programPathText.setEnabled(false);
			filePath.setEnabled(false);
			urlText.setEnabled(true);
			webRootText.setEnabled(true);
			webRootProjectSelectButton.setEnabled(true);
			webRootFilesystemSelectButton.setEnabled(true);
			validateProgramPathAndURL();
			updateLaunchConfigurationDialog();
		}));
		urlText = new Text(resComposite, SWT.BORDER);
		GridData urlTextGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		urlTextGD.horizontalSpan = 3;
		urlText.setLayoutData(urlTextGD);
		urlDecoration = new ControlDecoration(urlText, SWT.TOP | SWT.LEFT);
		urlDecoration.setImage(fieldDecoration.getImage());
		urlText.addModifyListener(e -> {
			validateProgramPathAndURL();
			updateLaunchConfigurationDialog();
		});
		
		new Label(resComposite, SWT.NONE).setText(Messages.AbstractRunHTMLDebugTab_webRoot_folder);
		webRootText = new Text(resComposite, SWT.BORDER);
		webRootText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		webRootDecoration = new ControlDecoration(webRootText, SWT.TOP | SWT.LEFT);
		webRootDecoration.setImage(fieldDecoration.getImage());
		webRootText.addModifyListener(e -> {
			validateProgramPathAndURL();
			updateLaunchConfigurationDialog();
		});
		webRootProjectSelectButton = new Button(resComposite, SWT.PUSH);
		webRootProjectSelectButton.setText(Messages.AbstractRunHTMLDebugTab_browse_workspace);
		webRootProjectSelectButton.addSelectionListener(widgetSelectedAdapter(e -> {
			ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
					new WorkbenchContentProvider());
			dialog.setTitle(Messages.AbstractRunHTMLDebugTab_select_webroot);
			dialog.setMessage(Messages.AbstractRunHTMLDebugTab_select_webroot);
			dialog.setValidator(selection -> {
				//Ok-button is only activated after the user has selected something
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
							"", null);
				}
				for (Object f : selection) {
					if (!(f instanceof IProject || f instanceof IFolder)) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
								"Must select a project or a folder", null); //$NON-NLS-1$
					}
				}
				return new Status(IStatus.OK, Activator.PLUGIN_ID, 0,
						"", null);
			});
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
			dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
			if (dialog.open() == IDialogConstants.OK_ID) {
				IResource resource = (IResource) dialog.getFirstResult();
				if (resource != null) {
					String arg = resource.getFullPath().toString();
					String fileLoc = VariablesPlugin.getDefault().getStringVariableManager()
							.generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
					webRootText.setText(fileLoc);
				}
			}
		}));
		webRootFilesystemSelectButton = new Button(resComposite, SWT.PUSH);
		webRootFilesystemSelectButton.setText(Messages.AbstractRunHTMLDebugTab_browse);
		webRootFilesystemSelectButton.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			DirectoryDialog directoryDialog = new DirectoryDialog(resComposite.getShell());
			directoryDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
			directoryDialog.setText(Messages.AbstractRunHTMLDebugTab_select_webroot);
			String path = directoryDialog.open();
			if (path != null) {
				webRootText.setText(path);
			}
		}));

		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_argument);
		this.argumentsText = new Text(resComposite, SWT.BORDER);
		GridData argsGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		argsGD.horizontalSpan = 3;
		this.argumentsText.setLayoutData(argsGD);
		this.argumentsText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		new Label(resComposite, SWT.NONE).setText(Messages.RunProgramTab_workingDirectory);
		this.workingDirectoryText = new Text(resComposite, SWT.BORDER);
		this.workingDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		this.workingDirectoryText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		Button workingDirectoryButton = new Button(resComposite, SWT.PUSH);
		workingDirectoryButton.setText(Messages.AbstractRunHTMLDebugTab_browse);
		workingDirectoryButton.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			DirectoryDialog workingDirectoryDialog = new DirectoryDialog(resComposite.getShell());
			workingDirectoryDialog.setFilterPath(workingDirectoryText.getText());
			workingDirectoryDialog.setText("Select folder to watch for changes"); //$NON-NLS-1$
			String path = workingDirectoryDialog.open();
			if (path != null) {
				workingDirectoryText.setText(path);
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		}));
		setControl(resComposite);
	}

	private void validateProgramPathAndURL() {
		setDirty(true);
		
		setErrorMessage(null);
		fileDecoration.hide();
		urlDecoration.hide();
		webRootDecoration.hide();

		String errorMessage = null;
		if (fileRadio.getSelection()) {
			try {
				File file = new File(VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programPathText.getText()));
				if (!file.isFile()) {
					errorMessage = Messages.RunProgramTab_error_unknownFile;
				} else if (!shortcut.canLaunch(file)) {
					errorMessage = "Not a html file"; //$NON-NLS-1$
				} else if (!file.canRead()) {
					errorMessage = Messages.RunProgramTab_error_nonReadableFile;
				}
			} catch (CoreException ex) {
				errorMessage = ex.getMessage();
			}
			
			if (errorMessage != null) {
				setErrorMessage(errorMessage);
				fileDecoration.setDescriptionText(errorMessage);
				fileDecoration.show();
			}
			
		} else if (urlRadio.getSelection()) {
			try {
				new URL(urlText.getText());
			} catch (MalformedURLException ex) {
				errorMessage = MessageFormat.format(
						Messages.RunProgramTab_error_malformedUR, 
						ex.getMessage());
				urlDecoration.setDescriptionText(errorMessage);
				urlDecoration.show();
			}				
			boolean showWebRootDecoration = false;
			if(webRootText.getText().isBlank()) {
				errorMessage = Messages.AbstractRunHTMLDebugTab_cannot_debug_without_webroot;
				showWebRootDecoration = true;
			} else {
				try {
					File file = new File(VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(webRootText.getText()));
					if (!file.exists()) {
						errorMessage = Messages.AbstractRunHTMLDebugTab_cannot_access_webroot_folder;
						showWebRootDecoration = true;
					} else if (!file.isDirectory()) {
						errorMessage = Messages.AbstractRunHTMLDebugTab_webroot_folder_is_not_a_directory;
						showWebRootDecoration = true;
					}
				} catch (CoreException e) {
					errorMessage = e.getMessage();
					showWebRootDecoration = true;
				}

				if (showWebRootDecoration) {
					webRootDecoration.setDescriptionText(errorMessage);
					webRootDecoration.show();
				}

				if (errorMessage != null) {
					setErrorMessage(errorMessage);
				}
			}
		}

		updateLaunchConfigurationDialog();
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
					.setText(configuration.getAttribute(LaunchConstants.PROGRAM, defaultSelectedFile));
			this.argumentsText.setText(configuration.getAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, "")); //$NON-NLS-1$
			this.workingDirectoryText.setText(
					configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, pathOrEmpty(getSelectedProject())));
			this.urlText.setText(configuration.getAttribute(ChromeRunDAPDebugDelegate.URL, "")); //$NON-NLS-1$
			this.webRootText.setText(configuration.getAttribute(AbstractHTMLDebugDelegate.WEBROOT, ""));
			boolean fileRadioButtonSelected = configuration.getAttribute(AbstractHTMLDebugDelegate.FILE_RADIO_BUTTON_SELECTED, true);
			if (fileRadioButtonSelected) {
				fileRadio.setSelection(true);
				urlRadio.setSelection(false);
				programPathText.setEnabled(true);
				filePath.setEnabled(true);
				urlText.setEnabled(false);
				webRootText.setEnabled(false);
				webRootProjectSelectButton.setEnabled(false);
				webRootFilesystemSelectButton.setEnabled(false);
			} else {
				fileRadio.setSelection(false);
				urlRadio.setSelection(true);
				programPathText.setEnabled(false);
				filePath.setEnabled(false);
				urlText.setEnabled(true);
				webRootText.setEnabled(true);
				webRootProjectSelectButton.setEnabled(true);
				webRootFilesystemSelectButton.setEnabled(true);
			}
			
			validateProgramPathAndURL();
			
		} catch (CoreException e) {
			ILog.get().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String programPath = this.programPathText.getText();
		configuration.setAttribute(LaunchConstants.PROGRAM, programPath);
		configuration.setAttribute(ChromeRunDAPDebugDelegate.URL, urlText.getText());
		configuration.setAttribute(AbstractHTMLDebugDelegate.WEBROOT, this.webRootText.getText());
		configuration.setAttribute(AbstractHTMLDebugDelegate.FILE_RADIO_BUTTON_SELECTED, fileRadio.getSelection());

		configuration.setAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, this.argumentsText.getText());
		String workingDirectory = this.workingDirectoryText.getText();
		configuration.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDirectory);
		configuration.setMappedResources(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new File(programPath).toURI()));
	}

	@Override
	public String getName() {
		return Messages.RunProgramTab_title;
	}

}
