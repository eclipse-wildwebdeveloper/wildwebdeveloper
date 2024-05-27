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
 *   Pierre-Yves B. - Issue #180 Wrong path to nodeDebug.js
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.node;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class NodeAttachDebugDelegate extends VSCodeJSDebugDelegate {

	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.nodeDebugAttach"; //$NON-NLS-1$

	// see https://github.com/Microsoft/vscode-node-debug/blob/master/src/node/nodeDebug.ts LaunchRequestArguments
	static final String ADDRESS = "address"; //$NON-NLS-1$
	static final String LOCAL_ROOT = "localRoot"; //$NON-NLS-1$
	static final String REMOTE_ROOT = "remoteRoot"; //$NON-NLS-1$

	public NodeAttachDebugDelegate() {
		super("pwa-node");
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// user settings
		Map<String, Object> param = new HashMap<>();
		param.put("request", "attach");
		param.put(ADDRESS, configuration.getAttribute(ADDRESS, "no address defined")); //$NON-NLS-1$
		param.put(LaunchConstants.PORT, configuration.getAttribute(LaunchConstants.PORT, -1));
		param.put("type", type);
		param.put("continueOnAttach", true);
		if (configuration.hasAttribute(LOCAL_ROOT)) {
			param.put(LOCAL_ROOT, VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(configuration.getAttribute(LOCAL_ROOT, "")));
		}
		if (configuration.hasAttribute(REMOTE_ROOT)) {
			param.put(REMOTE_ROOT, configuration.getAttribute(REMOTE_ROOT, ""));
		}
		File runtimeExecutable = NodeJSManager.getNodeJsLocation();
		if (runtimeExecutable != null) {
			param.put(RUNTIME_EXECUTABLE, runtimeExecutable.getAbsolutePath());
		}
		try {
			URL fileURL = FileLocator.toFileURL(getClass().getResource(NODE_DEBUG_CMD));
			File file = new File(fileURL.getPath());
			int port = 0;
			try (ServerSocket serverSocket = new ServerSocket(0)) {
				port = serverSocket.getLocalPort();
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			Process vscodeJsDebugExec = DebugPlugin.exec(new String[] { runtimeExecutable.getAbsolutePath(), file.getAbsolutePath(), Integer.toString(port) }, new File(System.getProperty("user.dir")), new String[] { "DA_TEST_DISABLE_TELEMETRY=true"}, false);
			IProcess vscodeJsDebugIProcess = DebugPlugin.newProcess(launch, vscodeJsDebugExec, "debug adapter");
			AtomicBoolean started = new AtomicBoolean();
			vscodeJsDebugIProcess.getStreamsProxy().getOutputStreamMonitor().addListener((text, mon) -> {
				if (text.toLowerCase().contains("listening")) {
					started.set(true);
				}
			});
			Instant request = Instant.now();
			while (!started.get() && Duration.between(request, Instant.now()).compareTo(Duration.ofSeconds(3)) < 3) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setAttachDebugAdapter("::1", port);
			builder.setMonitorDebugAdapter(configuration.getAttribute(DSPPlugin.ATTR_DSP_MONITOR_DEBUG_ADAPTER, false));
			builder.setDspParameters(param);
			super.launch(builder);
			IDebugEventSetListener shutdownParentOnCompletion = new IDebugEventSetListener() {
				@Override
				public void handleDebugEvents(DebugEvent[] events) {
					if (Arrays.stream(events).anyMatch(event ->
						event.getKind() == DebugEvent.TERMINATE &&
						event.getSource() instanceof final IDebugTarget target &&
						target.getLaunch() == launch)) {
						if (Arrays.stream(launch.getDebugTargets()).allMatch(IDebugTarget::isTerminated)
							&& List.of(vscodeJsDebugIProcess).equals(Arrays.stream(launch.getProcesses()).filter(Predicate.not(IProcess::isTerminated)).toList())) {
								try {
									vscodeJsDebugIProcess.terminate();
								} catch (DebugException ex) {
									vscodeJsDebugExec.destroy();
								} 
								DebugPlugin.getDefault().removeDebugEventListener(this);
							}
					}
				}
			};
			DebugPlugin.getDefault().addDebugEventListener(shutdownParentOnCompletion);
		} catch (IOException e) {
			IStatus errorStatus = Status.error(e.getMessage(), e);
			ILog.get().log(errorStatus);
			Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus)); //$NON-NLS-1$
		}

	}

}
