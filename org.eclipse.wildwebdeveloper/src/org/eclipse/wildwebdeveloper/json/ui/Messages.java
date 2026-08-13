/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Lars Vogel (Vogella GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json.ui;

import org.eclipse.osgi.util.NLS;

/**
 * JSON messages keys.
 *
 */
public class Messages extends NLS {

	// --------- JSON Main preference page
	public static String JSonPreferencePage_maxItemsComputed;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.json.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
