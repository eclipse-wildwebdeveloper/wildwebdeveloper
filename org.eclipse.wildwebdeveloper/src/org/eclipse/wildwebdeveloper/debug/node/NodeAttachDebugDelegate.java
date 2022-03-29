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
package org.eclipse.wildwebdeveloper.debug.node;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class NodeAttachDebugDelegate extends DSPLaunchDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.nodeDebugAttach"; //$NON-NLS-1$

	// see https://github.com/Microsoft/vscode-node-debug/blob/master/src/node/nodeDebug.ts LaunchRequestArguments
	static final String ADDRESS = "address"; //$NON-NLS-1$
	static final String LOCAL_ROOT = "localRoot"; //$NON-NLS-1$
	static final String REMOTE_ROOT = "remoteRoot"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>();
		param.put(ADDRESS, configuration.getAttribute(ADDRESS, "no address defined")); //$NON-NLS-1$
		param.put(LaunchConstants.PORT, configuration.getAttribute(LaunchConstants.PORT, -1));
		if (configuration.hasAttribute(LOCAL_ROOT)) {
			param.put(LOCAL_ROOT, VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(configuration.getAttribute(LOCAL_ROOT, "")));
		}
		if (configuration.hasAttribute(REMOTE_ROOT)) {
			param.put(REMOTE_ROOT, configuration.getAttribute(REMOTE_ROOT, ""));
		}
		try {
			URL fileURL = FileLocator.toFileURL(
					getClass().getResource("/node_modules/node-debug2/out/src/nodeDebug.js"));
			File file = new File(fileURL.getPath());
			List<String> debugCmdArgs = Collections.singletonList(file.getAbsolutePath());

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(NodeJSManager.getNodeJsLocation().getAbsolutePath(), debugCmdArgs);
			builder.setMonitorDebugAdapter(configuration.getAttribute(DSPPlugin.ATTR_DSP_MONITOR_DEBUG_ADAPTER, false));
			builder.setDspParameters(param);

			super.launch(builder);
		} catch (IOException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus)); //$NON-NLS-1$
		}

	}

}
