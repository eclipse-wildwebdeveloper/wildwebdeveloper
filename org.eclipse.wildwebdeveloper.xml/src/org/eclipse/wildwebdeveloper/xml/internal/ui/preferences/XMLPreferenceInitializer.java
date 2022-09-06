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

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_CATAGLOGS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_CODELENS_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_JOIN_COMMENTLINES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_NO_GRAMMAR;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES;
import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

public class XMLPreferenceInitializer extends AbstractPreferenceInitializer {
	private static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();

	@Override
	public void initializeDefaultPreferences() {
		STORE.setDefault(XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_CATAGLOGS.preferenceId, "");
		STORE.setDefault(XML_PREFERENCES_CODELENS_ENABLED.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FOLDING_INCLUDE_CLOSING_TAG_IN_FOLD.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_FORMAT_EMPTY_ELEMENTS.preferenceId, "ignore");
		STORE.setDefault(XML_PREFERENCES_FORMAT_SPACE_BEFORE_EMPTY_CLOSE_TAG.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_SPLIT_ATTRIBUTES_INDENT_SIZE.preferenceId, 2);
		STORE.setDefault(XML_PREFERENCES_FORMAT_CLOSING_BRACKET_NEW_LINE.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_FORMAT_PRESERVE_ATTRIBUTE_LINE_BREAKS.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_FORMAT_XSI_SCHEMA_LOCATION_SPLIT.preferenceId, "onPair");
		STORE.setDefault(XML_PREFERENCES_FORMAT_JOIN_COMMENTLINES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_ENABLED.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.preferenceId, "onNamespaceEncountered");
		STORE.setDefault(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.preferenceId, "always");
		STORE.setDefault(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_NO_GRAMMAR.preferenceId, "hint");
	}

}