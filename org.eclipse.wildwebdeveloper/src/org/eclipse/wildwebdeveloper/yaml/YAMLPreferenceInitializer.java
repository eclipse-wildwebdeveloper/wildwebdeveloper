/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.yaml;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;

import com.google.gson.JsonObject;


public class YAMLPreferenceInitializer extends AbstractPreferenceInitializer {
	private static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();
	public static final String YAML_SCHEMA_PREFERENCE = "wildwebdeveloper.yaml.schema";
	
	@Override
	public void initializeDefaultPreferences() {
		STORE.setDefault(YAML_SCHEMA_PREFERENCE, getDefaultYamlSchema());
	}
	
	private static String getDefaultYamlSchema() {
		JsonObject schemaJson = new JsonObject();
		return schemaJson.toString();
	}
	
}
