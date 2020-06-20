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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.AbstractHTMLDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.chrome.ChromeRunDAPDebugDelegate;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class FirefoxRunDABDebugDelegate extends AbstractHTMLDebugDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.runFirefoxDebug"; //$NON-NLS-1$

	// see
	// https://github.com/firefox-devtools/vscode-firefox-debug/blob/master/src/adapter/configuration.ts
	// for launch/attach configuration parameters
	static final String PORT = "port"; //$NON-NLS-1$
	static final String REQUEST = "request"; //$NON-NLS-1$
	static final String PREFERENCES = "preferences"; //$NON-NLS-1$
	static final String TMP_DIRS = "tmpdirs"; //$NON-NLS-1$
	static final String TYPE = "type"; //$NON-NLS-1$
	static final String FIREFOX_EXECUTABLE = "firefoxExecutable"; //$NON-NLS-1$
	static final String PROFILE_DIR = "profileDir"; //$NON-NLS-1$
	static final String RELOAD_ON_CHANGE = "reloadOnChange"; //$NON-NLS-1$
	static final String FILE = "file"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>();
		param.put(REQUEST, "launch"); //$NON-NLS-1$
		// TODO: Let user set location of firefox executable
		param.put(FIREFOX_EXECUTABLE, findFirefoxLocation());

		// File or URL to debug 
		String url = configuration.getAttribute(ChromeRunDAPDebugDelegate.URL, "");
		if (!url.isEmpty()) {
			param.put(ChromeRunDAPDebugDelegate.URL, url);
			File projectDirectory = new File(configuration.getAttribute(AbstractHTMLDebugDelegate.CWD, ""));
			param.put("webRoot", projectDirectory.getAbsolutePath());
		} else {
			param.put(FILE, configuration.getAttribute(AbstractHTMLDebugDelegate.PROGRAM, "No program path set").trim()); //$NON-NLS-1$
		}
		param.put(PREFERENCES, "{}"); //$NON-NLS-1$
		param.put(TMP_DIRS, System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		param.put(TYPE, "firefox"); //$NON-NLS-1$
		if (configuration.getAttribute(RELOAD_ON_CHANGE, false)) {
			String workspaceDir = configuration.getAttribute(AbstractHTMLDebugDelegate.CWD, "");
			param.put(RELOAD_ON_CHANGE, workspaceDir);
		}

		File debugAdapter = findDebugAdapter();
		super.launchWithParameters(configuration, mode, launch, monitor, param, debugAdapter);
	}

	static File findDebugAdapter() {
		URL fileURL;
		try {
			fileURL = FileLocator.toFileURL(FirefoxRunDABDebugDelegate.class
					.getResource("/node_modules/firefox-debugadapter/adapter.bundle.js"));
			return new File(fileURL.toURI());
		} catch (IOException | URISyntaxException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
					"Debug error", e.getMessage(), errorStatus)); //$NON-NLS-1$
		}
		return null;

	}

	static String findFirefoxLocation() {
		String res = NodeJSManager.which("firefox").getAbsolutePath();
		if (res == null) {
			res = "/path/to/firefox";
		}
		return res;
	}

}
