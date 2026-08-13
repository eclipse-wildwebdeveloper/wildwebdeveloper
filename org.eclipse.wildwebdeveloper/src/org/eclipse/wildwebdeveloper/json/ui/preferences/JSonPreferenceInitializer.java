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
package org.eclipse.wildwebdeveloper.json.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * JSON preference initializer.
 *
 */
public class JSonPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		JSonPreferenceServerConstants.initializeDefaultPreferences();
	}

}
