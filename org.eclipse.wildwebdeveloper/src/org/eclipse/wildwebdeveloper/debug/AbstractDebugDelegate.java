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
 *   Andrew Obuchowicz (Red Hat Inc.) 
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;

public class AbstractDebugDelegate extends DSPLaunchDelegate{
	public static final String PROGRAM = "program"; //$NON-NLS-1$
	public static final String ARGUMENTS = "runtimeArgs"; //$NON-NLS-1$
	public static final String CWD = DebugPlugin.ATTR_WORKING_DIRECTORY; //$NON-NLS-1$
	public static final String ENV = ILaunchManager.ATTR_ENVIRONMENT_VARIABLES;
	public static final String SOURCE_MAPS = "sourceMaps";
	public static final String PORT = "port"; //$NON-NLS-1$
	public static final String REQUEST = "request"; //$NON-NLS-1$

	public void launchWithParameters(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor, Map<String, Object> param, File debugAdapter) throws CoreException {
		try {
			List<String> debugCmdArgs = Collections.singletonList(debugAdapter.getAbsolutePath());

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(InitializeLaunchConfigurations.getNodeJsLocation(), debugCmdArgs);
			builder.setMonitorDebugAdapter(configuration.getAttribute(DSPPlugin.ATTR_DSP_MONITOR_DEBUG_ADAPTER, false));
			builder.setDspParameters(param);

			super.launch(builder);
		} catch (Exception e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
					ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus); //$NON-NLS-1$
			}
			});

		}
	}

}
