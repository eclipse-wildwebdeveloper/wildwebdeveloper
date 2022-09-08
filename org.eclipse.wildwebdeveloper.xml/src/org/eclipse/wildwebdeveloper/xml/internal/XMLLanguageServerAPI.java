/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.xml.internal.autoclose.AutoCloseTagResponse;

/**
 * XML language server API which defines custom LSP commands.
 *
 */
public interface XMLLanguageServerAPI extends LanguageServer {

	@JsonRequest("xml/closeTag")
	CompletableFuture<AutoCloseTagResponse> closeTag(TextDocumentPositionParams params);

}
