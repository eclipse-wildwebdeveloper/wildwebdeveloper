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
	private Position selection;
	
	private AutoInsertLastChange change;
	

	public TextDocumentIdentifier getTextDocument() {
		return textDocument;
	}

	public void setTextDocument(TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}

	public Position getSelection() {
		return selection;
	}

	public void setSelection(Position selection) {
		this.selection = selection;
	}
	
	public AutoInsertLastChange getChange() {
		return change;
	}

	public void setChange(AutoInsertLastChange options) {
		this.change = options;
	}

}
