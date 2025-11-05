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

import java.lang.reflect.Field;
import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.IncludeWorkspaceHeaderCompletions;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.PreferredMdPathExtensionStyle;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.ServerLog;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.ValidateEnabled;
import org.eclipse.wildwebdeveloper.markdown.ui.preferences.MarkdownPreferences.ValidateEnabledForFragmentLinks;

/**
 * Markdown main preference page.
 */
public final class MarkdownPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public MarkdownPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	private static <E extends Enum<E>> String[][] toLabelValueArray(final Class<E> enumClass) {
		try {
			final Field labelField = enumClass.getDeclaredField("label");
			final Field valueField = enumClass.getDeclaredField("value");
			return Arrays.stream(enumClass.getEnumConstants())
					.map(enumValue -> {
						try {
							return new String[] { //
								(String) labelField.get(enumValue), //
								(String) valueField.get(enumValue) //
							};
						} catch (final IllegalAccessException ex) {
							throw new RuntimeException(ex);
						}
					})
					.toArray(String[][]::new);
		} catch (final NoSuchFieldException ex) {
			throw new IllegalArgumentException(enumClass.getName() + " must have 'label' and 'value' fields");
		}
	}

	private BooleanFieldEditor validateEnabledEditor;
	private Composite validationGroup;

	@Override
	protected void createFieldEditors() {
		final Composite pageParent = getFieldEditorParent();
		// General
		addField(new ComboFieldEditor(
				MD_SERVER_LOG,
				"Server log level",
				toLabelValueArray(ServerLog.class),
				pageParent));

		// Occurrences
		addField(new BooleanFieldEditor(MD_OCCURRENCES_HIGHLIGHT_ENABLED, "Highlight link occurrences", pageParent));

		// Suggestions
		final var suggestionsGroup = createPaddedGroup(pageParent, "Suggestions");
		addField(new ComboFieldEditor(
				MD_PREFERRED_MD_PATH_EXTENSION_STYLE,
				"Path suggestions: add file extensions (e.g. `.md`) for links to Markdown files?",
				toLabelValueArray(PreferredMdPathExtensionStyle.class),
				suggestionsGroup));
		addField(new BooleanFieldEditor(MD_SUGGEST_PATHS_ENABLED, "Enable path suggestions while writing links",
				suggestionsGroup));
		addField(new ComboFieldEditor(
				MD_SUGGEST_PATHS_INCLUDE_WKS_HEADER_COMPLETIONS,
				"Enable suggestions for headers in other Markdown files",
				toLabelValueArray(IncludeWorkspaceHeaderCompletions.class),
				suggestionsGroup));

		final var excludeGlobs = new StringFieldEditor(
				MD_SUGGEST_PATHS_EXCLUDE_GLOBS,
				"Excluded path suggestion globs (comma-separated)",
				suggestionsGroup);
		addField(excludeGlobs);
		final String excludeTooltip = """
			Glob patterns to exclude Markdown files from path suggestions.
			Matched against project-relative paths, for example:
			• **/node_modules/**
			• docs/generated/**
			• **/drafts/**
			• **/*.tmp.md
			""";
		excludeGlobs.getLabelControl(suggestionsGroup).setToolTipText(excludeTooltip);
		excludeGlobs.getTextControl(suggestionsGroup).setToolTipText(excludeTooltip);

		// Validation
		validateEnabledEditor = new BooleanFieldEditor(MD_VALIDATE_ENABLED, "Enable validation", pageParent);
		addField(validateEnabledEditor);

		validationGroup = createPaddedGroup(pageParent, "Validation settings");
		addField(new ComboFieldEditor(
				MD_VALIDATE_REFERENCE_LINKS_ENABLED,
				"Reference links validation severity [text][ref]",
				toLabelValueArray(ValidateEnabled.class),
				validationGroup));
		addField(new ComboFieldEditor(
				MD_VALIDATE_FRAGMENT_LINKS_ENABLED,
				"Fragment links validation severity [text](#head)",
				toLabelValueArray(ValidateEnabled.class),
				validationGroup));
		addField(new ComboFieldEditor(
				MD_VALIDATE_FILE_LINKS_ENABLED,
				"File links validation severity",
				toLabelValueArray(ValidateEnabled.class),
				validationGroup));
		addField(new ComboFieldEditor(
				MD_VALIDATE_FILE_LINKS_MARKDOWN_FRAGMENT_LINKS,
				"Fragment part of links to headers in other files in Markdown files",
				toLabelValueArray(ValidateEnabledForFragmentLinks.class),
				validationGroup));

		final var ignoredLinks = new StringFieldEditor(
				MD_VALIDATE_IGNORED_LINKS,
				"Ignored link globs (comma-separated)",
				validationGroup);
		addField(ignoredLinks);
		final String ignoredTooltip = """
			Glob patterns are matched against the link destination (href).

			Suppresses:
			• Missing file -> match the path only
			    e.g. docs/generated/**, **/images/**
			• Missing header (same file) -> match '#fragment'
			    e.g. #*, #intro
			• Missing header (other file) -> match 'path#fragment' or just the path
			    e.g. /guide.md#*, docs/guide.md#intro, /guide.md
			""";
		ignoredLinks.getLabelControl(validationGroup).setToolTipText(ignoredTooltip);
		ignoredLinks.getTextControl(validationGroup).setToolTipText(ignoredTooltip);

		addField(new ComboFieldEditor(
				MD_VALIDATE_UNUSED_LINK_DEFS_ENABLED,
				"Unused link definitions severity",
				toLabelValueArray(ValidateEnabled.class),
				validationGroup));
		addField(new ComboFieldEditor(
				MD_VALIDATE_DUPLICATE_LINK_DEFS_ENABLED,
				"Duplicated definitions severity",
				toLabelValueArray(ValidateEnabled.class),
				validationGroup));
	}

	@Override
	protected void initialize() {
		super.initialize();
		// Ensure enablement matches loaded preference values after editors are initialized
		updateValidationGroupEnablement();
		normalizeComboWidths(getFieldEditorParent());
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() == validateEnabledEditor) {
			updateValidationGroupEnablement();
		}
	}

	private void updateValidationGroupEnablement() {
		final boolean enabled = validateEnabledEditor != null && validateEnabledEditor.getBooleanValue();
		setEnabledRecursive(validationGroup, enabled);
	}

	private static void setEnabledRecursive(final Control control, final boolean enabled) {
		if (control == null || control.isDisposed())
			return;

		control.setEnabled(enabled);
		if (control instanceof final Composite comp) {
			for (final Control child : comp.getChildren()) {
				setEnabledRecursive(child, enabled);
			}
		}
	}

	public static Composite createPaddedGroup(final Composite parent, final String title) {
		final int marginWidth = 12;
		final int marginHeight = 8;
		final int spacingX = 8;
		final int spacingY = 6;
		final var group = new Group(parent, SWT.NONE);
		if (title != null)
			group.setText(title);

		group.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false)
				.span(2 /* Field editors use 2-column grids */, 0)
				.create());

		group.setLayout(new GridLayout(1, false));

		final var body = new Composite(group, SWT.NONE);
		body.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false)
				.create());

		body.setLayout(GridLayoutFactory.swtDefaults()
				.numColumns(2 /* Field editors use 2-column grids */)
				.margins(marginWidth, marginHeight)
				.spacing(spacingX, spacingY)
				.create());

		return body;
	}

	private void normalizeComboWidths(final Composite container) {
		if (container == null || container.isDisposed())
			return;

		final var gc = new GC(container);
		try {
			for (final Control child : container.getChildren()) {
				if (child instanceof final Composite comp) {
					normalizeComboWidths(comp);
				}
				if (child instanceof final Combo combo) {
					final var gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);

					int max = 0;
					for (final String item : combo.getItems()) {
						final Point p = gc.textExtent(item);
						if (p.x > max)
							max = p.x;
					}
					gd.widthHint = max;
					child.setLayoutData(gd);
				}
			}
		} finally {
			gc.dispose();
		}
		container.layout(true, true);
	}
}
