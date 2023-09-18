/*******************************************************************************
 * Copyright (c) 2023 Dawid Pakuła and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Dawid Pakuła - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.vue.autoinsert.AutoInsertParams;
import org.eclipse.wildwebdeveloper.vue.autoinsert.AutoInsertResponse;

/**
 * VUE language server API which defines custom LSP commands.
 *
 */
public interface VueLanguageServerAPI extends LanguageServer {

	/**
	 * Auto insert custom LSP command provided by the HTML language server to
	 * support auto close tag and auto insert of quote for attribute.
	 * 
	 * @param params auto insert parameters.
	 * @return the content with the auto close tag / auto insert of quote and null
	 *         otherwise.
	 * 
	 */
	@JsonRequest("volar/client/autoInsert")
	CompletableFuture<Either<String, AutoInsertResponse>> autoInsert(AutoInsertParams params);
	

}
