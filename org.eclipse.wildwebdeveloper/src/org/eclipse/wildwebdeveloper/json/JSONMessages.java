/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.wildwebdeveloper.json;

import org.eclipse.osgi.util.NLS;

public class JSONMessages extends NLS {

	public static String JSON_PreferencePage_title;
	public static String JSON_Schema_PreferencePage_title;
	public static String Add;
	public static String Edit;
	public static String Remove;
	public static String EntryDialog_AddSchema_title;
	public static String EntryDialog_AddSchema_subtitle;
	public static String EntryDialog_EditSchema_title;
	public static String EntryDialog_EditSchema_subtitle;
	public static String FilePattern;
	public static String FilePattern_Tooltip;
	public static String SchemaLocation;
	public static String SchemaLocation_Tooltip;
	public static String Browse;
	public static String EntryDialog_Error_FilePattern_required;
	public static String EntryDialog_Error_FilePattern_already_exists;
	public static String EntryDialog_Error_SchemaLocation_required;
	public static String EntryDialog_Error_SchemaLocation_invalid;

	static {
		NLS.initializeMessages(JSONMessages.class.getName(), JSONMessages.class);
	}

	private JSONMessages() {
	}
}
