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
package org.eclipse.wildwebdeveloper.debug.firefox;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
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

public class FirefoxRunDebugLaunchShortcut implements ILaunchShortcut2 {

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return getLaunchConfigurations(getLaunchableResource(selection));
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return getLaunchConfigurations(getLaunchableResource(editorpart));
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() != 1) {
				return null;
			}
			Object firstObject = structuredSelection.getFirstElement();
			IResource resource = Adapters.adapt(firstObject, IResource.class);
			return resource;
		}
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		IEditorInput input = editorpart.getEditorInput();
		if (input instanceof FileEditorInput) {
			return ((FileEditorInput) input).getFile();
		} /* else if (input instanceof ...) */
		return null;
	}

	@Override
	public void launch(ISelection selection, String mode) {
		ILaunchConfiguration[] configurations = getLaunchConfigurations(selection);
		launch(mode, configurations);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
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


	private ILaunchConfiguration[] getLaunchConfigurations(IResource resource) {
		if (resource == null || !resource.isAccessible()) {
			return new ILaunchConfiguration[0];
		}
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configType = launchManager
				.getLaunchConfigurationType(FirefoxRunDABDebugDelegate.ID);
		try {
			ILaunchConfiguration[] existing = Arrays.stream(launchManager.getLaunchConfigurations(configType))
				.filter(launchConfig -> {
					try {
						return launchConfig.getAttribute(FirefoxRunDABDebugDelegate.FILE, "").equals(resource.getLocation().toFile().toString()); //$NON-NLS-1$
					} catch (CoreException e) {
						Activator.getDefault().getLog().log(e.getStatus());
						return false;
					}
				}).toArray(ILaunchConfiguration[]::new);
			if (existing.length != 0) {
				return existing;
			}
				
			String configName = launchManager.generateLaunchConfigurationName(resource.getLocation().toString());
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, configName);
			wc.setAttribute(FirefoxRunDABDebugDelegate.FILE, resource.getLocation().toString());
			wc.setAttribute(FirefoxRunDABDebugDelegate.WORKING_DIRECTORY, resource.getLocation().removeLastSegments(1).toString());
			return new ILaunchConfiguration[] { wc };
		} catch (CoreException e) {
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "error", e.getMessage(), e.getStatus()); //$NON-NLS-1$
			Activator.getDefault().getLog().log(e.getStatus());
		}
		return null;
	}
}
