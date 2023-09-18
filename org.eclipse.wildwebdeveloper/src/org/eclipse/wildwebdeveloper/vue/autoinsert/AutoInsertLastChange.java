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

import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

/**
 * VUE Auto Insert parameters.
 * 
 */
public class AutoInsertLastChange extends TextDocumentContentChangeEvent {

	private int rangeOffset;

	public int getRangeOffset() {
		return rangeOffset;
	}

	public void setRangeOffset(int rangeOffset) {
		this.rangeOffset = rangeOffset;
	}
}
