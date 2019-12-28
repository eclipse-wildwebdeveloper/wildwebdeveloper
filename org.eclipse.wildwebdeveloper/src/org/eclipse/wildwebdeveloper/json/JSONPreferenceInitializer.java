/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;

import com.google.gson.JsonObject;

public class JSONPreferenceInitializer extends AbstractPreferenceInitializer {
	private static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();
	public static final String JSON_SCHEMA_PREFERENCE = "wildwebdeveloper.json.schema";

	@Override
	public void initializeDefaultPreferences() {
		STORE.setDefault(JSON_SCHEMA_PREFERENCE, getDefaultJSONSchema());
	}

	private static String getDefaultJSONSchema() {
		JsonObject schemaJson = new JsonObject();
		schemaJson.addProperty("/.bower.json", "http://json.schemastore.org/bower");
		schemaJson.addProperty("/.bowerrc", "http://json.schemastore.org/bowerrc");
		schemaJson.addProperty("/bower.json", "http://json.schemastore.org/bower");
		schemaJson.addProperty("/jsconfig.json", "http://json.schemastore.org/jsconfig");
		schemaJson.addProperty("/omnisharp.json", "http://json.schemastore.org/omnisharp");
		schemaJson.addProperty("/tsconfig.*.json", "http://json.schemastore.org/tsconfig");
		schemaJson.addProperty("/tsconfig.json", "http://json.schemastore.org/tsconfig");
		schemaJson.addProperty("package.json", "http://json.schemastore.org/package");
		return schemaJson.toString();
	}

}
