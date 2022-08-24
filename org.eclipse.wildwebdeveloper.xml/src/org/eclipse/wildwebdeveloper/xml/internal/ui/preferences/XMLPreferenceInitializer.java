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
		STORE.setDefault(XML_PREFERENCES_CATAGLOGS.preferenceId, "");
		STORE.setDefault(XML_PREFERENCES_CODELENS_ENABLED.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_ENABLED.preferenceId, true);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.preferenceId, "onNamespaceEncountered");
		STORE.setDefault(XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.preferenceId, "always");
		STORE.setDefault(XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.preferenceId, false);
		STORE.setDefault(XML_PREFERENCES_VALIDATION_NO_GRAMMAR.preferenceId, "hint");
	}

}