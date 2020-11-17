/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper;

import org.eclipse.osgi.util.NLS;

public class SchemaAssociationsMessages extends NLS {

	public static String SchemaAssociations_PreferencePage_title;
	public static String Add;
	public static String Edit;
	public static String Remove;
	public static String SchemaAssociationDialog_Add_title;
	public static String SchemaAssociationDialog_Add_subtitle;
	public static String SchemaAssociationDialog_Edit_title;
	public static String SchemaAssociationDialog_Edit_subtitle;
	public static String ContentType;
	public static String ContentTypeId;
	public static String ContentTypeId_Tooltip;
	public static String SchemaLocation;
	public static String SchemaLocation_Tooltip;
	public static String Browse;
	public static String SchemaAssociationDialog_Error_ContentType_required;
	public static String SchemaAssociationDialog_Error_ContentType_already_exists;
	public static String SchemaAssociationDialog_Error_SchemaLocation_required;
	public static String SchemaAssociationDialog_Error_SchemaLocation_invalid;

	static {
		NLS.initializeMessages(SchemaAssociationsMessages.class.getName(), SchemaAssociationsMessages.class);
	}

	private SchemaAssociationsMessages() {
	}
}
