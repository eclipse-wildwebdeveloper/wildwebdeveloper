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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.wildwebdeveloper.debug.node.NodeAttachDebugDelegate;

public class FirefoxAttachDebugDelegate extends DSPLaunchDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.nodeDebug"; //$NON-NLS-1$

	// see https://github.com/firefox-devtools/vscode-firefox-debug/blob/master/src/adapter/configuration.ts for launch/attach configuration parameters
	static final String PORT = "port"; //$NON-NLS-1$
	static final String REQUEST = "request"; //$NON-NLS-1$


	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>(); 
		param.put(REQUEST, "attach"); //$NON-NLS-1$
		int port = configuration.getAttribute(NodeAttachDebugDelegate.PORT, 4711);
		param.put(PORT, port);

		try {
			List<String> debugCmdArgs = Collections.singletonList(FirefoxRunDABDebugDelegate.findDebugAdapter().getAbsolutePath());

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

}
