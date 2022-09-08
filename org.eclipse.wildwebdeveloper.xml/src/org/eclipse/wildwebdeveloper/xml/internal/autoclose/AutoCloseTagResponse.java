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
package org.eclipse.wildwebdeveloper.xml.internal.autoclose;

import org.eclipse.lsp4j.Range;

/**
 * Auto close tag LSP response.
 *
 */
public class AutoCloseTagResponse {

	public String snippet;
	public Range range;

	public AutoCloseTagResponse(String snippet, Range range) {
		this.snippet = snippet;
		this.range = range;
	}

	public AutoCloseTagResponse(String snippet) {
		this.snippet = snippet;
	}
}