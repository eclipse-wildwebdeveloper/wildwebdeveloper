/*******************************************************************************
 * Copyright (c) 2018, 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.firefox;

import org.eclipse.wildwebdeveloper.debug.AbstractHTMLDebugAdapterLaunchShortcut;

public class FirefoxRunDebugLaunchShortcut extends AbstractHTMLDebugAdapterLaunchShortcut {

	public FirefoxRunDebugLaunchShortcut() {
		super(FirefoxRunDABDebugDelegate.ID);
	}

}
