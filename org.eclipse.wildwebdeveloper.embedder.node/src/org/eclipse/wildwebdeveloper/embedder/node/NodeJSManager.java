/*******************************************************************************
 * Copyright (c) 2019, 2021 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.embedder.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Version;

@SuppressWarnings("restriction")
public class NodeJSManager {

	private static final String MACOS_DSCL_SHELL_PREFIX = "UserShell: ";

	private static final Set<Integer> SUPPORT_NODEJS_MAJOR_VERSIONS = Set.of(10, 11, 12, 13, 14);

	private static boolean alreadyWarned;
	private static Properties cachedNodeJsInfoProperties;
	private static final Object EXPAND_LOCK = new Object();

	public static File getNodeJsLocation() {
		{
			String nodeJsLocation = System.getProperty("org.eclipse.wildwebdeveloper.nodeJSLocation");
			if (nodeJsLocation != null) {
				File nodejs = new File(nodeJsLocation);
				if (nodejs.exists()) {
					validateNodeVersion(nodejs);
					return new File(nodeJsLocation);
				}
			}
		}

		Properties properties = getNodeJsInfoProperties();
		if (properties != null) {
			try {
				IPath stateLocationPath = Platform.getStateLocation(Activator.getDefault().getBundle());
				if (stateLocationPath != null) {
					File installationPath = stateLocationPath.toFile();
					File nodePath = new File(installationPath, properties.getProperty("nodePath"));
					synchronized (EXPAND_LOCK) {
						if (!nodePath.exists() || !nodePath.canRead() || !nodePath.canExecute()) {
							CompressUtils.unarchive(FileLocator.find(Activator.getDefault().getBundle(),
									new Path(properties.getProperty("archiveFile"))), installationPath);
						}
					}
					return nodePath;
				}
			} catch (IOException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}
		}

		File res = which("node");
		if (res == null && getDefaultNodePath().exists()) {
			res = getDefaultNodePath();
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

	/**
	 *
	 * @return
	 * @since 0.2
	 */
	public static File getNpmLocation() {
		String npmFileName = Platform.getOS().equals(Platform.OS_WIN32) ? "npm.cmd" : "npm";
		File nodeJsLocation = getNodeJsLocation();
		if (nodeJsLocation != null) {
			File res = new File(nodeJsLocation.getParentFile(), npmFileName);
			if (res.canExecute()) {
				return res;
			}
		}
		return which(npmFileName);
	}

	public static File which(String program) {
		Properties properties = getNodeJsInfoProperties();
		if (properties != null) {
			IPath stateLocationPath = InternalPlatform.getDefault()
					.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));
			if (stateLocationPath != null) {
				File installationPath = stateLocationPath.toFile();
				File nodePath = new File(installationPath, properties.getProperty("nodePath"));
				if (nodePath.exists() && nodePath.canRead() && nodePath.canExecute()) {
					File exe = new File(nodePath.getParent(), program);
					if (exe.canExecute()) {
						return exe;
					} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
						exe = new File(nodePath.getParent(), program + ".exe");
						if (exe.canExecute()) {
							return exe;
						}
					}
				}
			}
		}

		String[] paths = System.getenv("PATH").split(System.getProperty("path.separator"));
		for (String path : paths) {
			File exe = new File(path, program);
			if (exe.canExecute())
				return exe;
		}

		String res = null;
		String[] command = new String[] { "/bin/bash", "-c", "-l", "which " + program };
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where " + program };
		} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			command = new String[] { getDefaultShellMacOS(), "-c", "-li", "which " + program };
		}
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));) {
			res = reader.readLine();
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
		return res != null ? new File(res) : null;
	}

	private static Properties getNodeJsInfoProperties() {
		if (cachedNodeJsInfoProperties == null) {
			URL nodeJsInfo = FileLocator.find(Activator.getDefault().getBundle(), new Path("nodejs-info.properties"));
			if (nodeJsInfo != null) {
				try (InputStream infoStream = nodeJsInfo.openStream()) {
					Properties properties = new Properties();
					properties.load(infoStream);
					cachedNodeJsInfoProperties = properties;
				} catch (IOException e) {
					Activator.getDefault().getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return cachedNodeJsInfoProperties;
	}

	private static String getDefaultShellMacOS() {
		String res = null;
		String[] command = new String[] { "/bin/bash", "-c", "-l", "dscl . -read ~/ UserShell" };
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));) {
			res = reader.readLine();
			if (!res.startsWith(MACOS_DSCL_SHELL_PREFIX)) {
				Activator.getDefault().getLog()
						.log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
								"Cannot find default shell. Use '/bin/zsh' instead."));
				return "/bin/zsh"; // Default shell since macOS 10.15
			}
			res = res.substring(MACOS_DSCL_SHELL_PREFIX.length());
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
		return res;
	}

	private static File getDefaultNodePath() {
		return new File(switch (Platform.getOS()) {
			case Platform.OS_MACOSX -> "/usr/local/bin/node";
			case Platform.OS_WIN32 -> "C:\\Program Files\\nodejs\\node.exe";
			default ->"/usr/bin/node";
		});
	}

	private static void validateNodeVersion(File nodeJsLocation) {
		String nodeVersion = null;
		String[] nodeVersionCommand = new String[] { nodeJsLocation.getAbsolutePath(), "-v" };

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
			Display.getDefault().asyncExec(() -> MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
					"Missing node.js", "Could not find node.js. This will result in editors missing key features.\n"
							+ "Please make sure node.js is installed and that your PATH environment variable contains the location to the `node` executable."));
		}
		alreadyWarned = true;
	}

	private static void warnNodeJSVersionUnsupported(String version) {
		if (!alreadyWarned) {
			Display.getDefault().asyncExec(() -> MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
					"Node.js " + version + " is not supported",
					"Node.js " + version + " is not supported. This will result in editors missing key features.\n"
							+ "Please make sure a supported version of node.js is installed and that your PATH environment variable contains the location to the `node` executable.\n"
							+ "Supported major versions are: " + SUPPORT_NODEJS_MAJOR_VERSIONS.stream()
									.map(String::valueOf).collect(Collectors.joining(", "))));
		}
		alreadyWarned = true;
	}

	private static void warnNodeJSVersionCouldNotBeDetermined() {
		if (!alreadyWarned) {
			Display.getDefault().asyncExec(() -> MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
					"Node.js version could not be determined",
					"Node.js version could not be determined. Please make sure a supported version of node.js is installed, editors may be missing key features otherwise.\n"
							+ "Supported major versions are: " + SUPPORT_NODEJS_MAJOR_VERSIONS.stream()
									.map(String::valueOf).collect(Collectors.joining(", "))));
		}
		alreadyWarned = true;
	}

}
