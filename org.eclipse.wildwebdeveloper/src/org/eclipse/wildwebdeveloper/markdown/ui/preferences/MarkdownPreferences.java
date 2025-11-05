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

import static org.eclipse.wildwebdeveloper.ui.preferences.Settings.isMatchSection;

import org.eclipse.jface.preference.IPreferenceStore;
import java.util.Arrays;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.ui.preferences.Settings;

/**
 * Markdown preference server constants and helpers.
 *
 * See https://github.com/microsoft/vscode-markdown-languageserver/blob/c827107fea3b3252c9f769caf3ba4901159fec84/src/configuration.ts#L11
 */
public final class MarkdownPreferences {

	enum ServerLog {
		off("Off"),
		debug("Debug"),
		trace("Trace");

		private ServerLog(final String label) {
			this.value = toString();
			this.label = label;
		}

		final String value;
		final String label;
	}

	enum PreferredMdPathExtensionStyle {
		auto("Auto"),
		includeExtension("Include .md extension"),
		removeExtension("Remove .md extension");

		private PreferredMdPathExtensionStyle(final String label) {
			this.value = toString();
			this.label = label;
		}

		final String value;
		final String label;
	}

	enum IncludeWorkspaceHeaderCompletions {
		never("Never"),
		onSingleOrDoubleHash("On single or double '#'"),
		onDoubleHash("Only on double '##'");

		private IncludeWorkspaceHeaderCompletions(final String label) {
			this.value = toString();
			this.label = label;
		}

		final String value;
		final String label;
	}

	enum ValidateEnabled {
		ignore("Ignore"),
		warning("Warning"),
		error("Error"),
		hint("Hint");

		private ValidateEnabled(final String label) {
			this.value = toString();
			this.label = label;
		}

		final String value;
		final String label;
	}

	enum ValidateEnabledForFragmentLinks {
		ignore("Ignore"),
		warning("Warning"),
		error("Error"),
		hint("Hint"),
		inherit("Inherit");

		private ValidateEnabledForFragmentLinks(final String label) {
			this.value = toString();
			this.label = label;
		}

		final String value;
		final String label;
	}

	private static final String MD_SECTION = "markdown";

	static final String MD_SERVER_LOG = MD_SECTION + ".server.log"; // ServerLog

	static final String MD_OCCURRENCES_HIGHLIGHT_ENABLED = MD_SECTION + ".occurrencesHighlight.enabled"; // boolean

	/*
	 * Suggest
	 */
	static final String MD_PREFERRED_MD_PATH_EXTENSION_STYLE = MD_SECTION + ".preferredMdPathExtensionStyle"; // PreferredMdPathExtensionStyle
	static final String MD_SUGGEST_PATHS_ENABLED = MD_SECTION + ".suggest.paths.enabled"; // boolean
	static final String MD_SUGGEST_PATHS_INCLUDE_WKS_HEADER_COMPLETIONS = MD_SECTION
			+ ".suggest.paths.includeWorkspaceHeaderCompletions"; // IncludeWorkspaceHeaderCompletions


	// Note: MD_SUGGEST_PATHS_EXCLUDE_GLOBS is a client-only preference used for
	// filtering path suggestions on the Eclipse side and is not sent to the server.
	static final String MD_SUGGEST_PATHS_EXCLUDE_GLOBS = MD_SECTION + ".suggest.paths.excludeGlobs"; // comma list

	/*
	 * Validation
	 */
	private static final String MD_VADLIDATE_SECTION = MD_SECTION + ".validate";
	static final String MD_VALIDATE_ENABLED = MD_VADLIDATE_SECTION + ".enabled"; // boolean
	static final String MD_VALIDATE_REFERENCE_LINKS_ENABLED = MD_VADLIDATE_SECTION + ".referenceLinks.enabled"; // ValidateEnabled
	static final String MD_VALIDATE_FRAGMENT_LINKS_ENABLED = MD_VADLIDATE_SECTION + ".fragmentLinks.enabled"; // ValidateEnabled
	static final String MD_VALIDATE_FILE_LINKS_ENABLED = MD_VADLIDATE_SECTION + ".fileLinks.enabled"; // ValidateEnabled
	static final String MD_VALIDATE_FILE_LINKS_MARKDOWN_FRAGMENT_LINKS = MD_VADLIDATE_SECTION + ".fileLinks.markdownFragmentLinks"; // ValidateEnabledForFragmentLinks
	static final String MD_VALIDATE_IGNORED_LINKS = MD_VADLIDATE_SECTION + ".ignoredLinks"; // comma list
	static final String MD_VALIDATE_UNUSED_LINK_DEFS_ENABLED = MD_VADLIDATE_SECTION + ".unusedLinkDefinitions.enabled"; // ValidateEnabled
	static final String MD_VALIDATE_DUPLICATE_LINK_DEFS_ENABLED = MD_VADLIDATE_SECTION + ".duplicateLinkDefinitions.enabled"; // ValidateEnabled

	public static Settings getGlobalSettings() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final var settings = new Settings(store);

		settings.fillAsString(MD_SERVER_LOG);
		settings.fillAsBoolean(MD_OCCURRENCES_HIGHLIGHT_ENABLED);

		/*
		 * Suggest
		 */
		settings.fillAsString(MD_PREFERRED_MD_PATH_EXTENSION_STYLE);
		settings.fillAsBoolean(MD_SUGGEST_PATHS_ENABLED);
		settings.fillSetting(MD_SUGGEST_PATHS_INCLUDE_WKS_HEADER_COMPLETIONS,
				store.getString(MD_SUGGEST_PATHS_INCLUDE_WKS_HEADER_COMPLETIONS));

		/*
		 * Validation
		 */
		// Top-level enabled is boolean true per server type
		settings.fillAsBoolean(MD_VALIDATE_ENABLED);
		settings.fillAsString(MD_VALIDATE_REFERENCE_LINKS_ENABLED);
		settings.fillAsString(MD_VALIDATE_FRAGMENT_LINKS_ENABLED);
		settings.fillAsString(MD_VALIDATE_FILE_LINKS_ENABLED);
		settings.fillAsString(MD_VALIDATE_FILE_LINKS_MARKDOWN_FRAGMENT_LINKS);
		// Build ignoredLinks as array, filtering out empty entries to avoid server error
		final String rawIgnored = store.getString(MD_VALIDATE_IGNORED_LINKS);
		if (rawIgnored == null || rawIgnored.trim().isEmpty()) {
			settings.fillSetting(MD_VALIDATE_IGNORED_LINKS, new String[0]);
		} else {
			final String[] globs = Arrays.stream(rawIgnored.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.toArray(String[]::new);
			settings.fillSetting(MD_VALIDATE_IGNORED_LINKS, globs);
		}
		settings.fillAsString(MD_VALIDATE_UNUSED_LINK_DEFS_ENABLED);
		settings.fillAsString(MD_VALIDATE_DUPLICATE_LINK_DEFS_ENABLED);
		return settings;
	}

	/**
	 * Returns comma-separated glob patterns from preferences for excluding files from
	 * Markdown path suggestions. Empty or blank entries are filtered out.
	 */
	public static String[] getSuggestPathsExcludeGlobs() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final String raw = store.getString(MD_SUGGEST_PATHS_EXCLUDE_GLOBS);
		if (raw == null || raw.trim().isEmpty()) {
			return new String[0];
		}
		return Arrays.stream(raw.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

	public static boolean isMatchMarkdownSection(final String section) {
		return isMatchSection(section, MD_SECTION);
	}

	private MarkdownPreferences() {
	}
}
