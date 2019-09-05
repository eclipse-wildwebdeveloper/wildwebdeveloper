/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
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
import org.eclipse.wildwebdeveloper.Activator;

public abstract class AbstractDebugAdapterLaunchShortcut implements ILaunchShortcut2 {

	protected final String launchConfigTypeId;
	protected final String contentTypeId;

	public AbstractDebugAdapterLaunchShortcut(String launchConfigTypeId, String contentTypeId) {
		this.launchConfigTypeId = launchConfigTypeId;
		this.contentTypeId = contentTypeId;
	}

	public boolean canLaunch(File file) {
		return file.exists() && Platform.getContentTypeManager().getContentType(contentTypeId).isAssociatedWith(file.getName());
	}

	@Override public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		IResource launchableResource = getLaunchableResource(selection);
		if (launchableResource != null) {
			return getLaunchConfigurations(launchableResource.getLocation().toFile());
		}
		return getLaunchConfigurations(SelectionUtils.getFile(selection, this::canLaunch));
	}

	@Override public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		IResource launchableResource = getLaunchableResource(editorpart);
		if (launchableResource != null) {
			return getLaunchConfigurations(launchableResource.getLocation().toFile());
		}
		return getLaunchConfigurations(SelectionUtils.getFile(editorpart.getEditorInput(), this::canLaunch));
	}

	@Override public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() != 1) {
				return null;
			}
			Object firstObject = structuredSelection.getFirstElement();
			IResource resource = Adapters.adapt(firstObject, IResource.class);
			if (canLaunch(resource.getLocation().toFile())) {
				return resource;
			}
		}
		return null;
	}

	@Override public IResource getLaunchableResource(IEditorPart editorpart) {
		IEditorInput input = editorpart.getEditorInput();
		if (input instanceof FileEditorInput) {
			IFile file = ((FileEditorInput) input).getFile();
			if (canLaunch(file.getLocation().toFile())) {
				return file;
			}
		}
		return null;
	}

	@Override public void launch(ISelection selection, String mode) {
		ILaunchConfiguration[] configurations = getLaunchConfigurations(selection);
		launch(mode, configurations);
	}

	@Override public void launch(IEditorPart editor, String mode) {
		ILaunchConfiguration[] configurations = getLaunchConfigurations(editor);
		launch(mode, configurations);
	}

	private void launch(String mode, ILaunchConfiguration[] configurations) {
		if (configurations.length == 1) {
			CompletableFuture.runAsync(() -> DebugUITools.launch(configurations[0], mode));
		} else if (configurations.length > 1) {
			LaunchConfigurationSelectionDialog dialog = new LaunchConfigurationSelectionDialog(Display.getDefault().getActiveShell(), configurations);
			if (dialog.open() == IDialogConstants.OK_ID) {
				launch(mode, Arrays.asList(dialog.getResult()).toArray(new ILaunchConfiguration[dialog.getResult().length]));
			}
		}
	}

	private ILaunchConfiguration[] getLaunchConfigurations(File file) {
		if (file == null || !canLaunch(file)) {
			return new ILaunchConfiguration[0];
		}
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configType = launchManager.getLaunchConfigurationType(launchConfigTypeId);
		try {
			ILaunchConfiguration[] existing = Arrays.stream(launchManager.getLaunchConfigurations(configType))
				.filter(launchConfig -> match(launchConfig, file))
				.toArray(ILaunchConfiguration[]::new);
			if (existing.length != 0) {
				return existing;
			}
				
			String configName = launchManager.generateLaunchConfigurationName(file.getAbsolutePath());
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, configName);
			configureLaunchConfiguration(file, wc);
			return new ILaunchConfiguration[] { wc };
		} catch (CoreException e) {
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "error", e.getMessage(), e.getStatus()); //$NON-NLS-1$
			Activator.getDefault().getLog().log(e.getStatus());
		}
		return new ILaunchConfiguration[0];
	}

	/**
	 * Takes a working copy of a launch configuration and sets the default attributes according to provided file
	 * @param file
	 * @param wc
	 */
	public abstract void configureLaunchConfiguration(File file, ILaunchConfigurationWorkingCopy wc);

	/**
	 * @param launchConfig
	 * @param selectedFile
	 * @return whether the launchConfig is related to the selectedFile
	 */
	public abstract boolean match(ILaunchConfiguration launchConfig, File selectedFile);
}