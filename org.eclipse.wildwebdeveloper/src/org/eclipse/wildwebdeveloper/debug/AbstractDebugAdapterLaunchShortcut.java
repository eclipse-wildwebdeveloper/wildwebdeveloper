/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Pierre-Yves B. - Issue #309 Launch called from the wrong thread
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationSelectionDialog;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

public abstract class AbstractDebugAdapterLaunchShortcut implements ILaunchShortcut2 {

	private final String[] contentTypeIds;
	private final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
	private final ILaunchConfigurationType configType;
	private final boolean autoStartNewlyCreatedConfiguration;

	protected AbstractDebugAdapterLaunchShortcut(String launchConfigTypeId, String contentTypeId, boolean autoStartNewlyCreatedConfiguration) {
		this(launchConfigTypeId, new String[] {contentTypeId}, autoStartNewlyCreatedConfiguration);
	}

	protected AbstractDebugAdapterLaunchShortcut(String launchConfigTypeId, String[] contentTypeIds, boolean autoStartNewlyCreatedConfiguration) {
		this.autoStartNewlyCreatedConfiguration = autoStartNewlyCreatedConfiguration;
		this.contentTypeIds = contentTypeIds;
		this.configType = launchManager.getLaunchConfigurationType(launchConfigTypeId);
	}

	public boolean canLaunch(File file) {
		return file.exists() &&
			Arrays.stream(contentTypeIds).map(Platform.getContentTypeManager()::getContentType)
				.anyMatch(type -> type.isAssociatedWith(file.getName()));
	}

	public boolean canLaunchResource(IResource resource) {
		int resourceType = resource.getType();
		if (resourceType == IResource.FILE) {
			File file = resource.getLocation().toFile();
			return canLaunch(file);
		}
		return false;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		IResource launchableResource = getLaunchableResource(selection);
		if (launchableResource != null) {
			return getLaunchConfigurations(launchableResource.getLocation().toFile());
		}
		return getLaunchConfigurations(SelectionUtils.getFile(selection, this::canLaunch));
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		IResource launchableResource = getLaunchableResource(editorpart);
		if (launchableResource != null) {
			return getLaunchConfigurations(launchableResource.getLocation().toFile());
		}
		return getLaunchConfigurations(SelectionUtils.getFile(editorpart.getEditorInput(), this::canLaunch));
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection structuredSelection) {
			if (structuredSelection.size() != 1) {
				return null;
			}
			Object firstObject = structuredSelection.getFirstElement();
			IResource resource = Adapters.adapt(firstObject, IResource.class);
			int resourceType = resource.getType();
			if (resourceType == IResource.FILE) {
				if (canLaunch(resource.getLocation().toFile())) {
					return resource;
				}
			} else if (resourceType == IResource.PROJECT || resourceType == IResource.FOLDER) {
				return getLaunchableResource(Adapters.adapt(resource, IContainer.class));
			}
		}
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		IEditorInput input = editorpart.getEditorInput();
		if (input instanceof FileEditorInput) {
			IFile file = ((FileEditorInput) input).getFile();
			if (canLaunch(file.getLocation().toFile())) {
				return file;
			}
		}
		return null;
	}

	/**
	 * Returns a resource we can launch from the container shortcut
	 * @param container
	 * @return a resource that can be run from this container. Can be <code>null</code>.
	 */
	protected abstract IResource getLaunchableResource(IContainer container);

	@Override
	public void launch(ISelection selection, String mode) {
		launch(mode, getLaunchConfigurations(selection), SelectionUtils.getFile(selection, this::canLaunch));
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		launch(mode, getLaunchConfigurations(editor), SelectionUtils.getFile(editor.getEditorInput(), this::canLaunch));
	}

	private void launch(String mode, ILaunchConfiguration[] configurations, File launchableFile) {
		if (configurations == null) {
			// TODO honor contract of ILaunchShortcut2.getLaunchConfigurations
			return;
		} else if (configurations.length == 0 && launchableFile != null && launchableFile.exists()) {
			ILaunchConfiguration configuration;
			try {
				configuration = createNewLaunchConfiguration(launchableFile);
				Display.getDefault().asyncExec(() -> {
					if (autoStartNewlyCreatedConfiguration) {
						DebugUITools.launch(configuration, mode);
					} else {
						if (DebugUIPlugin.openLaunchConfigurationEditDialog(Display.getCurrent().getActiveShell(), configuration, DebugUITools.getLaunchGroup(configuration, mode).getIdentifier(), null, true) == IDialogConstants.OK_ID) {
							DebugUITools.launch(configuration, mode);	
						}
					}
				});
			} catch (CoreException e) {
				ILog.get().error(e.getMessage(), e);
			}
		} else if (configurations.length == 1) {
			Display.getDefault().asyncExec(() -> DebugUITools.launch(configurations[0], mode));
		} else if (configurations.length > 1) {
			LaunchConfigurationSelectionDialog dialog = new LaunchConfigurationSelectionDialog(
					Display.getDefault().getActiveShell(), configurations);
			if (dialog.open() == IDialogConstants.OK_ID) {
				launch(mode,
						Arrays.asList(dialog.getResult()).toArray(new ILaunchConfiguration[dialog.getResult().length]), launchableFile);
			}
		}
	}

	/**
	 * See {@link ILaunchShortcut2#getLaunchConfigurations(ISelection)} for contract.
	 * @param file
	 * @return
	 */
	private ILaunchConfiguration[] getLaunchConfigurations(File file) {
		if (file == null || !canLaunch(file)) {
			return null;
		}
		try {
			ILaunchConfiguration[] existing = Arrays.stream(launchManager.getLaunchConfigurations(configType))
					.filter(launchConfig -> match(launchConfig, file)).toArray(ILaunchConfiguration[]::new);
			if (existing.length != 0) {
				return existing;
			}
			return new ILaunchConfiguration[0];
		} catch (CoreException e) {
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "error", e.getMessage(), e.getStatus()); //$NON-NLS-1$
			ILog.get().log(e.getStatus());
		}
		return new ILaunchConfiguration[0];
	}

	private ILaunchConfigurationWorkingCopy createNewLaunchConfiguration(File file) throws CoreException {
		String configName = launchManager.generateLaunchConfigurationName(file.getAbsolutePath());
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, configName);
		wc.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, file.getParentFile().getAbsolutePath());
		wc.setMappedResources(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI()));
		configureLaunchConfiguration(file, wc);
		return wc;
	}

	/**
	 * Takes a working copy of a launch configuration and sets the default
	 * attributes according to provided file
	 * 
	 * @param file
	 * @param wc
	 */
	protected void configureLaunchConfiguration(File file, ILaunchConfigurationWorkingCopy wc) {
		IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
		wc.setAttribute(LaunchConstants.PROGRAM, iFile == null ? file.getAbsolutePath() : "${workspace_loc:" + iFile.getFullPath() + "}");
		wc.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, file.getParentFile().getAbsolutePath());
	}

	/**
	 * @param launchConfig
	 * @param selectedFile
	 * @return whether the launchConfig is related to the selectedFile
	 */
	private boolean match(ILaunchConfiguration launchConfig, File selectedFile) {
		try {
			String program = launchConfig.getAttribute(LaunchConstants.PROGRAM, "");
			Set<String> validValues = new HashSet<>();
			validValues.add(selectedFile.getAbsolutePath());
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(selectedFile.getAbsolutePath()));
			if (iFile != null) {
				validValues.add("${workspace_loc:" + iFile.getFullPath() + '}');
				validValues.add("${workspace_loc:" + iFile.getProject().getName() + "}/" + iFile.getProjectRelativePath());
				validValues.add("${workspace_loc:/" + iFile.getProject().getName() + "}/" + iFile.getProjectRelativePath());
				// we can actually also include variations for each segment, although it's not usual
			}
			return validValues.contains(program);
		} catch (CoreException e) {
			ILog.get().log(e.getStatus());
			return false;
		}
	}

}