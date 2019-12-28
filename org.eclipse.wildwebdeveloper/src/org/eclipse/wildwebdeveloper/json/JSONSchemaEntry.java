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

import java.util.Objects;

public class JSONSchemaEntry {

	private String filePattern;
	private String schemaLocation;

	public JSONSchemaEntry(String filePattern, String schemaLocation) {
		this.setFilePattern(filePattern);
		this.setSchemaLocation(schemaLocation);
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		JSONSchemaEntry entry = (JSONSchemaEntry) o;
		return Objects.equals(filePattern, entry.filePattern) && Objects.equals(schemaLocation, entry.schemaLocation);
	}

}
