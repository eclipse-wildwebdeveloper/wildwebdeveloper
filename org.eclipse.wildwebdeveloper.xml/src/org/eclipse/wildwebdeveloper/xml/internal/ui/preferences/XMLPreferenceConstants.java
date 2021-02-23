/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

public class XMLPreferenceConstants {

	public static class LemminxPreference {
		public final String preferenceId;
		/**
		 * without the "xml" prefix
		 * @see https://github.com/redhat-developer/vscode-xml#supported-vs-code-settings
		 */
		public final String lemminxOptionPath;

		public LemminxPreference(String preferenceId, String lemminxOptionPath) {
			this.preferenceId = preferenceId;
			this.lemminxOptionPath = lemminxOptionPath;
		}

		public void storeToLemminxOptions(Object value, Map<String, Object> options) {
			Map<String, Object> result = options;
			String[] paths = this.lemminxOptionPath.split("/");
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
			result.put(path, value);
		}
	}

	public static final LemminxPreference XML_PREFERENCES_CATAGLOGS = new LemminxPreference(Activator.PLUGIN_ID + ".catalogs", "catalogs");
	public static final LemminxPreference XML_PREFERENCES_CODELENS_ENABLED = new LemminxPreference(Activator.PLUGIN_ID + ".codeLens.enabled", "codeLens/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_ENABLED = new LemminxPreference(Activator.PLUGIN_ID + ".validation.enabled", "validation/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED = new LemminxPreference(Activator.PLUGIN_ID + ".validation.namespaces.enabled", "validation/namespaces/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED = new LemminxPreference(Activator.PLUGIN_ID + ".validation.schema.enabled", "validation/schema/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL = new LemminxPreference(Activator.PLUGIN_ID + ".validation.disallowDocTypeDecl", "validation/disallowDocTypeDecl");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES = new LemminxPreference(Activator.PLUGIN_ID + ".validation.resolveExternalEntities", "validation/resolveExternalEntities");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_NO_GRAMMAR = new LemminxPreference(Activator.PLUGIN_ID + ".validation.noGrammar", "validation/noGrammar");

	private static final LemminxPreference[] ALL_LEMMINX_PREFERENCES = {
			XML_PREFERENCES_CATAGLOGS,
			XML_PREFERENCES_CODELENS_ENABLED,
			XML_PREFERENCES_VALIDATION_ENABLED,
			XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED,
			XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED,
			XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL,
			XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES,
			XML_PREFERENCES_VALIDATION_NO_GRAMMAR
	};
			
	public static Optional<LemminxPreference> getLemminxPreference(PropertyChangeEvent event) {
		return Arrays.stream(ALL_LEMMINX_PREFERENCES).filter(pref -> Objects.equals(pref.preferenceId, event.getProperty())).findAny();
	}

	private XMLPreferenceConstants() {

	}

	public static void storePreferencesToLemminxOptions(IPreferenceStore store, Map<String, Object> xmlOpts) {
		XML_PREFERENCES_CATAGLOGS.storeToLemminxOptions(
				XMLCatalogs.getAllCatalogs(store).stream().map(File::getAbsolutePath).toArray(String[]::new),
				xmlOpts);
		XML_PREFERENCES_CODELENS_ENABLED.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_CODELENS_ENABLED.preferenceId), xmlOpts);
		XML_PREFERENCES_VALIDATION_ENABLED.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_VALIDATION_ENABLED.preferenceId), xmlOpts);
		XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.storeToLemminxOptions(store.getString(XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.preferenceId), xmlOpts);
		XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.storeToLemminxOptions(store.getString(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.preferenceId),
				xmlOpts);
		XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.storeToLemminxOptions(
				store.getBoolean(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.preferenceId), xmlOpts);
		XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.storeToLemminxOptions(
				store.getBoolean(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.preferenceId), xmlOpts);
		XML_PREFERENCES_VALIDATION_NO_GRAMMAR.storeToLemminxOptions(store.getString(XML_PREFERENCES_VALIDATION_NO_GRAMMAR.preferenceId),
				xmlOpts);
	}
}
