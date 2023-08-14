/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * JS/TS preference initializer.
 *
 */
public class JSTSPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		JSTSPreferenceServerConstants.initializeDefaultPreferences();
	}

}