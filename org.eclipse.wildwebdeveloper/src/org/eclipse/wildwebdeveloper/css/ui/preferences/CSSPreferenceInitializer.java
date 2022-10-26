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
package org.eclipse.wildwebdeveloper.css.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.wildwebdeveloper.css.ui.preferences.less.LESSPreferenceServerConstants;
import org.eclipse.wildwebdeveloper.css.ui.preferences.scss.SCSSPreferenceServerConstants;

/**
 * CSS, LESS, SCSS preference initializer.
 *
 */
public class CSSPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		CSSPreferenceServerConstants.initializeDefaultPreferences();
		LESSPreferenceServerConstants.initializeDefaultPreferences();
		SCSSPreferenceServerConstants.initializeDefaultPreferences();
	}

}