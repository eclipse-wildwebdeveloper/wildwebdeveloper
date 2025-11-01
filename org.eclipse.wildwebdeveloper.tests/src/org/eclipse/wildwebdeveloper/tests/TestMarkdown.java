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
package org.eclipse.wildwebdeveloper.tests;

import static org.eclipse.core.resources.IMarker.*;
import static org.eclipse.wildwebdeveloper.markdown.MarkdownDiagnosticsManager.MARKDOWN_MARKER_TYPE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

record MarkdownTest(String markdown, String messagePattern, int severity) {
}

@ExtendWith(AllCleanRule.class)
class TestMarkdown {

	@Test
	void diagnosticsCoverTypicalMarkdownIssues() throws Exception {
		var project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);

		final var markerTests = Collections.synchronizedCollection(new ArrayList<MarkdownTest>());
		markerTests
				.add(new MarkdownTest("Reference link to [an undefined reference][missing-ref]", "No link definition found: 'missing-ref'",
						SEVERITY_WARNING));
		markerTests.add(
				new MarkdownTest("Relative file link: [data](./nonexistent-folder/data.csv)", "File does not exist at path: .*data\\.csv",
						SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("Broken image: ![logo](../assets/logo.png)", "File does not exist at path: .*logo\\.png",
				SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("Link to missing header in this file: [Jump to Setup](#setup)", "No header found: 'setup'",
				SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("Link to missing header in another file: [See Guide](./GUIDE.md#installing)",
				"Header does not exist in file: installing",
				SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("Undefined footnote here [^missing-footnote]", "No link definition found: '\\^missing-footnote'",
				SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("This is a paragraph with an [undefined link][undefined-link].",
				"No link definition found: 'undefined-link'",
				SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("[unused-link]: https://unused-link.com", "Link definition is unused",
				SEVERITY_WARNING));
		markerTests.add(new MarkdownTest("""
			This is a paragraph with a [duplicate link][duplicate-link].
			[duplicate-link]: https://duplicate-link.com
			[duplicate-link]: https://duplicate-link.com
			""", "Link definition for 'duplicate-link' already exists", SEVERITY_ERROR));

		final IFile referencedFile = project.getFile("GUIDE.md");
		referencedFile.create("".getBytes(), true, false, null);

		final IFile file = project.getFile("broken.md");
		file.create(markerTests.stream().map(MarkdownTest::markdown).collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8),
				true,
				false, null);

		final var editor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		final var display = editor.getSite().getShell().getDisplay();
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		/*
		 * ensure Markdown Language Server is started and connected
		 */
		final var markdownLS = new AtomicReference<LanguageServerWrapper>();
		DisplayHelper.waitForCondition(display, 10_000, () -> {
			markdownLS.set(LanguageServiceAccessor.getStartedWrappers(doc, null, false).stream() //
					.filter(w -> "org.eclipse.wildwebdeveloper.markdown".equals(w.serverDefinition.id)) //
					.findFirst().orElse(null));
			return markdownLS.get() != null //
					&& markdownLS.get().isActive() //
					&& markdownLS.get().isConnectedTo(LSPEclipseUtils.toUri(doc));
		});

		// Wait until all expected diagnostics are present (by message fragments)
		DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 15_000, () -> {
			try {
				final var markers = file.findMarkers(MARKDOWN_MARKER_TYPE, true, IResource.DEPTH_ZERO);
				if (markers.length == 0)
					return false;

				for (final IMarker m : markers) {
					final Object msgObj = m.getAttribute(IMarker.MESSAGE);
					if (!(msgObj instanceof final String msg))
						continue;
					markerTests.removeIf(t -> t.severity() == m.getAttribute(IMarker.SEVERITY, -1) &&
							msg.matches(t.messagePattern()));
				}
				return markerTests.isEmpty();
			} catch (CoreException e) {
				return false;
			}
		});

		assertTrue(markerTests.isEmpty(), "The following markers were not found: " + markerTests);
	}
}
