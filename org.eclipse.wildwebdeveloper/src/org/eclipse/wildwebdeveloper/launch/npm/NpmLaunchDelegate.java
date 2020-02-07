/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.launch.npm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
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
				configuration.getAttribute(AbstractHTMLDebugDelegate.CWD, "No package.json directory path set").trim()); //$NON-NLS-1$
		File packageJSON = new File(
				configuration.getAttribute(AbstractHTMLDebugDelegate.PROGRAM, "No package.json path set").trim()); //$NON-NLS-1$
		String argumentString = configuration.getAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, "No NPM argument set") //$NON-NLS-1$
				.trim();
		List<String> arguments = new ArrayList<>();
		arguments.add(findNPMLocation());
		arguments.addAll(Arrays.asList(argumentString.split(" "))); //$NON-NLS-1$
		monitor.beginTask(Messages.NpmLaunchDelegate_npmInstallFor + packageJSON.getAbsolutePath(), 2);
		monitor.worked(1);

		Display.getDefault().asyncExec(() -> {
			try {
				Process dependencyInstaller = new ProcessBuilder(arguments).directory(packageJSONDirectory).start();
				dependencyInstaller.waitFor();
				String operationOutput = convertInputStreamToString(dependencyInstaller.getInputStream());
				String errorOutput = convertInputStreamToString(dependencyInstaller.getErrorStream());
				if (!operationOutput.equals("")) { //$NON-NLS-1$
					Activator.getDefault().getLog().log(
							new Status(1, Activator.PLUGIN_ID, Messages.NpmLaunchDelegate_npmOutput + operationOutput));
					outputToConsole(operationOutput);
				}
				if (!errorOutput.equals("")) { //$NON-NLS-1$
					Activator.getDefault().getLog()
							.log(new Status(2, Activator.PLUGIN_ID, Messages.NpmLaunchDelegate_npmError + errorOutput));
					outputToConsole(errorOutput);
				}

				// Refresh project hierarchy to show changes
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(packageJSONDirectory.getName());
				if (project != null) {
					project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				}
				monitor.worked(1);
			} catch (InterruptedException | IOException | CoreException e) {
				IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
				Activator.getDefault().getLog().log(errorStatus);
				Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
						Messages.NpmLaunchDelegate_npmError, e.getMessage(), errorStatus)); // $NON-NLS-1$
			}
			monitor.done();
		});

	}

	private void outputToConsole(String outputText) {
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
		try (MessageConsoleStream stream = console.newMessageStream()) {
			stream.write(outputText);
		} catch (IOException e1) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage(), e1));
		}
	}

	private static String convertInputStreamToString(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(StandardCharsets.UTF_8.name());
	}

	private static String findNPMLocation() {
		return InitializeLaunchConfigurations.which("npm"); //$NON-NLS-1$
	}

}
