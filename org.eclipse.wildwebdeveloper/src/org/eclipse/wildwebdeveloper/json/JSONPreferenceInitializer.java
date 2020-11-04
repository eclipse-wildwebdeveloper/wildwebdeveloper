/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
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
		STORE.setDefault(JSON_SCHEMA_PREFERENCE, getDefaultJSONSchemas());
	}

	private static String getDefaultJSONSchemas() {
		JsonObject jsonSchemas = new JsonObject();
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.bower", "http://json.schemastore.org/bower");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.bowerrc", "http://json.schemastore.org/bowerrc");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.eslintrc", "http://json.schemastore.org/eslintrc");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.jsconfig", "http://json.schemastore.org/jsconfig");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.lerna", "http://json.schemastore.org/lerna");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.npmpackage", "http://json.schemastore.org/package");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.omnisharp", "http://json.schemastore.org/omnisharp");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.tsconfig", "http://json.schemastore.org/tsconfig");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.tslint", "http://json.schemastore.org/tslint");
		jsonSchemas.addProperty("org.eclipse.wildwebdeveloper.json.typings", "http://json.schemastore.org/typing");
		return jsonSchemas.toString();
	}

}
