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

	public static String XMLPreferencesPage_XMLCatalogsLink;
	
	public static String XMLCatalogPreferencesPage_Entries;
	public static String XMLCatalogPreferencesPage_Edit;
	public static String XMLCatalogPreferencesPage_OpenInEditorTitle;
	public static String XMLCatalogPreferencesPage_OpenInEditorMessage;
	public static String XMLCatalogPreferencesPage_OpenInEditorApplyAndEdit;
	public static String XMLCatalogPreferencesPage_OpenInEditorNo;

	public static String PreferencesPage_Add;
	public static String PreferencesPage_Remove;

	static {
		NLS.initializeMessages("org.eclipse.wildwebdeveloper.xml.internal.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
