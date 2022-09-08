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
package org.eclipse.wildwebdeveloper.html.ui.preferences;

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CLOSING_TAGS;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;

/**
 * HTML preference initializer.
 *
 */
public class HTMLPreferenceInitializer extends AbstractPreferenceInitializer {

	private static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();

	@Override
	public void initializeDefaultPreferences() {
		// Client settings
		STORE.setDefault(HTML_PREFERENCES_AUTO_CLOSING_TAGS, true);
	}

}