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
 *   Pierre-Yves B. - Issue #180 Wrong path to nodeDebug.js
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.chrome;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.node.NodeAttachDebugDelegate;

public class ChromeAttachDebugDelegate extends AbstractDebugDelegate {
	
	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.chromeRunDebug"; //$NON-NLS-1$

	// see https://github.com/Microsoft/vscode-node-debug/blob/master/src/node/nodeDebug.ts LaunchRequestArguments
	static final String ADDRESS = "address"; //$NON-NLS-1$
	static final String PORT = "port"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>();
		param.put(ADDRESS, configuration.getAttribute(ADDRESS, "no address defined")); //$NON-NLS-1$
		param.put(PORT, configuration.getAttribute(NodeAttachDebugDelegate.PORT, 9229));
		param.put("webRoot", "/home/aobuchow/Documents/Firefox Debugger Testing");
		param.put("url", "https://www.google.ca/");
		param.put("runtimeExecutable", "/usr/bin/chromium-browser");
		
		super.launchWithParameters(configuration, mode, launch, monitor, param, ChromeRunDAPDebugDelegate.findDebugAdapter());

	}

}
