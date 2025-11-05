/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.markdown.ui.preferences;

import static org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.IncludeWorkspaceHeaderCompletions;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.PreferredMdPathExtensionStyle;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.ServerLog;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.ValidateEnabled;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.ValidateEnabledForFragmentLinks;

/**
 * Initializes default Markdown preferences.
 */
public final class MarkdownPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(MD_SERVER_LOG, ServerLog.off.value);
		store.setDefault(MD_OCCURRENCES_HIGHLIGHT_ENABLED, true);

		/*
		 * Suggest
		 */
		store.setDefault(MD_PREFERRED_MD_PATH_EXTENSION_STYLE, PreferredMdPathExtensionStyle.auto.value);
		store.setDefault(MD_SUGGEST_PATHS_ENABLED, true);
		store.setDefault(MD_SUGGEST_PATHS_INCLUDE_WKS_HEADER_COMPLETIONS, IncludeWorkspaceHeaderCompletions.onDoubleHash.value);
		store.setDefault(MD_SUGGEST_PATHS_EXCLUDE_GLOBS, "**/node_modules/**");

		/*
		 * Validation
		 */
		store.setDefault(MD_VALIDATE_ENABLED, true);
		store.setDefault(MD_VALIDATE_REFERENCE_LINKS_ENABLED, ValidateEnabled.warning.value);
		store.setDefault(MD_VALIDATE_FRAGMENT_LINKS_ENABLED, ValidateEnabled.warning.value);
		store.setDefault(MD_VALIDATE_FILE_LINKS_ENABLED, ValidateEnabled.warning.value);
		store.setDefault(MD_VALIDATE_FILE_LINKS_MARKDOWN_FRAGMENT_LINKS, ValidateEnabledForFragmentLinks.inherit.value);
		store.setDefault(MD_VALIDATE_IGNORED_LINKS, "");
		store.setDefault(MD_VALIDATE_UNUSED_LINK_DEFS_ENABLED, ValidateEnabled.warning.value);
		store.setDefault(MD_VALIDATE_DUPLICATE_LINK_DEFS_ENABLED, ValidateEnabled.error.value);
	}
}
