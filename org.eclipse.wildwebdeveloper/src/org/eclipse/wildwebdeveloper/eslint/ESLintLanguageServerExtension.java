/*******************************************************************************
 * Copyright (c) 2020, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.eslint;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface ESLintLanguageServerExtension {

	@JsonRequest(value = "eslint/confirmESLintExecution")
	public CompletableFuture<Integer> confirmESLintExecution(Object param);
	
	@JsonRequest(value = "eslint/status")
	public CompletableFuture<Void> eslintStatus(Object o);
	
	@JsonRequest(value = "eslint/openDoc")
	public CompletableFuture<Void> openDoc(Map<String,String> data);
	
	@JsonRequest(value = "eslint/noLibrary")
	public CompletableFuture<Void> noLibrary(Map<String,Map<String,String>> data);
	
	@JsonRequest(value = "eslint/noConfig")
	public CompletableFuture<Void> noConfig(Map<String,Map<String,String>> data);
}
