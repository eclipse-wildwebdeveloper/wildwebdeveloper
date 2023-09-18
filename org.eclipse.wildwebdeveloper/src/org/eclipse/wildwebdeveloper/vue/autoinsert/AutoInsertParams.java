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
package org.eclipse.wildwebdeveloper.vue.autoinsert;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * VUE Auto Insert parameters.
 * 
 */
public class AutoInsertParams {

	/**
	 * The text document.
	 */
	private TextDocumentIdentifier textDocument;
	/**
	 * The position inside the text document.
	 */
	private Position position;
	
	private AutoInsertOptions options;
	

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
	
	public AutoInsertOptions getOptions() {
		return options;
	}

	public void setOptions(AutoInsertOptions options) {
		this.options = options;
	}

}
