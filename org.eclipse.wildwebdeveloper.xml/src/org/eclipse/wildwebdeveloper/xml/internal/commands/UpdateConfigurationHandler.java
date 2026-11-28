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
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLCatalogs;

/**
 * Handles the {@code xml.update.configuration} command sent by LemMinX when the
 * user clicks the "Register Catalog" or "Unregister Catalog" code lens on an
 * XML catalog file.
 *
 * <p>
 * The command carries a {@code ConfigurationItemEdit} argument specifying:
 * <ul>
 * <li>{@code section} — the setting to update (e.g.
 * {@code "xml.catalogs"})</li>
 * <li>{@code value} — the catalog file URI to add or remove</li>
 * <li>{@code editType} — {@code "Add"} or {@code "Delete"}</li>
 * </ul>
 * Currently only {@code "xml.catalogs"} is supported.
 * </p>
 *
 * @author Angelo ZERR
 */
public class UpdateConfigurationHandler extends LSPCommandHandler {

	private static final String SECTION_XML_CATALOGS = "xml.catalogs";

	/**
	 * Configuration item edit type. Represents a configuration edit operation
	 */
	// Enum mapping configuration edit operations (Add=1, Delete=2) to ordinal values for Gson deserialization
	private static enum ConfigurationItemEditType {
		// Add a new catalog URI to the configuration
		Add(0),

		// Delete a catalog URI from the configuration
		Delete(1);

		private final int value;

		ConfigurationItemEditType(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ConfigurationItemEditType forValue(final int value) {
			ConfigurationItemEditType[] allValues = ConfigurationItemEditType.values();
			if (value < 1 || value > allValues.length) {
				throw new IllegalArgumentException("Illegal enum value: " + value);
			}
			return allValues[value - 1];
		}
	}

	// Record holding a configuration edit request: section to edit, value to add/delete, and edit type (Add/Delete)
	private static record ConfigurationItemEdit(String section, String value, ConfigurationItemEditType editType) {
	}

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		// Extract configuration edit request from LSP command arguments
		ConfigurationItemEdit edit = ArgumentsUtils.getArgAt(command, 0, ConfigurationItemEdit.class);
		if (edit == null) {
			return null;
		}
		String section = edit.section();
		String value = edit.value();
		ConfigurationItemEditType editType = edit.editType();

		// Validate required fields before proceeding
		if (section == null || value == null || editType == null) {
			return null;
		}
		// Currently only handle xml.catalogs configuration; other sections can be added here
		if (SECTION_XML_CATALOGS.equals(section)) {
			updateCatalogs(value, editType);
		}
		return null;
	}

	private static void updateCatalogs(String catalogUri, ConfigurationItemEditType editType) {
		try {
			// Convert URI string to File object for preference store
			File catalogFile = new File(URI.create(catalogUri));
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			Set<File> catalogs = XMLCatalogs.getUserCatalogs(store);

			// Add or remove catalog from user preferences based on edit type
			if (editType == ConfigurationItemEditType.Add) {
				catalogs.add(catalogFile);
			} else if (editType == ConfigurationItemEditType.Delete) {
				catalogs.remove(catalogFile);
			}
			// Persist the updated set of catalogs back to preference store
			XMLCatalogs.storeUserCatalogs(store, catalogs);
		} catch (Exception e) {
			ILog.get().error("Failed to update XML catalog configuration for URI: " + catalogUri, e);
		}
	}
}
