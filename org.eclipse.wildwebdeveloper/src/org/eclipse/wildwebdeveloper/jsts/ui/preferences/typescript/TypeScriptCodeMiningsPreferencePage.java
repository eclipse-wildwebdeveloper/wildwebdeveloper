/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences.typescript;

import org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSCodeMiningsPreferencePage;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSLanguagePreferences;

/**
 * TypeScript Code Minings preference page.
 */
public final class TypeScriptCodeMiningsPreferencePage extends JSTSCodeMiningsPreferencePage {

	public TypeScriptCodeMiningsPreferencePage() {
		super(JSTSLanguagePreferences.TS);
	}
}
