/*******************************************************************************
 * Copyright (c) Dawid Pakuła and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Dawid Pakuła <zulus@w3des.net> - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts;

import org.eclipse.lsp4j.services.LanguageServer;

public interface JSTSLanguageServerAPI extends LanguageServer {

	public final static String TS_REQUEST_COMMAND = "typescript.tsserverRequest";
}
