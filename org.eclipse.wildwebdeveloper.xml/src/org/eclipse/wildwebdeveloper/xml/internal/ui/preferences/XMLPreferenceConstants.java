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

		public LemminxPreference(String lemminxOptionPath) {
			this.preferenceId = getPreferenceId(lemminxOptionPath);
			this.lemminxOptionPath = lemminxOptionPath;
		}
		
		private static String getPreferenceId(String lemminxOptionPath) {
			return Activator.PLUGIN_ID + "." + lemminxOptionPath.replace("/", ".");
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


	// General settings
	public static final LemminxPreference XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES = new LemminxPreference("downloadExternalResources/enabled");

	// Catalog settings
	public static final LemminxPreference XML_PREFERENCES_CATAGLOGS = new LemminxPreference("catalogs");
	
	// CodeLens settings
	public static final LemminxPreference XML_PREFERENCES_CODELENS_ENABLED = new LemminxPreference("codeLens/enabled");
	
	// Folding settings
	public static final LemminxPreference XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD = new LemminxPreference("foldings/includeClosingTagInFold");

	// Format settings
	public static final LemminxPreference XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS = new LemminxPreference("format/emptyElements");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG = new LemminxPreference("format/spaceBeforeEmptyCloseTag");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES = new LemminxPreference("format/splitAttributes");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE = new LemminxPreference("format/splitAttributesIndentSize");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS = new LemminxPreference("format/preserveAttributeLineBreaks");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE = new LemminxPreference("format/closingBracketNewLine");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT = new LemminxPreference("format/xsiSchemaLocationSplit");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_JOIN_COMMENTLINES = new LemminxPreference("format/joinCommentLines");

	// Validation settings
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_ENABLED = new LemminxPreference("validation/enabled");

	public static final LemminxPreference XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED = new LemminxPreference("validation/namespaces/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED = new LemminxPreference("validation/schema/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL = new LemminxPreference("validation/disallowDocTypeDecl");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES = new LemminxPreference("validation/resolveExternalEntities");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_NO_GRAMMAR = new LemminxPreference("validation/noGrammar");
	
	private static final LemminxPreference[] ALL_LEMMINX_PREFERENCES = {
			XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES,
			XML_PREFERENCES_CATAGLOGS,
			XML_PREFERENCES_CODELENS_ENABLED,
			XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD,
			XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS,
			XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG,
			XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES,
			XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE,
			XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS,
			XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE,
			XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT,
			XML_PREFERENCES_FORMAT_JOIN_COMMENTLINES,
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
		// General settings
		XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId), xmlOpts);
		
		// Catalog settings
		XML_PREFERENCES_CATAGLOGS.storeToLemminxOptions(
				XMLCatalogs.getAllCatalogs(store).stream().map(File::getAbsolutePath).toArray(String[]::new),
				xmlOpts);
		
		// CodeLens settings
		XML_PREFERENCES_CODELENS_ENABLED.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_CODELENS_ENABLED.preferenceId), xmlOpts);

		// Folding settings
		XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD.preferenceId), xmlOpts);
		
		// Format settings
		XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS.storeToLemminxOptions(store.getString(XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE.storeToLemminxOptions(store.getInt(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT.storeToLemminxOptions(store.getString(XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT.preferenceId), xmlOpts);
		XML_PREFERENCES_FORMAT_JOIN_COMMENTLINES.storeToLemminxOptions(store.getBoolean(XML_PREFERENCES_FORMAT_JOIN_COMMENTLINES.preferenceId), xmlOpts);
		
		// Validation settings
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
