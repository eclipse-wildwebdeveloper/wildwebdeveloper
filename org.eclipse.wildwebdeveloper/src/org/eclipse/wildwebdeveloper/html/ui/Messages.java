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
package org.eclipse.wildwebdeveloper.html.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	// --------- HTML Preference page
	public static String HTMLPreferencePage_autoClosingTags;
	
	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.html.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
