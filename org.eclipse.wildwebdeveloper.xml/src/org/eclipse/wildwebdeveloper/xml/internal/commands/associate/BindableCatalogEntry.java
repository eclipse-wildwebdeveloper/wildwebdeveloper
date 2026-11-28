/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands.associate;

/**
 * A catalog entry that can be bound to an XML document, as returned by the
 * {@code xml.catalog.entries.bindable} LemMinX command.
 */
public class BindableCatalogEntry {

	/** URI of the catalog file that declares this entry. */
	public String catalogURI;

	/** Entry type: {@code "public"}, {@code "system"}, or {@code "uri"}. */
	public String entryType;

	/** The identifier: publicId, systemId, or uri-name attribute value. */
	public String identifier;

	/** The resolved grammar URI (file:/// or http(s)://). */
	public String targetURI;

	/** The raw {@code uri} attribute value from the catalog entry. */
	public String uriValue;

	/** Grammar type: {@code "xsd"}, {@code "dtd"}, {@code "rng"}, {@code "rnc"}. */
	public String grammarType;
}
