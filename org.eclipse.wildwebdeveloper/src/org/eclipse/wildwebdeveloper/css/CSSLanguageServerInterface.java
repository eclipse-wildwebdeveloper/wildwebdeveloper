/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.wildwebdeveloper.css;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.wildwebdeveloper.colors.DocumentColorProvider;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

public interface CSSLanguageServerInterface extends LanguageServer, DocumentColorProvider {

	@Override
	@JsonRequest("css/colorSymbols")
	CompletableFuture<List<Range>> findDocumentColors(URI uri);

}
