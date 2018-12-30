/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class InitializeLaunchConfigurations {

	private static boolean alreadyWarned;

	public static String getVSCodeLocation(String appendPathSuffix) {
		String res = null;
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			res = "/usr/share/code";
		} else if (Platform.getOS().equals(Platform.OS_WIN32)) {
			res = "C:/Program Files (x86)/Microsoft VS Code";
		} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/Applications/Visual Studio Code.app";

			IPath path = new Path(appendPathSuffix);
			// resources/ maps to Contents/Resources on macOS
			if (path.segmentCount() > 1 && path.segment(0).equals("resources")) {
				path = path.removeFirstSegments(1);
				appendPathSuffix = new Path("/Contents/Resources").append(path).toOSString();
			}
		}
		if (res != null && new File(res).isDirectory()) {
			if (res.contains(" ") && Platform.getOS().equals(Platform.OS_WIN32)) {
				return "\"" + res + appendPathSuffix + "\"";
			}
			return res + appendPathSuffix;
		}
		return "/unknown/path/to/VSCode" + appendPathSuffix;
	}

	public static String getNodeJsLocation() {
		{
			String nodeJsLocation = System.getProperty("org.eclipse.wildwebdeveloper.nodeJSLocation");
			if (nodeJsLocation != null && Files.exists(Paths.get(nodeJsLocation))) {
				return nodeJsLocation;
			}
		}
		
		String res = "/path/to/node";
		String[] command = new String[] {"/bin/bash", "-c", "which node"};
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] {"cmd", "/c", "where node"};
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			res = reader.readLine();
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		} finally {
			IOUtils.closeQuietly(reader);
		}

		// Try default install path as last resort
		if (res == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/usr/local/bin/node";
		}

		if (res != null && Files.exists(Paths.get(res))) {
			return res;
		} else if (!alreadyWarned){
			warnNodeJSMissing();
			alreadyWarned = true;
		}
		return null;
	}

	private static void warnNodeJSMissing() {
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
					"Missing node.js",
					"Could not find node.js. This will result in editors missing key features.\n" +
					"Please make sure node.js is installed and that your PATH environement variable contains the location to the `node` executable.");
		});
	}

}
