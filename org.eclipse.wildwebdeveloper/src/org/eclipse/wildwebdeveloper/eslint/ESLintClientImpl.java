/*******************************************************************************
 * Copyright (c) 2020, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Pierre-Yves Bigourdan - Allow configuring directory of ESLint package
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.eslint;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ILog;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSPreferenceServerConstants;

public class ESLintClientImpl extends LanguageClientImpl implements ESLintLanguageServerExtension {

	@Override
	public CompletableFuture<Integer> confirmESLintExecution(Object param) {
		return CompletableFuture.completedFuture(Integer.valueOf(4));
	}

	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
		ConfigurationItem configurationItem = configurationParams.getItems().get(0);
		
		Map<String, Object> config = new HashMap<>(6, 1.f);

		// search first for the highest directory that has a package.json file
		// to be set as the workspaceFolder below
		// then when the workingDirectory is set in mode:auto the eslint server code will look up
		// until the workspaceFolder which folder is best suited for its workingDirectoy (where the config files are in)
		// also this workspaceFolder is also used to find the node models (eslint module)
		// because we set the nodePath below to this same directory.
		File highestPackageJsonDir = null;
		try {
			highestPackageJsonDir = new File(new URI(configurationItem.getScopeUri())).getParentFile();
			File parentFile = highestPackageJsonDir;
			while (parentFile != null) {
				if (new File(parentFile, "package.json").exists()) highestPackageJsonDir = parentFile;
				parentFile = parentFile.getParentFile();
			}
		} catch (URISyntaxException e) {
			// shouldn't happen else what to do here?
		}
		
		// `pre-release/2.3.0`: Disable using experimental Flat Config system 
		config.put("experimental", Collections.emptyMap());

		// `pre-release/2.3.0`: Add stub `problems` settings due to:
		//   ESLint: Cannot read properties of undefined (reading \u0027shortenToSingleLine\u0027). Please see the \u0027ESLint\u0027 output channel for details.
		config.put("problems", Collections.emptyMap());

		config.put("workspaceFolder", Collections.singletonMap("uri", highestPackageJsonDir.toURI().toString())); 

		// if you set a workspaceFolder and then the working dir in auto mode eslint will try to get to the right config location.
		config.put("workingDirectory", Collections.singletonMap("mode", "auto")); 

		// this should not point to a nodejs executable but to a parent directory containing the ESLint package
		config.put("nodePath",getESLintPackageDir(highestPackageJsonDir));

		config.put("validate", "on");
		config.put("run", "onType");
		config.put("rulesCustomizations", Collections.emptyList());

		config.put("codeAction", Map.of("disableRuleComment", Map.of("enable", "true", "location", "separateLine"), 
										"showDocumentation", Collections.singletonMap("enable", "true")));
		return CompletableFuture.completedFuture(Collections.singletonList(config));
	}

	private String getESLintPackageDir(File highestPackageJsonDir) {
		String eslintNodePath = JSTSPreferenceServerConstants.getESLintNodePath();

		// check if user specified a valid absolute path
		File eslintNodeFileUsingAbsolutePath = new File(eslintNodePath);
		if (eslintNodeFileUsingAbsolutePath.exists()) {
			return eslintNodeFileUsingAbsolutePath.getAbsolutePath();
		}

		// check if user specified a valid project-relative path
		File eslintNodeFileUsingProjectRelativePath = new File(highestPackageJsonDir.getAbsolutePath(), eslintNodePath);
		if (eslintNodeFileUsingProjectRelativePath.exists()) {
			return eslintNodeFileUsingProjectRelativePath.getAbsolutePath();
		}

		// fall back to the folder containing "node_modules"
		return highestPackageJsonDir.getAbsolutePath();
	}

	@Override
	public CompletableFuture<Void> eslintStatus(Object o) {
		// ignore for now
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> openDoc(Map<String,String> data) {
		if (data.containsKey("url")) {
			Display.getDefault().asyncExec(() -> {
				IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
				try {
					browserSupport.createBrowser("openDoc").openURL(new URL(data.get("url")));
				} catch (Exception e) {
					ILog.get().error(e.getMessage(), e);
				}
			});
		}
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> noLibrary(Map<String,Map<String,String>> data) {
		MessageParams params = new MessageParams(MessageType.Info, "No ES Library found for file: " + data.get("source").get("uri"));
		logMessage(params);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> noConfig(Map<String, Map<String, String>> data) {
		MessageParams params = new MessageParams(MessageType.Info, "No ES Configuration found for file: " + data.get("source").get("uri") 
				+ ": " + data.get("message"));
		logMessage(params);
		return CompletableFuture.completedFuture(null);
	}
}
