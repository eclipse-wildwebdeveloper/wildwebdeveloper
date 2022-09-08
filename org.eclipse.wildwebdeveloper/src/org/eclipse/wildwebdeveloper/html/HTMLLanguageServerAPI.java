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
package org.eclipse.wildwebdeveloper.html;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.html.autoinsert.AutoInsertParams;

/**
 * HTML language server API which defines custom LSP commands.
 *
 */
public interface HTMLLanguageServerAPI extends LanguageServer {

	/**
	 * Auto insert custom LSP command provided by the HTML language server to
	 * support auto close tag and auto insert of quote for attribute.
	 * 
	 * @param params auto insert parameters.
	 * @return the content with the auto close tag / auto insert of quote and null
	 *         otherwise.
	 * 
	 * @see <a
	 *      href="https://github.com/microsoft/vscode/blob/main/extensions/html-language-features/client/src/autoInsertion.ts>https://github.com/microsoft/vscode/blob/main/extensions/html-language-features/client/src/autoInsertion.ts</a>
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/9b80ed652434a6e82b3ac28c1a9a01132c8faea3/extensions/html-language-features/client/src/htmlClient.ts#L46"
	 *      >https://github.com/microsoft/vscode/blob/9b80ed652434a6e82b3ac28c1a9a01132c8faea3/extensions/html-language-features/client/src/htmlClient.ts#L46</a>
	 */
	@JsonRequest("html/autoInsert")
	CompletableFuture<String> autoInsert(AutoInsertParams params);

}
