/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.yaml;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ConfigurationParams;

public class YamlLanguageClientImpl extends LanguageClientImpl {

	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
		return CompletableFuture.completedFuture(List.of(YAMLLanguageServer.getYamlConfigurationOptions()));
	}
}
