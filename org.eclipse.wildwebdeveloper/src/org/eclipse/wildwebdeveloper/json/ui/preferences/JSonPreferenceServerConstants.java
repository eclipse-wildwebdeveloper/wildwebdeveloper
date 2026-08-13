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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * JSON preference server constants.
 *
 */
public class JSonPreferenceServerConstants {

	private static final String JSON_SECTION = "json"; //$NON-NLS-1$

	/**
	 * Equivalent of VSCode's json.maxItemsComputed. Also drives the folding limits
	 * below, as VSCode does. 0 means no limit.
	 */
	public static final String JSON_PREFERENCES_MAXITEMSCOMPUTED = "json.resultLimit"; //$NON-NLS-1$

	private static final String JSON_PREFERENCES_JSON_FOLDINGLIMIT = "json.jsonFoldingLimit"; //$NON-NLS-1$
	private static final String JSON_PREFERENCES_JSONC_FOLDINGLIMIT = "json.jsoncFoldingLimit"; //$NON-NLS-1$
	private static final String JSON_PREFERENCES_VALIDATE_ENABLE = "json.validate.enable"; //$NON-NLS-1$
	private static final String JSON_PREFERENCES_FORMAT_ENABLE = "json.format.enable"; //$NON-NLS-1$
	private static final String HTTP_PREFERENCES_PROXYSTRICTSSL = "http.proxyStrictSSL"; //$NON-NLS-1$

	public static final int MAXITEMSCOMPUTED_DEFAULT = 5000;

	/**
	 * The server re-reads all of its settings from every notification, so entries
	 * that only restate a default still have to be sent: without "validate.enable"
	 * it turns validation off, and without "http.proxyStrictSSL" it stops verifying
	 * certificates when downloading schemas.
	 */
	public static Settings getGlobalSettings() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Settings settings = new Settings(store);

		int maxItemsComputed = store.getInt(JSON_PREFERENCES_MAXITEMSCOMPUTED);
		settings.fillSetting(JSON_PREFERENCES_MAXITEMSCOMPUTED, maxItemsComputed);
		settings.fillSetting(JSON_PREFERENCES_JSON_FOLDINGLIMIT, maxItemsComputed);
		settings.fillSetting(JSON_PREFERENCES_JSONC_FOLDINGLIMIT, maxItemsComputed);

		settings.fillSetting(JSON_PREFERENCES_VALIDATE_ENABLE, Boolean.TRUE);
		settings.fillSetting(JSON_PREFERENCES_FORMAT_ENABLE, Boolean.TRUE);
		settings.fillSetting(HTTP_PREFERENCES_PROXYSTRICTSSL, Boolean.TRUE);

		return settings;
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(JSON_PREFERENCES_MAXITEMSCOMPUTED, MAXITEMSCOMPUTED_DEFAULT);
	}

	public static boolean isMatchJSonSection(String section) {
		return Settings.isMatchSection(section, JSON_SECTION);
	}

}
