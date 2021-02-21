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
package org.eclipse.wildwebdeveloper.xml.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	// --------- XML Catalog preference page

	public static String XMLPreferencePage_XMLCatalogsLink;

	public static String XMLCatalogPreferencePage_Entries;
	public static String XMLCatalogPreferencePage_Edit;
	public static String XMLCatalogPreferencePage_OpenInEditorTitle;
	public static String XMLCatalogPreferencePage_OpenInEditorMessage;
	public static String XMLCatalogPreferencePage_OpenInEditorApplyAndEdit;
	public static String XMLCatalogPreferencePage_OpenInEditorNo;

	// --------- XML Validation preference page

	public static String XMLValidationPreferencePage_validation_enabled;
	public static String XMLValidationPreferencePage_validation_namespaces_enabled;
	public static String XMLValidationPreferencePage_validation_schema_enabled;
	public static String XMLValidationPreferencePage_validation_disallowDocTypeDecl;
	public static String XMLValidationPreferencePage_validation_resolveExternalEntities;
	public static String XMLValidationPreferencePage_validation_noGrammar;

	// --------- Buttons
	
	public static String PreferencePage_Add;
	public static String PreferencePage_Remove;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.xml.internal.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
