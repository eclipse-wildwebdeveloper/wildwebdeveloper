/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.markdown;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Markdown language server API for client->server custom requests.
 */
public interface MarkdownLanguageServerAPI extends LanguageServer {

	/**
	 * <pre>
	 * Request: { id: number; uri: string; kind: 'create' | 'change' | 'delete' }
	 * Response: void
	 * </pre>
	 */
	@JsonRequest("markdown/fs/watcher/onChange")
	CompletableFuture<Void> fsWatcherOnChange(Map<String, Object> params);
}
