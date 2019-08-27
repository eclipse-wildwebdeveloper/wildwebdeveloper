/*******************************************************************************
 * Copyright (c) 2018, 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *   Pierre-Yves B. - Issue #180 Wrong path to nodeDebug.js
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.firefox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;

public class FirefoxRunDABDebugDelegate extends DSPLaunchDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.firefoxDebug"; //$NON-NLS-1$

	// see https://github.com/firefox-devtools/vscode-firefox-debug/blob/master/src/adapter/configuration.ts for launch/attach configuration parameters
	static final String PORT = "port"; //$NON-NLS-1$
	static final String REQUEST = "request"; //$NON-NLS-1$
	static final String PREFERENCES = "preferences"; //$NON-NLS-1$
	static final String TMP_DIRS = "tmpdirs"; //$NON-NLS-1$
	static final String TYPE = "type"; //$NON-NLS-1$
	static final String DETACHED = "detached"; //$NON-NLS-1$
	static final String FIREFOX_EXECUTABLE = "firefoxExecutable"; //$NON-NLS-1$
	static final String PROFILE_DIR = "profileDir"; //$NON-NLS-1$
	static final String CONFIGURATION = "configuration"; //$NON-NLS-1$
	static final String RELOAD_ON_CHANGE = "reloadOnChange"; //$NON-NLS-1$
	static final String ARGUMENTS = "args"; //$NON-NLS-1$
	static final String FILE = "file"; //$NON-NLS-1$


	public static final String WORKING_DIRECTORY = "";

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		String fileToDebug = configuration.getAttribute(FILE, "").trim(); //$NON-NLS-1$
		Map<String, Object> param = new HashMap<>(); 
		param.put(REQUEST, "launch"); //$NON-NLS-1$
		param.put(FIREFOX_EXECUTABLE, findFirefoxLocation()); //$NON-NLS-1$
		//param.put(PROFILE_DIR, "/home/aobuchow/.mozilla/firefox/guoqedcl.default"); //$NON-NLS-1$
		param.put(FILE, fileToDebug); //$NON-NLS-1$
		param.put(PREFERENCES, "{}"); //$NON-NLS-1$
		param.put(TMP_DIRS, System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		param.put(TYPE, "firefox"); //$NON-NLS-1$
		param.put(DETACHED, Boolean.FALSE);
		if (configuration.getAttribute(RELOAD_ON_CHANGE, false)) {
			String workspaceDir = configuration.getAttribute(WORKING_DIRECTORY, ""); 
			param.put(RELOAD_ON_CHANGE, workspaceDir);
		}

		try {
			List<String> debugCmdArgs = Collections.singletonList(findDebugAdapter().getAbsolutePath());

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(InitializeLaunchConfigurations.getNodeJsLocation(), debugCmdArgs);
			builder.setMonitorDebugAdapter(configuration.getAttribute(DSPPlugin.ATTR_DSP_MONITOR_DEBUG_ADAPTER, false));
			builder.setDspParameters(param);

			super.launch(builder);
		} catch (IOException | URISyntaxException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus); //$NON-NLS-1$
		}
	}

	static File findDebugAdapter() throws IOException, URISyntaxException {
		URL fileURL = FileLocator.toFileURL(
				FirefoxRunDABDebugDelegate.class.getResource("/language-servers/node_modules/firefox-debugadapter/adapter.bundle.js"));
		return new File(fileURL.toURI());
	}

	private static String findFirefoxLocation() {
		String res = InitializeLaunchConfigurations.which("firefox");
		if (res != null) {
			return null;
		}
		return "/path/to/firefox";
	}

}
