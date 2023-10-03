/*******************************************************************************
 * Copyright (c) 2023 Dawid Pakuła and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Dawid Pakuła - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.ui.preferences.javascript;

import org.eclipse.lsp4e.configuration.EclipsePreferenceProvider;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.typescript.TypeScriptPreferenceServerConstants;

public class JSTSConfigurationProvider extends EclipsePreferenceProvider {

	public JSTSConfigurationProvider() {
		super(Activator.PLUGIN_ID);
		
		addBool(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_ENUM_MEMBER_VALUE_HINTS);
		addBool(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_LIKE_RETURN_TYPE_HINTS);
		addBool(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_PARAMETER_TYPE_HINTS);
		add(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS);
		addBool(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS_WHEN_ARGUMENT_MATCHES_NAME);
		addBool(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PROPERTY_DECLARATION_TYPE_HINTS);
		addBool(JavaScriptPreferenceServerConstants.JAVASCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_VARIABLE_TYPE_HINTS_WHEN_TYPE_MATCHES_NAME);
		
		addBool(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_ENUM_MEMBER_VALUE_HINTS);
		addBool(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_LIKE_RETURN_TYPE_HINTS);
		addBool(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_FUNCTION_PARAMETER_TYPE_HINTS);
		add(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS);
		addBool(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PARAMETER_NAME_HINTS_WHEN_ARGUMENT_MATCHES_NAME);
		addBool(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_PROPERTY_DECLARATION_TYPE_HINTS);
		addBool(TypeScriptPreferenceServerConstants.TYPESCRIPT_PREFERENCES_INLAY_HINTS_INCLUDE_INLAY_VARIABLE_TYPE_HINTS_WHEN_TYPE_MATCHES_NAME);
	}

}
