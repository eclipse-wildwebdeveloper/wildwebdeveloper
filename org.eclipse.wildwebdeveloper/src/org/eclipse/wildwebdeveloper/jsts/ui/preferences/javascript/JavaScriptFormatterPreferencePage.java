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
 *   Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript;

import org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSFormatterPreferencePage;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSLanguagePreferences;

/**
 * JavaScript Inlay Hint preference page.
 */
public final class JavaScriptFormatterPreferencePage extends JSTSFormatterPreferencePage {

	public JavaScriptFormatterPreferencePage() {
		super(JSTSLanguagePreferences.JS);
	}
}
