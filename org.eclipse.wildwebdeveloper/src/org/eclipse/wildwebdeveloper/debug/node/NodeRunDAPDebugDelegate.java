/*******************************************************************************
 * Copyright (c) 2018, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *   Pierre-Yves B. - Issue #180 Wrong path to nodeDebug.js
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.node;

public class NodeRunDAPDebugDelegate extends VSCodeJSDebugDelegate {

	public static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.nodeDebug"; //$NON-NLS-1$

	public NodeRunDAPDebugDelegate() {
		super("pwa-node");
	}
}
