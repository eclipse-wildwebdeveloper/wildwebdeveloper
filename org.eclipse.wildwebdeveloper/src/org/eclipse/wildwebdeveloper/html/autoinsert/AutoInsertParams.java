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
package org.eclipse.wildwebdeveloper.html.autoinsert;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * HTML Auto Insert parameters.
 * 
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/9b80ed652434a6e82b3ac28c1a9a01132c8faea3/extensions/html-language-features/client/src/htmlClient.ts#L31">https://github.com/microsoft/vscode/blob/9b80ed652434a6e82b3ac28c1a9a01132c8faea3/extensions/html-language-features/client/src/htmlClient.ts#L31</a>
 */
public class AutoInsertParams {

	public enum AutoInsertKind {
		
		autoQuote, autoClose;
	}
	
	/**
	 * The auto insert kind
	 */
	private String kind; // 'autoQuote' | 'autoClose';
	/**
	 * The text document.
	 */
	private TextDocumentIdentifier textDocument;
	/**
	 * The position inside the text document.
	 */
	private Position position;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public TextDocumentIdentifier getTextDocument() {
		return textDocument;
	}

	public void setTextDocument(TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
