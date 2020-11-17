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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class ESLintClientImpl extends LanguageClientImpl implements ESLintLanguageServerExtension {

	@Override
	public CompletableFuture<Integer> confirmESLintExecution(Object param) {
		return CompletableFuture.completedFuture(Integer.valueOf(4));
	}

	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
		Map<String, String> config = new HashMap<>(3, 1.f);
		config.put("validate", "on");
		config.put("run", "onType");
		config.put("nodePath", NodeJSManager.getNodeJsLocation().getAbsolutePath());
		return CompletableFuture.completedFuture(Collections.singletonList(config));
	}

	@Override
	public CompletableFuture<Void> eslintStatus(Object o) {
		// ignore for now
		return CompletableFuture.completedFuture(null);
	}
}
