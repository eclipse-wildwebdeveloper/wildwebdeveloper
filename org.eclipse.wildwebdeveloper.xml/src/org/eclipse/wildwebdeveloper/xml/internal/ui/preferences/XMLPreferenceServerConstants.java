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

/**
 * XML pXMLPreferenceServerConstantsants.
 *
 */
public class XMLPreferenceServerConstants {

	public static class LemminxPreference {
		public final String preferenceId;
		/**
		 * without the "xml" prefix
		 * 
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

		@SuppressWarnings("unchecked")
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
	public static final LemminxPreference XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES = new LemminxPreference(
			"downloadExternalResources/enabled");

	// Catalog settings
	public static final LemminxPreference XML_PREFERENCES_CATAGLOGS = new LemminxPreference("catalogs");

	// CodeLens settings
	public static final LemminxPreference XML_PREFERENCES_CODELENS_ENABLED = new LemminxPreference("codeLens/enabled");

	// Folding settings
	public static final LemminxPreference XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD = new LemminxPreference(
			"foldings/includeClosingTagInFold");

	// Format settings

	// General
	public static final LemminxPreference XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH = new LemminxPreference(
			"format/maxLineWidth");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING = new LemminxPreference(
			"format/grammarAwareFormatting");

	// Format settings for Text content
	public static final LemminxPreference XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES = new LemminxPreference(
			"format/preservedNewlines");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES = new LemminxPreference(
			"format/joinContentLines");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE = new LemminxPreference(
			"format/insertFinalNewline");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES = new LemminxPreference(
			"format/trimFinalNewlines");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE = new LemminxPreference(
			"format/trimTrailingWhitespace");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_PRESERVE_SPACE = new LemminxPreference(
			"format/preserveSpace");

	// Format settings for Tag elements
	public static final LemminxPreference XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS = new LemminxPreference(
			"format/emptyElements");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG = new LemminxPreference(
			"format/spaceBeforeEmptyCloseTag");

	// Format settings for Attributes
	public static final LemminxPreference XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES = new LemminxPreference(
			"format/splitAttributes");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE = new LemminxPreference(
			"format/splitAttributesIndentSize");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS = new LemminxPreference(
			"format/preserveAttributeLineBreaks");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE = new LemminxPreference(
			"format/closingBracketNewLine");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT = new LemminxPreference(
			"format/xsiSchemaLocationSplit");
	public static final LemminxPreference XML_PREFERENCES_FORMAT_ENFORCE_QUOTE_STYLE = new LemminxPreference(
			"format/enforceQuoteStyle");

	// Format settings for comments
	public static final LemminxPreference XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES = new LemminxPreference(
			"format/joinCommentLines");

	// Format settings for comments
	public static final LemminxPreference XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES = new LemminxPreference(
			"format/joinCDATALines");

	// Validation settings
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_ENABLED = new LemminxPreference(
			"validation/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED = new LemminxPreference(
			"validation/namespaces/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED = new LemminxPreference(
			"validation/schema/enabled");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL = new LemminxPreference(
			"validation/disallowDocTypeDecl");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES = new LemminxPreference(
			"validation/resolveExternalEntities");
	public static final LemminxPreference XML_PREFERENCES_VALIDATION_NO_GRAMMAR = new LemminxPreference(
			"validation/noGrammar");

	private static final LemminxPreference[] ALL_LEMMINX_PREFERENCES = { //
			XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES, //
			XML_PREFERENCES_CATAGLOGS, //
			XML_PREFERENCES_CODELENS_ENABLED, //
			XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD, //
			XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH, //
			XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING, //
			XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES, //
			XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES, //
			XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE, //
			XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES, //
			XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE, //
			XML_PREFERENCES_FORMAT_PRESERVE_SPACE, //
			XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS, //
			XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG, //
			XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES, //
			XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE, //
			XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS, //
			XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE, //
			XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT, //
			XML_PREFERENCES_FORMAT_ENFORCE_QUOTE_STYLE, //
			XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES, //
			XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES, //
			XML_PREFERENCES_VALIDATION_ENABLED, //
			XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED, //
			XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED, //
			XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL, //
			XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES, //
			XML_PREFERENCES_VALIDATION_NO_GRAMMAR };

	public static Optional<LemminxPreference> getLemminxPreference(PropertyChangeEvent event) {
		return Arrays.stream(ALL_LEMMINX_PREFERENCES)
				.filter(pref -> Objects.equals(pref.preferenceId, event.getProperty())).findAny();
	}

	private XMLPreferenceServerConstants() {

	}

	public static void storePreferencesToLemminxOptions(IPreferenceStore store, Map<String, Object> xmlOpts) {
		// General settings
		setAsBoolean(XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES, store, xmlOpts);

		// Catalog settings
		XML_PREFERENCES_CATAGLOGS.storeToLemminxOptions(
				XMLCatalogs.getAllCatalogs(store).stream().map(File::getAbsolutePath).toArray(String[]::new), xmlOpts);

		// CodeLens settings
		setAsBoolean(XML_PREFERENCES_CODELENS_ENABLED, store, xmlOpts);

		// Folding settings
		setAsBoolean(XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD, store, xmlOpts);

		// Format settings
		setAsInt(XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE, store, xmlOpts);
		setAsArrayString(XML_PREFERENCES_FORMAT_PRESERVE_SPACE, store, xmlOpts);
		setAsInt(XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES, store, xmlOpts);
		setAsString(XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG, store, xmlOpts);
		setAsString(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES, store, xmlOpts);
		setAsInt(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE, store, xmlOpts);
		setAsString(XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT, store, xmlOpts);
		setAsString(XML_PREFERENCES_FORMAT_ENFORCE_QUOTE_STYLE, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES, store, xmlOpts);

		// Validation settings
		setAsBoolean(XML_PREFERENCES_VALIDATION_ENABLED, store, xmlOpts);
		setAsString(XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED, store, xmlOpts);
		setAsString(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL, store, xmlOpts);
		setAsBoolean(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES, store, xmlOpts);
		setAsString(XML_PREFERENCES_VALIDATION_NO_GRAMMAR, store, xmlOpts);
	}

	private static void setAsString(LemminxPreference preference, IPreferenceStore store, Map<String, Object> xmlOpts) {
		preference.storeToLemminxOptions(store.getString(preference.preferenceId), xmlOpts);
	}

	private static void setAsArrayString(LemminxPreference preference, IPreferenceStore store, Map<String, Object> xmlOpts) {
		String value = store.getString(preference.preferenceId);		
		preference.storeToLemminxOptions(value.split(","), xmlOpts);
	}
	
	private static void setAsBoolean(LemminxPreference preference, IPreferenceStore store,
			Map<String, Object> xmlOpts) {
		preference.storeToLemminxOptions(store.getBoolean(preference.preferenceId), xmlOpts);
	}

	private static void setAsInt(LemminxPreference preference, IPreferenceStore store, Map<String, Object> xmlOpts) {
		preference.storeToLemminxOptions(store.getInt(preference.preferenceId), xmlOpts);
	}

}
