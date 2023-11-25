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
 *  Pierre-Yves Bigourdan - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;

/**
 * JS/TS preference server constants.
 *
 */
public class JSTSPreferenceServerConstants {

	public static final String TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION = "typescript.tsserver.typescript.version";

	public static final String TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_ECLIPSE = "Eclipse version";
	public static final String TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_PROJECT = "Project version";
	
	public static final String ESLINT_PREFERENCES_NODE_PATH = "eslint.nodePath";

	public static String getTypeScriptVersion() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION);
	}

	public static String getESLintNodePath() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(ESLINT_PREFERENCES_NODE_PATH);
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION, TYPESCRIPT_PREFERENCES_TSSERVER_TYPESCRIPT_VERSION_ECLIPSE);
		store.setDefault(ESLINT_PREFERENCES_NODE_PATH, "");
	}
}
