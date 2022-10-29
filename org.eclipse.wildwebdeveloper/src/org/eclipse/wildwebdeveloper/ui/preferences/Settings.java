/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Settings class.
 *
 */
@SuppressWarnings("serial")
public class Settings extends HashMap<String, Object> {

	private IPreferenceStore store;

	public Settings(IPreferenceStore store) {
		this.store = store;
	}

	public void fillAsBoolean(String preferenceId) {
		boolean preferenceValue = store.getBoolean(preferenceId);
		fillSetting(preferenceId, preferenceValue);
	}

	public void fillAsInt(String preferenceId) {
		int preferenceValue = store.getInt(preferenceId);
		fillSetting(preferenceId, preferenceValue);
	}

	public void fillAsString(String preferenceId) {
		String preferenceValue = store.getString(preferenceId);
		fillSetting(preferenceId, preferenceValue);
	}

	public void fillAsStringArray(String preferenceId, String separator) {
		String[] preferenceValue = store.getString(preferenceId).split(separator);
		fillSetting(preferenceId, preferenceValue);
	}

	public void fillSetting(String preferenceId, Object preferenceValue) {
		fillSetting(preferenceId, preferenceValue, this);
	}

	@SuppressWarnings("unchecked")
	private static void fillSetting(String preferenceId, Object preferenceValue, Map<String, Object> settings) {
		Map<String, Object> result = settings;
		String[] paths = preferenceId.split("[.]");
		String path = null;
		for (int i = 0; i < paths.length - 1; i++) {
			path = paths[i];
			if (result.containsKey(path)) {
				result = (Map<String, Object>) result.get(path);
			} else {
				Map<String, Object> item = new HashMap<>();
				result.put(path, item);
				result = item;
			}
		}
		path = paths[paths.length - 1];
		result.put(path, preferenceValue);
	}

	public Object findSettings(String[] sections) {
		Map<String, Object> current = this;
		for (String section : sections) {
			Object result = current.get(section);
			if (result == null || !(result instanceof Map)) {
				return null;
			}
			current = (Map<String, Object>) result;
		}
		return current;
	}

	public static boolean isMatchSection(String property, String section) {
		return (property.equals(section) || property.startsWith(section + "."));
	}
}
