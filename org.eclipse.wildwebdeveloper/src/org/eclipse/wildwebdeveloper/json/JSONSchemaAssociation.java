/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json;

import java.util.Objects;

public class JSONSchemaAssociation {

	private String contentType;
	private String contentTypeId;
	private String schemaLocation;

	public JSONSchemaAssociation(String contentType, String contentTypeId, String schemaLocation) {
		this.setContentType(contentType);
		this.setContentTypeId(contentTypeId);
		this.setSchemaLocation(schemaLocation);
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentTypeId() {
		return contentTypeId;
	}

	public void setContentTypeId(String contentTypeId) {
		this.contentTypeId = contentTypeId;
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
		JSONSchemaAssociation association = (JSONSchemaAssociation) o;
		return Objects.equals(contentType, association.contentType)
				&& Objects.equals(contentTypeId, association.contentTypeId)
				&& Objects.equals(schemaLocation, association.schemaLocation);
	}

}
