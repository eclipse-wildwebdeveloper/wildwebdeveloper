/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceClientConstants.XML_PREFERENCES_COMPLETION_AUTO_CLOSE_TAGS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_CATAGLOGS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_CODELENS_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_ENFORCE_QUOTE_STYLE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_PRESERVE_SPACE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_NO_GRAMMAR;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

/**
 * XML preference initializer.
 *
 */
public class XMLPreferenceInitializer extends AbstractPreferenceInitializer {

	private static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();

	private static final List<String> DEFAULT_PRESERVE_SPACE = Arrays.asList(//
			"xsl:text", //
			"xsl:comment", //
			"xsl:processing-instruction", //
			"literallayout", //
			"programlisting", //
			"screen", //
			"synopsis", //
			"pre", //
			"xd:pre");

	@Override
	public void initializeDefaultPreferences() {
		// Client settings
		STORE.setDefault(XML_PREFERENCES_COMPLETION_AUTO_CLOSE_TAGS, true);

		// Server settings
		STORE.setDefault(XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_CATAGLOGS.preferenceId, "");
		STORE.setDefault(XML_PREFERENCES_CODELENS_ENABLED.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD.preferenceId, true);

		STORE.setDefault(XML_PREFERENCES_FORMAT_MAX_LINE_WIDTH.preferenceId, 80);
		STORE.setDefault(XML_PREFERENCES_FORMAT_GRAMMAR_AWARE_FORMATTING.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_PRESERVED_NEW_LINES.preferenceId, 2);
		STORE.setDefault(XML_PREFERENCES_FORMAT_JOIN_CONTENT_LINES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_INSERT_FINAL_NEWLINE.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_TRIM_FINAL_NEWLINES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_TRIM_TRAILING_WHITESPACE.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_PRESERVE_SPACE.preferenceId,
				DEFAULT_PRESERVE_SPACE.stream().collect(Collectors.joining(",")));

		STORE.setDefault(XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS.preferenceId, "ignore");
		STORE.setDefault(XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES.preferenceId, "preserve");
		STORE.setDefault(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE.preferenceId, 2);
		STORE.setDefault(XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT.preferenceId, "onPair");
		STORE.setDefault(XML_PREFERENCES_FORMAT_ENFORCE_QUOTE_STYLE.preferenceId, "ignore");
		STORE.setDefault(XML_PREFERENCES_FORMAT_JOIN_COMMENT_LINES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_JOIN_CDATA_LINES.preferenceId, false);

		STORE.setDefault(XML_PREFERENCES_VALIDATION_ENABLED.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.preferenceId, "onNamespaceEncountered");
		STORE.setDefault(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.preferenceId, "always");
		STORE.setDefault(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_NO_GRAMMAR.preferenceId, "hint");
	}

}