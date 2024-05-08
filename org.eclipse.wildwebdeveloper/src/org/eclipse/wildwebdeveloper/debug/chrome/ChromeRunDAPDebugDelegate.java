/*******************************************************************************
 * Copyright (c) 2020, 2021 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.debug.chrome;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.internal.browser.BrowserManager;
import org.eclipse.ui.internal.browser.IBrowserDescriptor;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.node.VSCodeJSDebugDelegate;

public class ChromeRunDAPDebugDelegate extends VSCodeJSDebugDelegate {
	
	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.chromeRunDebug"; //$NON-NLS-1$

	public static final String URL = "url";

	public ChromeRunDAPDebugDelegate() {
		super("pwa-chrome");
	}

	@Override
	protected boolean configureAdditionalParameters(ILaunchConfiguration config, Map<String, Object> param) throws CoreException {
		if (super.configureAdditionalParameters(config, param)) {
			String program = (String)param.remove(LaunchConstants.PROGRAM);
			param.put("file", program);
			return true;
		}
		return false;
	}

	@Override
	public File computeRuntimeExecutable(ILaunchConfiguration configuration) {
		String res = ""; //$NON-NLS-1$
		try {
			res = configuration.getAttribute(RUNTIME_EXECUTABLE, res);
		} catch (CoreException e) {
			ILog.get().error(e.getMessage(), e);
		}
		File executable = new File(res);
		if (executable.isAbsolute() && executable.canExecute()) {
			return executable;
		}
		return BrowserManager.getInstance().getWebBrowsers().stream().filter(ChromeExecutableTab::isChrome).findAny().map(IBrowserDescriptor::getLocation).map(File::new).orElse(null);
	}
}
