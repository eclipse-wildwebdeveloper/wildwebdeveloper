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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;

/**
 * Label provider for {@link BindableCatalogEntry} items with styled text.
 * Displays {@code "TYPE  identifier  [GRAMMARTYPE]  — catalog.xml"}.
 * Catalog filename is shown in a lighter gray color.
 * When several catalog files share the same filename, the minimum number of
 * parent directory segments is prepended to disambiguate:
 * {@code "— project/catalog.xml"} or {@code "— org/project/catalog.xml"}.
 */
public class CatalogEntryLabelProvider extends LabelProvider implements IStyledLabelProvider {

	/** Maps each catalog URI to its shortest disambiguating path label. */
	private final Map<String, String> catalogLabels;

	public CatalogEntryLabelProvider(List<BindableCatalogEntry> entries) {
		catalogLabels = buildCatalogLabels(entries);
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof BindableCatalogEntry entry) {
			String type = entry.entryType != null ? entry.entryType.toUpperCase() : "?"; //$NON-NLS-1$
			String catalog = catalogLabels.getOrDefault(entry.catalogURI, ""); //$NON-NLS-1$

			StyledString result = new StyledString();
			result.append(type + "  " + entry.identifier); //$NON-NLS-1$
			if (!catalog.isEmpty()) {
				result.append("  — " + catalog, StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			}
			return result;
		}
		return new StyledString();
	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).getString();
	}

	/**
	 * Builds the shortest path label for every catalog URI in the entry list.
	 * URIs that share a filename are disambiguated by adding parent segments
	 * one at a time until all labels in the conflicting group are unique.
	 */
	private static Map<String, String> buildCatalogLabels(List<BindableCatalogEntry> entries) {
		// Collect distinct catalog URIs per filename
		Map<String, Set<String>> filenameToUris = new HashMap<>();
		for (BindableCatalogEntry entry : entries) {
			String filename = CatalogEntrySelectionDialog.catalogFileName(entry.catalogURI);
			if (!filename.isEmpty()) {
				filenameToUris.computeIfAbsent(filename, k -> new HashSet<>()).add(entry.catalogURI);
			}
		}

		Map<String, String> result = new HashMap<>();
		for (Map.Entry<String, Set<String>> e : filenameToUris.entrySet()) {
			Set<String> uris = e.getValue();
			if (uris.size() == 1) {
				// Unique filename: just the filename
				String uri = uris.iterator().next();
				result.put(uri, e.getKey());
			} else {
				// Ambiguous: find the minimum depth that makes all labels distinct
				result.putAll(disambiguate(uris));
			}
		}
		return result;
	}

	/**
	 * Returns a map from URI to the shortest path suffix that uniquely identifies
	 * each URI within the given set. Increases depth (number of trailing path
	 * segments) until all labels are distinct or depth exceeds 20.
	 */
	static Map<String, String> disambiguate(Set<String> uris) {
		Map<String, String> labels = new HashMap<>();
		for (int depth = 1; depth <= 20; depth++) {
			Map<String, List<String>> labelToUris = new HashMap<>();
			for (String uri : uris) {
				String label = lastSegments(uri, depth);
				labelToUris.computeIfAbsent(label, k -> new ArrayList<>()).add(uri);
			}
			// Assign labels for URIs that are now unique at this depth
			for (Map.Entry<String, List<String>> le : labelToUris.entrySet()) {
				if (le.getValue().size() == 1) {
					labels.putIfAbsent(le.getValue().get(0), le.getKey());
				}
			}
			if (labels.size() == uris.size()) {
				break;
			}
		}
		return labels;
	}

	/** Returns the last {@code n} slash-separated segments of {@code uri}. */
	static String lastSegments(String uri, int n) {
		if (uri == null || uri.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		String[] parts = uri.split("[/\\\\]"); //$NON-NLS-1$
		int start = Math.max(0, parts.length - n);
		return String.join("/", Arrays.copyOfRange(parts, start, parts.length)); //$NON-NLS-1$
	}
}
