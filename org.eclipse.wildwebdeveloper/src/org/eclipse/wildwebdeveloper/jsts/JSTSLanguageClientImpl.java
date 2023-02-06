/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts;

import java.util.Map;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public class JSTSLanguageClientImpl extends LanguageClientImpl {

	@JsonNotification(value = "$/typescriptVersion")
	public void typescriptVersion(Map<String, String> tsInfo) {
		logMessage(new MessageParams(MessageType.Info,
				"Typescript Server version info: " + (tsInfo != null ? tsInfo.toString() : "not provided")));
	}
}
