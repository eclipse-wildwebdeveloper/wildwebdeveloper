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

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

/**
 * XML preference constants. Name of preferences matches the following pattern:
 *
 * <p>
 * org.eclipse.wildwebdeveloper.xml (plugin id) concat with LemMinX settings
 * name (without the xml prefix).
 * </p>
 * 
 * @see https://github.com/redhat-developer/vscode-xml#supported-vs-code-settings
 */
public class XMLPreferenceConstants {

	public static final String XML_PREFERENCES_CATAGLOGS = Activator.PLUGIN_ID + ".catalogs";
	public static final String XML_PREFERENCES_VALIDATION_ENABLED = Activator.PLUGIN_ID + ".validation.enabled";
	public static final String XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED = Activator.PLUGIN_ID
			+ ".validation.namespaces.enabled";
	public static final String XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED = Activator.PLUGIN_ID
			+ ".validation.schema.enabled";
	public static final String XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL = Activator.PLUGIN_ID
			+ ".validation.disallowDocTypeDecl";
	public static final String XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES = Activator.PLUGIN_ID
			+ ".validation.resolveExternalEntities";
	public static final String XML_PREFERENCES_VALIDATION_NO_GRAMMAR = Activator.PLUGIN_ID + ".validation.noGrammar";

	public static boolean isXMLProperty(PropertyChangeEvent event) {
		String property = event.getProperty();
		return XML_PREFERENCES_CATAGLOGS.equals(property) || XML_PREFERENCES_VALIDATION_ENABLED.equals(property)
				|| XML_PREFERENCES_VALIDATION_NAMESPACES_ENABLED.equals(property)
				|| XML_PREFERENCES_VALIDATION_SCHEMA_ENABLED.equals(property)
				|| XML_PREFERENCES_VALIDATION_DISALLOW_DOCTYPE_DECL.equals(property)
				|| XML_PREFERENCES_VALIDATION_RESOLVE_EXTERNAL_ENTITIES.equals(property)
				|| XML_PREFERENCES_VALIDATION_NO_GRAMMAR.equals(property);
	}

	private XMLPreferenceConstants() {

	}
}
