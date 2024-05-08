/*******************************************************************************
 * Copyright (c) 2020, 2022 Red Hat Inc. and others.
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class NpmLaunchDelegate implements ILaunchConfigurationDelegate {

	public static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.NPMLaunch"; //$NON-NLS-1$
	public static final String ARGUMENTS = "runtimeArgs";

	private MessageConsole console;

	public NpmLaunchDelegate() {
		console = new MessageConsole("NPM output", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		File packageJSONDirectory = new File(
				VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, "No package.json directory path set").trim())); //$NON-NLS-1$
		File packageJSON = new File(
				VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(configuration.getAttribute(LaunchConstants.PROGRAM, "No package.json path set").trim())); //$NON-NLS-1$
		final String argumentString = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(configuration.getAttribute(ARGUMENTS, "No NPM argument set") //$NON-NLS-1$
				.trim());
		List<String> arguments = new ArrayList<>();
		arguments.addAll(NodeJSManager.prepareNPMProcessBuilder().command());
		arguments.addAll(Arrays.asList(argumentString.split(" "))); //$NON-NLS-1$
		monitor.beginTask(argumentString + ' ' + packageJSON.getAbsolutePath(), 2);
		monitor.worked(1);
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(packageJSONDirectory.getName());
		try {
			ProcessBuilder pb = new ProcessBuilder(arguments).directory(packageJSONDirectory);
			Map<String, String> envp = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, String>) null);
			if (envp != null && !envp.isEmpty()) {
				Map<String, String> env = pb.environment();
				envp.entrySet().forEach(e -> {
					String value = e.getValue();
					try {
						value = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(value);
					} catch (CoreException ex) {
						IStatus errorStatus = Status.error(ex.getMessage(), ex);
						ILog.get().log(errorStatus);
					}
					env.put(e.getKey(), value);
				});
			}
			
			final Process npmProcess = pb.start();
			DebugPlugin.newProcess(launch, npmProcess, argumentString);
			CompletableFuture.runAsync(() -> {
				try {
					npmProcess.waitFor();
				} catch (InterruptedException e) {
					IStatus errorStatus = Status.error(e.getMessage(), e);
					ILog.get().log(errorStatus);
					Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
							Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
				}
			}).whenComplete((ok, ko) -> {
				if (project != null) {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
					} catch (CoreException e) {
						IStatus errorStatus = Status.error(e.getMessage(), e);
						ILog.get().log(errorStatus);
						Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
								Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
					}
				}
				monitor.done();
			});
		} catch (IOException e) {
			IStatus errorStatus = Status.error(e.getMessage(), e);
			ILog.get().log(errorStatus);
			Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
					Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
		}
	}

}
