/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.eslint;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;

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
			highestPackageJsonDir = new File(new URI(configurationItem.getScopeUri())).getParentFile();;
			File parentFile = highestPackageJsonDir;
			while (parentFile != null) {
				if (new File(parentFile, "package.json").exists()) highestPackageJsonDir = parentFile;
				parentFile = parentFile.getParentFile();
			}
		} catch (URISyntaxException e) {
			// shouldn't happen else what to do here?
		}
		config.put("workspaceFolder", Collections.singletonMap("uri", highestPackageJsonDir.toURI().toString())); 

		// if you set a workspaceFolder and then the working dir in auto mode eslint will try to get to the right config location.
		config.put("workingDirectory", Collections.singletonMap("mode", "auto")); 


		// this should not point to a nodejs executable but nodePath is the path to the "node_modules" 
		// (or a parent having node modules, we just push in the highest dir (workspaceFolder) that has the package.json)
		config.put("nodePath", highestPackageJsonDir.getAbsolutePath());
		
		config.put("validate", "on");
		config.put("run", "onType");

		config.put("codeAction", Map.of("disableRuleComment", Map.of("enable", "true", "location", "separateLine"), 
										"showDocumentation", Collections.singletonMap("enable", "true")));
		return CompletableFuture.completedFuture(Collections.singletonList(config));
	}

	@Override
	public CompletableFuture<Void> eslintStatus(Object o) {
		// ignore for now
		return CompletableFuture.completedFuture(null);
	}
}
