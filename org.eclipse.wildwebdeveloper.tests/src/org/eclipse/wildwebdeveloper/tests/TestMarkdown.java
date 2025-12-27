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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.markdown.MarkdownDiagnosticsManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

record MarkdownTest(String markdown, String messagePattern, int severity) {
}

@ExtendWith(AllCleanRule.class)
class TestMarkdown {

	private record DiagnosticSpy(AtomicInteger calls,
			AtomicReference<CompletableFuture<DocumentDiagnosticReport>> lastFuture, LanguageServer server) {
	}

	private static DiagnosticSpy newDiagnosticSpy() {
		final var calls = new AtomicInteger();
		final var lastFuture = new AtomicReference<CompletableFuture<DocumentDiagnosticReport>>();

		final Object textDocumentService = Proxy.newProxyInstance(TestMarkdown.class.getClassLoader(),
				new Class[] { org.eclipse.lsp4j.services.TextDocumentService.class }, (proxy, method, args) -> {
					if ("diagnostic".equals(method.getName()) && args != null && args.length == 1
							&& args[0] instanceof DocumentDiagnosticParams) {
						calls.incrementAndGet();
						final var fut = new CompletableFuture<DocumentDiagnosticReport>();
						lastFuture.set(fut);
						return fut;
					}
					if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
						return CompletableFuture.completedFuture(null);
					}
					return null;
				});

		final InvocationHandler serverHandler = (proxy, method, args) -> (switch (method.getName()) {
		case "getTextDocumentService" -> textDocumentService;
		case "getWorkspaceService" -> null;
		case "initialize", "shutdown" -> CompletableFuture.completedFuture(null);
		case "exit" -> null;
		default -> null;
		});

		final var server = (LanguageServer) Proxy.newProxyInstance(TestMarkdown.class.getClassLoader(),
				new Class[] { LanguageServer.class }, serverHandler);
		return new DiagnosticSpy(calls, lastFuture, server);
	}

	private static boolean waitUpTo(final long timeoutMs, final BooleanSupplier condition)
			throws InterruptedException {
		final long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (condition.getAsBoolean())
				return true;
			Thread.sleep(20);
		}
		return condition.getAsBoolean();
	}

	@Test
	void refreshDiagnosticsDoesNothingWhenNoMarkdownBuffersOpen() throws Exception {
		final var project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getName() + ".nobuf." + System.nanoTime());
		project.create(null);
		project.open(null);

		final IFile file = project.getFile("doc.md");
		file.create("# Title\n".getBytes(StandardCharsets.UTF_8), true, false, null);

		final var spy = newDiagnosticSpy();
		MarkdownDiagnosticsManager.refreshAllOpenMarkdownFiles(spy.server());

		// Wait for debounce window + execution time; should still do nothing since no
		// Markdown buffer is open.
		assertTrue(waitUpTo(2_000, () -> spy.calls().get() == 0),
				"Diagnostic requests should not be made when no Markdown buffers are open");
	}

	@Test
	void refreshDiagnosticsIsDedupedWhileInFlight() throws Exception {
		final var project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getName() + ".dedupe." + System.nanoTime());
		project.create(null);
		project.open(null);

		final IFile file = project.getFile("open.md");
		file.create("# Title\n".getBytes(StandardCharsets.UTF_8), true, false, null);

		final var mgr = FileBuffers.getTextFileBufferManager();
		mgr.connect(file.getFullPath(), LocationKind.IFILE, null);
		try {
			final var spy = newDiagnosticSpy();

			MarkdownDiagnosticsManager.refreshAllOpenMarkdownFiles(spy.server());
			assertTrue(waitUpTo(2_000, () -> spy.calls().get() == 1),
					"Expected exactly one diagnostic request for the open Markdown buffer");

			// Trigger another refresh while the first diagnostic is still in-flight; should
			// not start a second diagnostic.
			MarkdownDiagnosticsManager.refreshAllOpenMarkdownFiles(spy.server());
			assertTrue(waitUpTo(2_000, () -> spy.calls().get() == 1), "Expected in-flight refresh to be de-duplicated");

			final var fut = spy.lastFuture().get();
			if (fut != null && !fut.isDone()) {
				fut.complete(null);
			}
		} finally {
			mgr.disconnect(file.getFullPath(), LocationKind.IFILE, null);
		}
	}

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

	@Test
	void workspaceHeaderCompletionsRespectExcludeGlobs() throws Exception {
		var project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + ".hdr" + System.nanoTime());
		project.create(null);
		project.open(null);

		// Configure exclusion: exclude docs/generated/** from workspace header completions
		Activator.getDefault().getPreferenceStore().setValue("markdown.suggest.paths.excludeGlobs", "docs/generated/**");

		// Create markdown files with unique headers
		// Ensure folders exist
		var docsFolder = project.getFolder("docs");
		if (!docsFolder.exists())
			docsFolder.create(true, true, null);
		var genFolder = docsFolder.getFolder("generated");
		if (!genFolder.exists())
			genFolder.create(true, true, null);

		IFile excluded = project.getFile("docs/generated/excluded.md");
		excluded.create("# Excluded Only\n".getBytes(StandardCharsets.UTF_8), true, false, null);

		IFile included = project.getFile("docs/included.md");
		included.create("# Included Only\n".getBytes(StandardCharsets.UTF_8), true, false, null);

		// File where we'll trigger completions (double hash to respect default preference)
		IFile index = project.getFile("index.md");
		index.create("[](##)\n".getBytes(StandardCharsets.UTF_8), true, false, null);

		var editor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), index);
		var display = editor.getSite().getShell().getDisplay();
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// Ensure Markdown Language Server is started and connected
		var markdownLS = new AtomicReference<LanguageServerWrapper>();
		assertTrue(DisplayHelper.waitForCondition(display, 10_000, () -> {
			markdownLS.set(LanguageServiceAccessor.getStartedWrappers(document, null, false).stream() //
					.filter(w -> "org.eclipse.wildwebdeveloper.markdown".equals(w.serverDefinition.id)) //
					.findFirst().orElse(null));
			return markdownLS.get() != null //
					&& markdownLS.get().isActive() //
					&& markdownLS.get().isConnectedTo(LSPEclipseUtils.toUri(document));
		}), "Markdown LS did not start");

		// Trigger content assist at the end of '##'
		int offset = document.get().indexOf("##") + 2;
		var cap = new LSContentAssistProcessor();

		assertTrue(DisplayHelper.waitForCondition(display, 15_000, () -> {
			ICompletionProposal[] proposals = cap.computeCompletionProposals(Utils.getViewer(editor), offset);
			if (proposals == null || proposals.length == 0)
				return false;
			boolean hasIncluded = Arrays.stream(proposals).anyMatch(p -> "#included-only".equals(p.getDisplayString()));
			boolean hasExcluded = Arrays.stream(proposals).anyMatch(p -> "#excluded-only".equals(p.getDisplayString()));
			return hasIncluded && !hasExcluded;
		}), "Workspace header completions did not respect exclude globs");
	}
}
