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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;
import org.eclipse.wildwebdeveloper.debug.AbstractHTMLDebugDelegate;

public class NpmLaunchDelegate implements ILaunchConfigurationDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.NPMLaunch"; //$NON-NLS-1$
	private MessageConsole console;

	public NpmLaunchDelegate() {
		console = new MessageConsole("NPM output", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		File packageJSONDirectory = new File(
				configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, "No package.json directory path set").trim()); //$NON-NLS-1$
		File packageJSON = new File(
				configuration.getAttribute(AbstractHTMLDebugDelegate.PROGRAM, "No package.json path set").trim()); //$NON-NLS-1$
		final String argumentString = configuration.getAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, "No NPM argument set") //$NON-NLS-1$
				.trim();
		List<String> arguments = new ArrayList<>();
		arguments.add(findNPMLocation());
		arguments.addAll(Arrays.asList(argumentString.split(" "))); //$NON-NLS-1$
		monitor.beginTask(argumentString + ' ' + packageJSON.getAbsolutePath(), 2);
		monitor.worked(1);
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(packageJSONDirectory.getName());
		try {
			final Process npmProcess = new ProcessBuilder(arguments).directory(packageJSONDirectory).start();
			DebugPlugin.newProcess(launch, npmProcess, argumentString);
			CompletableFuture.runAsync(() -> {
				try {
					npmProcess.waitFor();
				} catch (InterruptedException e) {
					IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
					Activator.getDefault().getLog().log(errorStatus);
					Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
							Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
				}
			}).whenComplete((ok, ko) -> {
				if (project != null) {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
					} catch (CoreException e) {
						IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
						Activator.getDefault().getLog().log(errorStatus);
						Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
								Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
					}
				}
				monitor.done();
			});
		} catch (IOException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
					Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
		}
	}

	private static String findNPMLocation() {
		return InitializeLaunchConfigurations.which("npm"); //$NON-NLS-1$
	}

}
