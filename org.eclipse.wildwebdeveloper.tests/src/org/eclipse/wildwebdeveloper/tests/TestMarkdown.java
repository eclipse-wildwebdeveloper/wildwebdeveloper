/*******************************************************************************
 * Copyright (c) 2025, 2026 Vegard IT GmbH and others.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
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

/**
 * Verifies Markdown language features and the lifecycle of their workspace problem markers.
 */
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

	private IFile createDiagnosticFile() throws Exception {
		// Register the buffer listener before opening a buffer, without starting a real language server.
		Class.forName(MarkdownDiagnosticsManager.class.getName());
		final var project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getName() + ".lifecycle." + System.nanoTime());
		project.create(null);
		project.open(null);
		final var file = project.getFile("doc.md");
		file.create("# Title\nBody\n".getBytes(StandardCharsets.UTF_8), true, false, null);
		return file;
	}

	private static CompletableFuture<?> requestDiagnostics(final IFile file, final DiagnosticSpy spy) throws Exception {
		// Inject a controllable server at the existing request boundary; no production test API is needed.
		final var method = MarkdownDiagnosticsManager.class.getDeclaredMethod("refreshFile", IFile.class,
				LanguageServer.class, ITextFileBuffer.class, boolean.class);
		method.setAccessible(true);
		return (CompletableFuture<?>) method.invoke(null, file, spy.server(),
				FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE), false);
	}

	private static DocumentDiagnosticReport diagnosticReport(final String... messages) {
		final var diagnostics = new ArrayList<Diagnostic>();
		for (final String message : messages) {
			final var diagnostic = new Diagnostic();
			diagnostic.setMessage(message);
			diagnostic.setSeverity(DiagnosticSeverity.Warning);
			diagnostic.setRange(new Range(new Position(1, 0), new Position(1, 4)));
			diagnostics.add(diagnostic);
		}
		final var report = new RelatedFullDocumentDiagnosticReport();
		report.setItems(diagnostics);
		return new DocumentDiagnosticReport(report);
	}

	private static void awaitDiagnosticMarkers() {
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5_000,
				() -> Job.getJobManager().find(MarkdownDiagnosticsManager.class).length == 0),
				"Markdown marker jobs did not finish");
	}

	private static void awaitDiagnosticRefresh(final CompletableFuture<?> refresh) {
		// Jobs leave the manager before their done listeners complete the refresh future.
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5_000, refresh::isDone),
				"Markdown diagnostic refresh did not finish");
		refresh.join();
	}

	private static List<String> diagnosticMessages(final IFile file) throws CoreException {
		final var messages = new ArrayList<String>();
		for (final var marker : file.findMarkers(MARKDOWN_MARKER_TYPE, true, IResource.DEPTH_ZERO)) {
			messages.add(marker.getAttribute(IMarker.MESSAGE, ""));
		}
		Collections.sort(messages);
		return messages;
	}

	@Test
	void disposingMarkdownBufferDoesNotWaitForWorkspaceNotification() throws Exception {
		final var file = createDiagnosticFile();
		final var workspace = ResourcesPlugin.getWorkspace();
		final var manager = FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		// Two viewers may share the file; only the last disconnect disposes its buffer.
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		final var markdownMarker = file.createMarker(MARKDOWN_MARKER_TYPE);
		final var otherMarker = file.createMarker(IMarker.PROBLEM);
		final var notificationEntered = new CountDownLatch(1);
		final var releaseNotification = new CountDownLatch(1);
		final var disposalReturned = new CountDownLatch(1);
		final IResourceChangeListener listener = event -> {
			if (event.getDelta() == null || event.getDelta().findMember(file.getFullPath()) == null)
				return;
			notificationEntered.countDown();
			try {
				releaseNotification.await(10, TimeUnit.SECONDS);
			} catch (final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		};
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		final var notificationJob = Job.create("Hold a resource change notification", monitor -> {
			otherMarker.setAttribute(IMarker.MESSAGE, "Trigger POST_CHANGE");
		});
		try {
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			assertTrue(markdownMarker.exists(), "A remaining buffer consumer must retain Markdown markers");
			notificationJob.schedule();
			assertTrue(notificationEntered.await(5, TimeUnit.SECONDS), "Resource notification did not start");
			// Release the workspace independently even when the old implementation blocks the UI thread.
			// The assertion checks ordering, rather than relying on how long disconnect normally takes.
			final var returnedBeforeRelease = CompletableFuture.supplyAsync(() -> {
				try {
					return disposalReturned.await(5, TimeUnit.SECONDS);
				} catch (final InterruptedException ex) {
					Thread.currentThread().interrupt();
					return false;
				} finally {
					releaseNotification.countDown();
				}
			});
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			disposalReturned.countDown();
			assertTrue(returnedBeforeRelease.get(10, TimeUnit.SECONDS),
					"Buffer disposal waited for workspace access on the UI thread");
			awaitDiagnosticMarkers();
			assertTrue(!markdownMarker.exists(), "Closing the buffer must eventually remove Markdown markers");
			assertTrue(otherMarker.exists(), "Cleanup must preserve other marker types");
		} finally {
			releaseNotification.countDown();
			workspace.removeResourceChangeListener(listener);
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5_000,
					() -> notificationJob.getState() == Job.NONE), "Resource notification did not finish");
		}
	}

	@Test
	void reopeningMarkdownBufferPreservesMarkersFromItsNewSession() throws Exception {
		final var file = createDiagnosticFile();
		final var manager = FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		final var jobs = Job.getJobManager();
		try {
			// Queue close cleanup but reopen before it can run, as compare-editor input replacement can do.
			jobs.suspend();
			try {
				manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
				manager.connect(file.getFullPath(), LocationKind.IFILE, null);
				file.createMarker(MARKDOWN_MARKER_TYPE).setAttribute(IMarker.MESSAGE, "fresh");
			} finally {
				jobs.resume();
			}
			awaitDiagnosticMarkers();
			assertEquals(List.of("fresh"), diagnosticMessages(file));
		} finally {
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			awaitDiagnosticMarkers();
		}
	}

	@Test
	void lateMarkdownDiagnosticsDoNotRestoreMarkersAfterClose() throws Exception {
		final var file = createDiagnosticFile();
		final var manager = FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		final var spy = newDiagnosticSpy();
		try {
			requestDiagnostics(file, spy);
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			awaitDiagnosticMarkers();
			spy.lastFuture().get().complete(diagnosticReport("stale"));
			awaitDiagnosticMarkers();
			assertEquals(List.of(), diagnosticMessages(file));
		} finally {
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
		}
	}

	@Test
	void reopeningMarkdownBufferStartsFreshDiagnosticsWhileOldRequestIsPending() throws Exception {
		final var file = createDiagnosticFile();
		final var manager = FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		final var spy = newDiagnosticSpy();
		CompletableFuture<DocumentDiagnosticReport> oldRequest = null;
		try {
			requestDiagnostics(file, spy);
			oldRequest = spy.lastFuture().get();
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			manager.connect(file.getFullPath(), LocationKind.IFILE, null);
			requestDiagnostics(file, spy);
			assertEquals(2, spy.calls().get(), "An old session must not suppress a new session's diagnostic request");
			spy.lastFuture().get().complete(diagnosticReport("fresh"));
			awaitDiagnosticMarkers();
			oldRequest.complete(diagnosticReport("stale"));
			awaitDiagnosticMarkers();
			assertEquals(List.of("fresh"), diagnosticMessages(file));
		} finally {
			if (oldRequest != null)
				oldRequest.complete(null);
			if (spy.lastFuture().get() != null)
				spy.lastFuture().get().complete(null);
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			awaitDiagnosticMarkers();
		}
	}

	@Test
	void markdownDiagnosticsForClosedFilesKeepAllMarkers() throws Exception {
		final var file = createDiagnosticFile();
		final var spy = newDiagnosticSpy();
		requestDiagnostics(file, spy);
		// Each range used to open and dispose a shared buffer, deleting markers from earlier ranges.
		spy.lastFuture().get().complete(diagnosticReport("first", "second"));
		awaitDiagnosticMarkers();
		assertEquals(List.of("first", "second"), diagnosticMessages(file));
		assertEquals(null, FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE));
	}

	@Test
	void markdownRefreshStaysInFlightUntilMarkersAreApplied() throws Exception {
		final var file = createDiagnosticFile();
		final var spy = newDiagnosticSpy();
		final var jobs = Job.getJobManager();
		final CompletableFuture<?> refresh;
		// A completed LS response must still suppress duplicate pulls while its marker write is queued.
		jobs.suspend();
		try {
			refresh = requestDiagnostics(file, spy);
			spy.lastFuture().get().complete(diagnosticReport("queued"));
			requestDiagnostics(file, spy);
			assertEquals(1, spy.calls().get());
		} finally {
			jobs.resume();
		}
		awaitDiagnosticRefresh(refresh);
		assertEquals(List.of("queued"), diagnosticMessages(file));
		requestDiagnostics(file, spy);
		assertEquals(2, spy.calls().get(), "A completed marker write must allow the next refresh");
		spy.lastFuture().get().complete(null);
	}

	@Test
	void serverRefreshDuringQueuedMarkdownMarkersRunsAgain() throws Exception {
		final var file = createDiagnosticFile();
		final var manager = FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		final var spy = newDiagnosticSpy();
		final var jobs = Job.getJobManager();
		Job markerJob = null;
		try {
			jobs.suspend();
			try {
				requestDiagnostics(file, spy);
				spy.lastFuture().get().complete(diagnosticReport("stale"));
				final var markerJobs = jobs.find(MarkdownDiagnosticsManager.class);
				assertEquals(1, markerJobs.length);
				markerJob = markerJobs[0];
				// Hold only marker application so the server's debounced refresh can still run.
				assertTrue(markerJob.sleep(), "Marker application must remain queued");
			} finally {
				jobs.resume();
			}
			for (int refresh = 0; refresh < 3; refresh++) {
				MarkdownDiagnosticsManager.refreshAllOpenMarkdownFiles(spy.server());
				final var field = MarkdownDiagnosticsManager.class.getDeclaredField("REFRESH_JOB");
				field.setAccessible(true);
				// Await consumption of each invalidation, not merely expiration of its debounce delay.
				((Job) field.get(null)).join();
			}
			assertEquals(1, spy.calls().get(), "Server invalidations must not overlap the active refresh");
			final var firstResponse = spy.lastFuture().get();
			markerJob.wakeUp();
			assertTrue(waitUpTo(5_000, () -> spy.lastFuture().get() != firstResponse),
					"A server invalidation received during marker application must trigger a follow-up pull");
			assertEquals(2, spy.calls().get(), "Pending server invalidations must coalesce into one follow-up pull");
			// Observe the active follow-up through the same boundary used by opportunistic parser pulls.
			// This also ensures its response handler is attached before the test completes that response.
			final var followUp = requestDiagnostics(file, spy);
			spy.lastFuture().get().complete(diagnosticReport("fresh"));
			awaitDiagnosticRefresh(followUp);
			assertEquals(2, spy.calls().get(), "An opportunistic pull must not request another follow-up");
			assertEquals(List.of("fresh"), diagnosticMessages(file));
		} finally {
			if (markerJob != null)
				markerJob.wakeUp();
			if (spy.lastFuture().get() != null)
				spy.lastFuture().get().complete(null);
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			awaitDiagnosticMarkers();
		}
	}

	@Test
	void markdownDiagnosticOffsetsUseUnsavedEditorContent() throws Exception {
		final var file = createDiagnosticFile();
		final var manager = FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		try {
			final var document = manager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE).getDocument();
			document.set("# A longer unsaved title\nBody\n");
			final var spy = newDiagnosticSpy();
			requestDiagnostics(file, spy);
			spy.lastFuture().get().complete(diagnosticReport("unsaved"));
			awaitDiagnosticMarkers();
			final var markers = file.findMarkers(MARKDOWN_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			assertEquals(1, markers.length);
			assertEquals(document.getLineOffset(1), markers[0].getAttribute(IMarker.CHAR_START, -1));
		} finally {
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			awaitDiagnosticMarkers();
		}
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
	@SuppressWarnings("restriction")
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
	@SuppressWarnings("restriction")
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
