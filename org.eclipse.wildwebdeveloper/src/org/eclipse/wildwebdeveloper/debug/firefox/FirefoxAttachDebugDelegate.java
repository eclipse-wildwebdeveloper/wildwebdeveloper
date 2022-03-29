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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wildwebdeveloper.debug.AbstractHTMLDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;

public class FirefoxAttachDebugDelegate extends AbstractHTMLDebugDelegate {
	
	static final String ID = "org.eclipse.wildwebdeveloper.firefoxDebug"; //$NON-NLS-1$
	
	private static final String REQUEST = "request"; //$NON-NLS-1$

	// see https://github.com/firefox-devtools/vscode-firefox-debug/blob/master/src/adapter/configuration.ts for launch/attach configuration parameters
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>(); 
		param.put(REQUEST, "attach"); //$NON-NLS-1$
		int port = configuration.getAttribute(LaunchConstants.PORT, 4711);
		param.put(LaunchConstants.PORT, port);
		
		super.launchWithParameters(configuration, mode, launch, monitor, param, FirefoxRunDABDebugDelegate.findDebugAdapter());
	}

}
