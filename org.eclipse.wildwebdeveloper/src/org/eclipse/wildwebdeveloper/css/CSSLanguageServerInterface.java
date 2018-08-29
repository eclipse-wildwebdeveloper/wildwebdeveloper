/**
 * Copyright (c) 2017 Angelo ZERR.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.wildwebdeveloper.css;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.colors.DocumentColorProvider;

public interface CSSLanguageServerInterface extends LanguageServer, DocumentColorProvider {

	@Override
	@JsonRequest("css/colorSymbols")
	CompletableFuture<List<Range>> findDocumentColors(URI uri);

}
