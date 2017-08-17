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
package org.eclipse.bluesky;

import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Extends {@link LanguageServer} to support custom notification with JSON
 * language server:
 * 
 * <ul>
 * <li>"json/schemaAssociations" to send a mapping between fileMatch (ex:
 * package.json) and uri (http://json.schemastore.org/package)</li>
 * </ul>
 *
 */
public interface JSonLanguageServerInterface extends LanguageServer {

	@JsonNotification("json/schemaAssociations")
	/**
	 * Send the JSON Schema associations waited by the VSCode JSON Language Server.
	 * 
	 * @param schemaAssociations
	 * @see https://github.com/Microsoft/vscode/blob/master/extensions/json/server/src/jsonServerMain.ts#L29
	 */
	void sendJSonchemaAssociations(Map<String, List<String>> schemaAssociations);
}
