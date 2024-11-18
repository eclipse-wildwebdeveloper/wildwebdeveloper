/*******************************************************************************
 * Copyright (c) 2019, 2024 Red Hat Inc. and others.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;

public class NodeJSManager {
    public static final String NODE_ROOT_DIRECTORY = ".node";

    private static final String MACOS_DSCL_SHELL_PREFIX = "UserShell: ";

    private static boolean alreadyWarned;
    private static Properties cachedNodeJsInfoProperties;
    private static final Object EXPAND_LOCK = new Object();

    /**
     * Finds Node.js executable installed in following list of locations:
     * - Location, specified in `org.eclipse.wildwebdeveloper.nodeJSLocation` system
     * property
     * - Platform Install Location
     * - Platform User Location
     * - WWD Node bundle configuration location
     * - OS dependent default installation path
     * In case of Node.js cannot be found installs the embedded version into the
     * first
     * available location of platform install/user/workspace locations
     *
     * @return The file for Node.js executable or null if it cannot be installed
     */
    public static File getNodeJsLocation() {
        String nodeJsLocation = System.getProperty("org.eclipse.wildwebdeveloper.nodeJSLocation");
        if (nodeJsLocation != null) {
            File nodejs = new File(nodeJsLocation);
            if (nodejs.exists()) {
                validateNodeVersion(nodejs);
                return new File(nodeJsLocation);
            }
        }

        Properties properties = getNodeJsInfoProperties();
        if (properties != null) {
            try {
                File nodePath = probeNodeJsExacutable(properties);
                if (nodePath != null) {
                    return nodePath;
                }

                File installationPath = probeNodeJsInstallLocationn();
                if (installationPath != null) {
                    nodePath = new File(installationPath, properties.getProperty("nodePath"));
                    synchronized (EXPAND_LOCK) {
                        if (!nodePath.exists() || !nodePath.canRead() || !nodePath.canExecute()) {
                            CompressUtils.unarchive(FileLocator.find(Activator.getDefault().getBundle(),
                                    new Path(properties.getProperty("archiveFile"))), installationPath);
                        }
                    }
                    return nodePath;
                }
            } catch (IOException e) {
                ILog.get().error(e.getMessage(), e);
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
     * Finds NPM executable installed in Node.js bundle location
     *
     * @return The file for NPM executable or null if it cannot be found
     * @since 0.2
     */
    public static File getNpmLocation() {
        String npmFileName = Platform.getOS().equals(Platform.OS_WIN32) ? "npm.cmd" : "npm";
        File nodeJsLocation = getNodeJsLocation();
        if (nodeJsLocation != null) {
            File res = new File(nodeJsLocation.getParentFile(), npmFileName);
            if (res.exists()) {
                return res;
            }
        }
        return which(npmFileName);
    }

    public static File getNpmJSLocation() {

        try {
            File npmLocation = getNpmLocation().getCanonicalFile();
            if (npmLocation.getAbsolutePath().endsWith(".js")) {
                return npmLocation;
            }
            String path = "node_modules/npm/bin/npm-cli.js";
            if (new File(npmLocation.getParentFile(), "node_modules").exists()) {
                return new File(npmLocation.getParentFile(), path);
            }
            File target = new File(npmLocation.getParentFile().getParentFile(), path);
            if (target.exists()) {
                return target;
            }

            return new File(npmLocation.getParentFile(), "lib/cli.js");
        } catch (IOException e) {
        }

        return null;

    }

    public static ProcessBuilder prepareNodeProcessBuilder(String... commands) {
        return prepareNodeProcessBuilder(Arrays.asList(commands));
    }

    public static ProcessBuilder prepareNodeProcessBuilder(List<String> commands) {
        List<String> tmp = new ArrayList<>();
        tmp.add(getNodeJsLocation().getAbsolutePath());
        tmp.addAll(commands);

        return new ProcessBuilder(tmp);
    }

    public static ProcessBuilder prepareNPMProcessBuilder(String... commands) {
        return prepareNPMProcessBuilder(Arrays.asList(commands));
    }

    public static ProcessBuilder prepareNPMProcessBuilder(List<String> commands) {
        List<String> tmp = new ArrayList<>();

        tmp.add(getNpmJSLocation().getAbsolutePath());
        tmp.addAll(commands);

        return prepareNodeProcessBuilder(tmp);
    }

    public static File which(String program) {
        Properties properties = getNodeJsInfoProperties();
        if (properties != null) {
            File nodePath = probeNodeJsExacutable(properties);
            if (nodePath != null && nodePath.exists() && nodePath.canRead() && nodePath.canExecute()) {
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
        try (BufferedReader reader = Runtime.getRuntime().exec(command).inputReader()) {
            res = reader.readLine();
        } catch (IOException e) {
            ILog.get().error(e.getMessage(), e);
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
                    ILog.get().error(e.getMessage(), e);
                }
            }
        }
        return cachedNodeJsInfoProperties;
    }

    private static final File probeNodeJsInstallLocationn() {
        File[] nodeJsLocations = getOrderedInstallationLocations();
        for (File installationPath : nodeJsLocations) {
            if (probeDirectoryForInstallation(installationPath)) {
                return installationPath;
            }
        }
        return null;
    }

    private static final boolean probeDirectoryForInstallation(File directory) {
        if (directory == null) {
            return false;
        }
        if (directory.exists() && directory.isDirectory()
                && directory.canWrite() && directory.canExecute()) {
            return true;
        }
        return probeDirectoryForInstallation(directory.getParentFile());
    }

    private static final File probeNodeJsExacutable(Properties properties) {
        File[] nodeJsLocations = getOrderedInstallationLocations();
        for (File installationPath : nodeJsLocations) {
            File nodePath = getNodeJsExecutablen(installationPath, properties);
            if (nodePath != null) {
                return nodePath;
            }
        }
        return null;
    }

    private static final File[] getOrderedInstallationLocations() {
        return new File[] {
                toFile(Platform.getInstallLocation(), NODE_ROOT_DIRECTORY), // Platform Install Location
                toFile(Platform.getUserLocation(), NODE_ROOT_DIRECTORY), // Platform User Location
                toFile(Platform.getStateLocation(Activator.getDefault().getBundle())) // Default
        };
    }

    private static final File toFile(Location location, String binDirectory) {
        File installLocation = location != null && location.getURL() != null ? new File(location.getURL().getFile())
                : null;
        if (installLocation != null && binDirectory != null) {
            installLocation = new File(installLocation, binDirectory);
        }
        return installLocation;
    }

    private static final File toFile(IPath locationPath) {
        return locationPath != null ? locationPath.toFile() : null;
    }

    private static final File getNodeJsExecutablen(File installationLocation, Properties properties) {
        if (installationLocation != null) {
            File nodePath = new File(installationLocation, properties.getProperty("nodePath"));
            if (nodePath.exists() && nodePath.canRead() && nodePath.canExecute()) {
                return nodePath;
            }
        }
        return null;
    }

    private static String getDefaultShellMacOS() {
        String res = null;
        String[] command = { "/bin/bash", "-c", "-l", "dscl . -read ~/ UserShell" };
        try (BufferedReader reader = Runtime.getRuntime().exec(command).inputReader()) {
            res = reader.readLine();
            if (!res.startsWith(MACOS_DSCL_SHELL_PREFIX)) {
                ILog.get().error("Cannot find default shell. Use '/bin/zsh' instead.");
                return "/bin/zsh"; // Default shell since macOS 10.15
            }
            res = res.substring(MACOS_DSCL_SHELL_PREFIX.length());
        } catch (IOException e) {
            ILog.get().error(e.getMessage(), e);
        }
        return res;
    }

    private static File getDefaultNodePath() {
        return new File(switch (Platform.getOS()) {
            case Platform.OS_MACOSX -> "/usr/local/bin/node";
            case Platform.OS_WIN32 -> "C:\\Program Files\\nodejs\\node.exe";
            default -> "/usr/bin/node";
        });
    }

    private static void validateNodeVersion(File nodeJsLocation) {
        String nodeVersion = null;
        String[] nodeVersionCommand = { nodeJsLocation.getAbsolutePath(), "-v" };

        try (BufferedReader reader = Runtime.getRuntime().exec(nodeVersionCommand).inputReader()) {
            nodeVersion = reader.readLine();
        } catch (IOException e) {
            ILog.get().error(e.getMessage(), e);
        }

        if (nodeVersion == null) {
            warnNodeJSVersionCouldNotBeDetermined();
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

    private static void warnNodeJSVersionCouldNotBeDetermined() {
        if (!alreadyWarned) {
            Display.getDefault().asyncExec(() -> MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                    "Node.js version could not be determined",
                    "Node.js version could not be determined. Please make sure a recent version of node.js is installed, editors may be missing key features otherwise.\n"));
        }
        alreadyWarned = true;
    }
}
