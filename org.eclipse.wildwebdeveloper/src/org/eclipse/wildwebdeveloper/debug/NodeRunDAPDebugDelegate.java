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
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;

import com.google.gson.JsonObject;

public class NodeRunDAPDebugDelegate extends DSPLaunchDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.nodeDebug"; //$NON-NLS-1$

	// see https://github.com/Microsoft/vscode-node-debug/blob/master/src/node/nodeDebug.ts LaunchRequestArguments
	static final String PROGRAM = "program"; //$NON-NLS-1$
	static final String ARGUMENTS = "args"; //$NON-NLS-1$
	private static final String CWD = "cwd"; //$NON-NLS-1$
	private static final String ENV = "env"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>();
		param.put(PROGRAM, configuration.getAttribute(PROGRAM, "no program path defined")); //$NON-NLS-1$
		String argsString = configuration.getAttribute(ARGUMENTS, "").trim(); //$NON-NLS-1$
		if (!argsString.isEmpty()) {
			Object[] args = Arrays.asList(argsString.split(" ")).stream() //$NON-NLS-1$
					.filter(s -> !s.trim().isEmpty()).toArray();
			if (args.length > 0) {
				param.put(ARGUMENTS, args);
			}
		}
		Map<String, String> env = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
				Collections.emptyMap());
		if (!env.isEmpty()) {
			JsonObject envJson = new JsonObject();
			env.forEach((key, value) -> envJson.addProperty(key, value));
			param.put(ENV, envJson);
		}
		String cwd = configuration.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, "").trim(); //$NON-NLS-1$
		if (!cwd.isEmpty()) {
			param.put(CWD, cwd);
		}

		try {
			URL fileURL = FileLocator.toFileURL(
					getClass().getResource("/language-servers/node_modules/node-debug2/out/src/nodeDebug.js"));
			List<String> debugCmdArgs = Collections.singletonList(fileURL.getPath());

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(InitializeLaunchConfigurations.getNodeJsLocation(), debugCmdArgs);
			builder.setMonitorDebugAdapter(true);
			builder.setDspParameters(param);

			super.launch(builder);
		} catch (IOException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus); //$NON-NLS-1$
		}
	}

}
