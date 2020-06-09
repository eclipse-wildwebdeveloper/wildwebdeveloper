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
package org.eclipse.wildwebdeveloper.debug.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.lsp4e.debug.launcher.DSPLaunchDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;
import org.eclipse.wildwebdeveloper.debug.Messages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class NodeRunDAPDebugDelegate extends DSPLaunchDelegate {

	public static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.nodeDebug"; //$NON-NLS-1$

	// see https://github.com/Microsoft/vscode-node-debug/blob/master/src/node/nodeDebug.ts LaunchRequestArguments
	public static final String PROGRAM = "program"; //$NON-NLS-1$
	public static final String ARGUMENTS = "args"; //$NON-NLS-1$
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
		
		if (!configureAdditionalParameters(param)) {
			return;
		}

		try {
			URL fileURL = FileLocator.toFileURL(
					getClass().getResource("/node_modules/node-debug2/out/src/nodeDebug.js"));
			File file = new File(fileURL.getPath());
			List<String> debugCmdArgs = Collections.singletonList(file.getAbsolutePath());

			DSPLaunchDelegateLaunchBuilder builder = new DSPLaunchDelegateLaunchBuilder(configuration, mode, launch,
					monitor);
			builder.setLaunchDebugAdapter(InitializeLaunchConfigurations.getNodeJsLocation(), debugCmdArgs);
			builder.setMonitorDebugAdapter(configuration.getAttribute(DSPPlugin.ATTR_DSP_MONITOR_DEBUG_ADAPTER, false));
			builder.setDspParameters(param);
			super.launch(builder);
		} catch (IOException e) {
			IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(errorStatus);
			Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug error", e.getMessage(), errorStatus)); //$NON-NLS-1$
		}
	}

	private boolean configureAdditionalParameters(Map<String, Object> param) {
		String program = (String)param.get(PROGRAM);
		String cwd = (String)param.get(CWD);
		
		if (program == null) {
			return false;
		}
		
		if (Platform.getContentTypeManager().getContentType("org.eclipse.wildwebdeveloper.ts")
					.isAssociatedWith(new File(program).getName())) {
			// TypeScript Source Mappings Configuration
			String tsConfigPath = cwd + "/tsconfig.json";
			String errorMessage = null;
			Map<String, Object> tsConfig = readTsConfig(tsConfigPath);
			Map<String, Object> co = (Map<String, Object>)tsConfig.get("compilerOptions");
			if (tsConfig.isEmpty() || co == null) {
				errorMessage = Messages.NodeDebug_TSConfirError_NoTsConfig;
				co = new HashMap<>();
			}

			//TS Compiler Options
			param.putAll(co);

			if (errorMessage == null) {
				Object option = co.get("sourceMap");
				boolean sourceMap  = option instanceof Boolean ? ((Boolean)option).booleanValue() : false;
				if (!sourceMap) {
					errorMessage = Messages.NodeDebug_TSConfirError_SourceMapIsNotEnabled;
				}
			}

			// Override "outDir" option by converting it to an absolute path
			boolean outDirOrFileIsSet = false;
			Object option = co.get("module");
			String module = option instanceof String ? ((String)option).trim() : null;
						
			option = co.get("outDir");
			String outDir = option instanceof String ? ((String)option).trim() : null;
			if (outDir != null && outDir.length() > 0 && !".".equals(outDir) && !"./".equals(outDir)) {
				param.put("outDir", cwd + "/" + outDir);
				outDirOrFileIsSet = true;
			}
			
			option = co.get("outFile");
			String outFile = option instanceof String ? ((String)option).trim() : null;
			if (outFile != null && outFile.length() != 0) {
				param.put("outFile", cwd + "/" + outFile);
				outDirOrFileIsSet = true;
				
				if (!"amd".equalsIgnoreCase(module)  && !"system".equalsIgnoreCase(module)) {
					errorMessage = Messages.NodeDebug_TSConfigError_OutDirNotSupportedModule;
				}
			}
			
			if (!outDirOrFileIsSet && errorMessage == null) {
				errorMessage = Messages.NodeDebug_TSConfigError_OutDirIsNotSet;
			}

			if (errorMessage != null) {
				// Display error message
				final int[] result = new int[1];
				final String dialogMessage = errorMessage;
				
				Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog dialog = new MessageDialog(DebugUIPlugin.getShell(),
									Messages.NodeDebug_TSConfirError_Title, null, dialogMessage, MessageDialog.QUESTION_WITH_CANCEL,
									2, Messages.NodeDebug_TSConfirError_OpenTSConfigInEditor,
									Messages.NodeDebug_TSConfirError_StartDebuggingAsIs, Messages.NodeDebug_TSConfirError_Cancel);
							result[0] = dialog.open();
						}
					});
				
				if (result[0] == 0) {
					// Open TSConfig in editor
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IDE.openEditor(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
									new File(tsConfigPath).toURI(), 
									"org.eclipse.ui.genericeditor.GenericEditor",
									true);
							} catch (PartInitException e1) {
								Activator.getDefault().getLog().error(e1.getMessage(), e1);
							}
						}
					});
				} else if (result[0] == 1) {
					// Start debugging as is
					return true;
				}
				return false;
			}
			
			return true;
		} else if (Platform.getContentTypeManager().getContentType("org.eclipse.wildwebdeveloper.js")
				.isAssociatedWith(new File(program).getName())) {

			// JavaScript configuration
			
			// workaround until
			// https://github.com/microsoft/vscode-node-debug2/commit/f2dfa4ca4026fb3e4f143a391270a03df8187b42#diff-d03a74f75ec189cbc7dd3d2e105fc9c9R625
			// is released in VSCode
			param.put("sourceMaps", false);
			return true;
		}
		return false;
	}
	
	public Map<String, Object> readTsConfig(String path) {
		try (BufferedReader in = new BufferedReader(new FileReader(path))) {
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine).append('\n');
			}
			Type type = new TypeToken<Map<String, Object>>() {}.getType();
			return new Gson().fromJson(response.toString(), type);
		} catch (IOException e) {
			return new HashMap<>(0);
		}
	}
}
