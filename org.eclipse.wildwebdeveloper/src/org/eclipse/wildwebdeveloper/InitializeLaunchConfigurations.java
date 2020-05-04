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
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *   Gautier de Saint Martin Lacaze - Issue #55 Warn missing or incompatible node.js
 *   Pierre-Yves B. - Issue #196 NullPointerException when validating Node.js version
 *   Pierre-Yves B. - Issue #238 Why does wildweb do "/bin/bash -c which node" ?
 *   Pierre-Yves B. - Issue #268 Incorrect default Node.js location for macOS
 *******************************************************************************/
package org.eclipse.wildwebdeveloper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Version;

public class InitializeLaunchConfigurations {

	private static final Set<Integer> SUPPORT_NODEJS_MAJOR_VERSIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(10, 11, 12, 13, 14)));

	private static boolean alreadyWarned;
	public static String getNodeJsLocation() {
		{
			String nodeJsLocation = System.getProperty("org.eclipse.wildwebdeveloper.nodeJSLocation");
			if (nodeJsLocation != null && Files.exists(Paths.get(nodeJsLocation))) {
				validateNodeVersion(nodeJsLocation);
				return nodeJsLocation;
			}
		}

		String res = which("node");
		if (res == null) {
			if (Files.exists(Paths.get(getDefaultNodePath()))) {
				res = getDefaultNodePath();
			}
		}

		if (res != null) {
			validateNodeVersion(res);

			return res;
		} else if (!alreadyWarned) {
			warnNodeJSMissing();
			alreadyWarned = true;
		}
		return null;
	}

	public static String which(String program) {

		String[] paths = System.getenv("PATH").split(System.getProperty("path.separator"));
		for (String path : paths) {
			File exe = new File(path, program);
			if (exe.canExecute())
				return exe.getAbsolutePath();
		}

		String res = null;
		String[] command = new String[] { "/bin/bash", "-c", "-l", "which " + program};
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where " + program };
		}
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));) {
			res = reader.readLine();
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
		return res;
	}

	private static String getDefaultNodePath() {
		switch (Platform.getOS()) {
			case Platform.OS_MACOSX:
				return "/usr/local/bin/node";
			case Platform.OS_WIN32:
				return "C:\\Program Files\\nodejs\\node.exe";
			default:
				return "/usr/bin/node";
		}
	}

	private static void validateNodeVersion(String nodeJsLocation) {

		String nodeVersion = null;
		String[] nodeVersionCommand = new String[] { nodeJsLocation, "-v" };

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(nodeVersionCommand).getInputStream()));) {
			nodeVersion = reader.readLine();
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}

		if (nodeVersion == null) {
			warnNodeJSVersionCouldNotBeDetermined();
		} else {
			Version parsedVersion = Version
					.parseVersion(nodeVersion.startsWith("v") ? nodeVersion.replace("v", "") : nodeVersion);
			if (!SUPPORT_NODEJS_MAJOR_VERSIONS.contains(parsedVersion.getMajor())) {
				warnNodeJSVersionUnsupported(nodeVersion);
			}
		}
	}

	private static void warnNodeJSMissing() {
		if (!alreadyWarned) {
			Display.getDefault().asyncExec(() -> 
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Missing node.js",
						"Could not find node.js. This will result in editors missing key features.\n"
								+ "Please make sure node.js is installed and that your PATH environment variable contains the location to the `node` executable.")
			);
		}
		alreadyWarned = true;
	}

	private static void warnNodeJSVersionUnsupported(String version) {
		if (!alreadyWarned) {
			Display.getDefault().asyncExec(() ->
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Node.js " + version + " is not supported",
						"Node.js " + version + " is not supported. This will result in editors missing key features.\n"
								+ "Please make sure a supported version of node.js is installed and that your PATH environment variable contains the location to the `node` executable.\n"
								+ "Supported major versions are: " + SUPPORT_NODEJS_MAJOR_VERSIONS.stream()
										.map(String::valueOf).collect(Collectors.joining(", ")))
			);
		}
		alreadyWarned = true;
	}

	private static void warnNodeJSVersionCouldNotBeDetermined() {
		if (!alreadyWarned) {
			Display.getDefault().asyncExec(() ->
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Node.js version could not be determined",
						"Node.js version could not be determined. Please make sure a supported version of node.js is installed, editors may be missing key features otherwise.\n"
								+ "Supported major versions are: " + SUPPORT_NODEJS_MAJOR_VERSIONS.stream()
										.map(String::valueOf).collect(Collectors.joining(", ")))
			);
		}
		alreadyWarned = true;
	}

}
