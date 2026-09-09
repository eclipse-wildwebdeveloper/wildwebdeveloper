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
package org.eclipse.wildwebdeveloper.markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.JobGroup;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.FullDocumentDiagnosticReport;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.UnchangedDocumentDiagnosticReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Pulls diagnostics from the Markdown language server and maps them to Eclipse problem markers.
 * Marker writes run in background workspace jobs so buffer disposal does not wait for workspace access.
 */
public final class MarkdownDiagnosticsManager {

	private static final String MARKDOWN_CONTENT_TYPE_ID = "org.eclipse.tm4e.language_pack.markdown";
	public static final String MARKDOWN_MARKER_TYPE = "org.eclipse.wildwebdeveloper.markdown.problem";

	private static final long REFRESH_DEBOUNCE_MS = 250;
	private static final long DIAGNOSTICS_TIMEOUT_SECONDS = 30;

	private static final Set<IFile> OPEN_MARKDOWN_FILES = ConcurrentHashMap.newKeySet();

	/** Tracks a buffer's refresh through marker writes and coalesces later server invalidations. */
	private record DiagnosticRefresh(ITextFileBuffer buffer, CompletableFuture<Void> completion, AtomicBoolean invalidated) {
	}

	/** De-dupes diagnostic pulls for the same file/server without suppressing a reopened buffer's first request. */
	private static final ConcurrentHashMap<String, DiagnosticRefresh> IN_FLIGHT_REFRESHES = new ConcurrentHashMap<>();

	/**
	 * Prevents diagnostic updates and marker deletion after buffer disposal
	 * from running concurrently, even when the workspace marker rule is null.
	 */
	private static final JobGroup MARKER_JOBS = new JobGroup("Wild Web Developer Markdown markers", 1, 0) {
		@Override
		protected boolean shouldCancel(final IStatus lastCompletedJobResult, final int numberOfFailedJobs,
				final int numberOfCanceledJobs) {
			// A failure for one file must not cancel pending marker work for other files.
			return false;
		}
	};

	/** Servers that requested a refresh since the last debounce run (identity-based: some LS proxies do not implement hashCode()) */
	private static final Set<LanguageServer> PENDING_REFRESH_SERVERS = Collections.newSetFromMap(new IdentityHashMap<>());

	/** Debounced, serialized refresh runner (avoids a thread pileup when multiple refreshes are requested in quick succession) */
	private static Job REFRESH_JOB;

	/** Guards access to REFRESH_JOB and the pending refresh bookkeeping below */
	private static final Object REFRESH_JOB_LOCK = new Object();

	/** Set when a refresh is requested while the job is running (rescheduled in the job-complete listener) */
	private static boolean REFRESH_RESCHEDULE_REQUESTED;

	private static boolean isMarkdownFile(final IFile file) {
		if (file == null)
			return false;
		try {
			final IContentTypeManager ctm = Platform.getContentTypeManager();
			final IContentType markdownCT = ctm.getContentType(MARKDOWN_CONTENT_TYPE_ID);
			if (markdownCT != null) {
				final IContentType fileCT = ctm.findContentTypeFor(file.getName());
				if (fileCT != null && fileCT.isKindOf(markdownCT))
					return true;
			}
		} catch (final Exception ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}

		// Fallback: cheap extension check (and keeps behavior if TM4E is absent / not initialized)
		final String ext = file.getFileExtension();
		if (ext == null)
			return false;
		return switch (ext.toLowerCase(Locale.ROOT)) {
			case "md", "markdown", "mdown" -> true;
			default -> false;
		};
	}

	private static IFile toWorkspaceFile(final IPath location) {
		if (location == null)
			return null;

		final var root = ResourcesPlugin.getWorkspace().getRoot();
		final IResource res = root.findMember(location);
		if (res instanceof final IFile file)
			return file;
		return root.getFileForLocation(location);
	}

	private static List<IFile> snapshotOpenMarkdownFiles() {
		if (OPEN_MARKDOWN_FILES.isEmpty())
			return List.of();

		final var out = new ArrayList<IFile>(OPEN_MARKDOWN_FILES.size());
		for (final IFile file : OPEN_MARKDOWN_FILES) {
			if (file != null && file.exists() && isMarkdownFile(file)) {
				out.add(file);
			}
		}
		return out;
	}

	static {
		// Remove problem markers when a Markdown text buffer is disposed (editor closed)
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new IFileBufferListener() {
			@Override
			public void bufferContentAboutToBeReplaced(final IFileBuffer buffer) {
				// no-op
			}

			@Override
			public void bufferContentReplaced(final IFileBuffer buffer) {
				// no-op
			}

			@Override
			public void bufferCreated(final IFileBuffer buffer) {
				try {
					final IPath location = buffer.getLocation();
					final IFile file = toWorkspaceFile(location);
					if (file != null && file.exists() && isMarkdownFile(file)) {
						OPEN_MARKDOWN_FILES.add(file);
					}
				} catch (final Exception ex) {
					ILog.get().warn(ex.getMessage(), ex);
				}
			}

			@Override
			public void bufferDisposed(final IFileBuffer buffer) {
				final IPath location = buffer.getLocation();
				if (location == null)
					return;

				try {
					final IFile file = toWorkspaceFile(location);
					if (file == null || !file.exists() || !isMarkdownFile(file))
						return;

					OPEN_MARKDOWN_FILES.remove(file);

					// Compare-editor input replacement can dispose buffers on the UI thread while a
					// resource notification owns the workspace. Never wait for marker writes here.
					scheduleMarkerUpdate(file, "Clear Markdown diagnostics", monitor -> {
						// A delayed close must not erase markers belonging to a reopened buffer.
						if (file.isAccessible() && getSharedBuffer(file) == null)
							clearMarkers(file);
					}).exceptionally(ex -> {
						ILog.get().warn(ex.getMessage(), ex);
						return null;
					});
				} catch (Exception ex) {
					ILog.get().warn(ex.getMessage(), ex);
				}
			}

			@Override
			public void dirtyStateChanged(final IFileBuffer buffer, final boolean isDirty) {
				// no-op
			}

			@Override
			public void stateChangeFailed(final IFileBuffer buffer) {
				// no-op
			}

			@Override
			public void stateChanging(final IFileBuffer buffer) {
				// no-op
			}

			@Override
			public void stateValidationChanged(final IFileBuffer buffer, final boolean isStateValidated) {
				// no-op
			}

			@Override
			public void underlyingFileDeleted(final IFileBuffer buffer) {
				// no-op
			}

			@Override
			public void underlyingFileMoved(final IFileBuffer buffer, IPath path) {
				try {
					final IFile newFile = toWorkspaceFile(buffer.getLocation());
					final IFile otherFile = toWorkspaceFile(path);
					if (newFile != null)
						OPEN_MARKDOWN_FILES.remove(newFile);
					if (otherFile != null)
						OPEN_MARKDOWN_FILES.remove(otherFile);
					if (newFile != null && newFile.exists() && isMarkdownFile(newFile))
						OPEN_MARKDOWN_FILES.add(newFile);
				} catch (final Exception ex) {
					ILog.get().warn(ex.getMessage(), ex);
				}
			}

		});
	}

	private static String markerKey(final String message, final int severity, final int charStart, final int charEnd) {
		return message + '|' + severity + '|' + charStart + ':' + charEnd;
	}

	private static String markerKey(final IMarker marker) throws CoreException {
		final var message = String.valueOf(marker.getAttribute(IMarker.MESSAGE));
		final int severity = marker.getAttribute(IMarker.SEVERITY, -1);
		final int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		final int charEnd = marker.getAttribute(IMarker.CHAR_END, -1);
		return markerKey(message, severity, charStart, charEnd);
	}

	private static void applyMarkers(final IFile file, final List<Diagnostic> diagnostics) {
		try {
			final var markdownMarkers = new HashMap<String, IMarker>();
			for (final IMarker m : file.findMarkers(MARKDOWN_MARKER_TYPE, true, IResource.DEPTH_ZERO)) {
				markdownMarkers.putIfAbsent(markerKey(m), m);
			}

			for (final Diagnostic d : diagnostics) {
				final String msg = getMessageString(d);
				final int severity = toIMarkerSeverity(d.getSeverity());
				final int line = d.getRange() != null ? d.getRange().getStart().getLine() + 1 : 1;
				int charStart = -1, charEnd = -1;
				if (d.getRange() != null) {
					try {
						final int[] offsets = toOffsets(file, d.getRange());
						charStart = offsets[0];
						charEnd = offsets[1];
					} catch (final Exception ignore) {
						ILog.get().warn(ignore.getMessage(), ignore);
					}
				}

				IMarker target = markdownMarkers.remove(markerKey(msg, severity, charStart, charEnd));
				if (target == null) {
					target = file.createMarker(MARKDOWN_MARKER_TYPE);
					target.setAttribute(IMarker.MESSAGE, msg);
					target.setAttribute(IMarker.SEVERITY, severity);
					target.setAttribute(IMarker.LINE_NUMBER, line);
					if (charStart >= 0 && charEnd >= 0) {
						target.setAttribute(IMarker.CHAR_START, charStart);
						target.setAttribute(IMarker.CHAR_END, charEnd);
					} else {
						target.setAttribute(IMarker.CHAR_START, -1);
						target.setAttribute(IMarker.CHAR_END, -1);
					}
				}
			}

			// Delete any of our markers that were not matched this round
			for (final IMarker m : markdownMarkers.values()) {
				try {
					m.delete();
				} catch (Exception ignore) {
					ILog.get().warn(ignore.getMessage(), ignore);
				}
			}
		} catch (final Exception ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}
	}

	private static String getMessageString(final Diagnostic d) {
		Either<String, MarkupContent> message = d.getMessage();
		if (message.isLeft()) {
			return message.getLeft();
		} else {
			return message.getRight().getValue();
		}
	}

	private static void clearMarkers(final IFile file) throws CoreException {
		file.deleteMarkers(MARKDOWN_MARKER_TYPE, true, IResource.DEPTH_ZERO);
	}

	private static List<Diagnostic> extractDiagnostics(final RelatedFullDocumentDiagnosticReport full) {
		if (full == null)
			return List.of();

		final var out = new ArrayList<Diagnostic>();
		if (full.getItems() != null)
			out.addAll(full.getItems());
		if (full.getRelatedDocuments() != null) {
			for (final Either<FullDocumentDiagnosticReport, UnchangedDocumentDiagnosticReport> rel : full.getRelatedDocuments().values()) {
				if (rel != null && rel.isLeft()) {
					final FullDocumentDiagnosticReport rfull = rel.getLeft();
					if (rfull.getItems() != null) {
						out.addAll(rfull.getItems());
					}
				}
				// if rel.isRight() -> unchanged for that related doc: ignore
			}
		}
		return out;
	}

	private static CompletableFuture<Void> scheduleMarkerUpdate(final IFile file, final String name,
			final ICoreRunnable update) {
		final var completion = new CompletableFuture<Void>();
		final var job = new WorkspaceJob(name) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				update.run(monitor);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(final Object family) {
				return family == MarkdownDiagnosticsManager.class;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().markerRule(file));
		job.setJobGroup(MARKER_JOBS);
		job.setSystem(true);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				// Finish the request after the workspace job, including cancellation or
				// failure.
				if (event.getResult().isOK())
					completion.complete(null);
				else
					completion.completeExceptionally(new CoreException(event.getResult()));
			}
		});
		job.schedule();
		return completion;
	}

	private static ITextFileBuffer getSharedBuffer(final IFile file) {
		return FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
	}

	private static CompletableFuture<Void> handleDiagnosticReport(final IFile file, final ITextFileBuffer requestBuffer,
			final DocumentDiagnosticReport report) {
		// An unchanged report retains the current markers.
		if (report == null || !report.isLeft())
			return CompletableFuture.completedFuture(null);

		return scheduleMarkerUpdate(file, "Update Markdown diagnostics", monitor -> {
			// Check at execution time: a queued response must not outlive its buffer
			// session.
			// Null still permits explicit pulls for unopened files without creating an
			// editor buffer.
			if (file.isAccessible() && getSharedBuffer(file) == requestBuffer)
				applyMarkers(file, extractDiagnostics(report.getLeft()));
		});
	}

	private static void scheduleRefreshAllOpenMarkdownFiles(final LanguageServer languageServer) {
		if (languageServer == null)
			return;

		synchronized (REFRESH_JOB_LOCK) {
			PENDING_REFRESH_SERVERS.add(languageServer);
			if (REFRESH_JOB == null) {
				REFRESH_JOB = new Job("Wild Web Developer Markdown diagnostics refresh") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						final List<LanguageServer> languageServersToRefresh;
						synchronized (REFRESH_JOB_LOCK) {
							if (PENDING_REFRESH_SERVERS.isEmpty())
								return Status.OK_STATUS;
							languageServersToRefresh = new ArrayList<>(PENDING_REFRESH_SERVERS);
							PENDING_REFRESH_SERVERS.clear();
						}

						if (monitor.isCanceled())
							return Status.OK_STATUS;

						final List<IFile> openFiles = snapshotOpenMarkdownFiles();
						if (openFiles.isEmpty())
							return Status.OK_STATUS;

						for (final LanguageServer ls : languageServersToRefresh) {
							if (monitor.isCanceled())
								break;
							for (final IFile file : openFiles) {
								if (monitor.isCanceled())
									break;
								final var buffer = getSharedBuffer(file);
								// A file closed since the snapshot is not an explicit unopened-file pull.
								if (buffer != null) {
									refreshFile(file, ls, buffer, true);
								}
							}
						}
						return Status.OK_STATUS;
					}
				};
				REFRESH_JOB.setSystem(true);
				REFRESH_JOB.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(final IJobChangeEvent event) {
						synchronized (REFRESH_JOB_LOCK) {
							if (!REFRESH_RESCHEDULE_REQUESTED && PENDING_REFRESH_SERVERS.isEmpty())
								return;
							REFRESH_RESCHEDULE_REQUESTED = false;

							if (REFRESH_JOB == null || REFRESH_JOB.getState() != Job.NONE) {
								REFRESH_RESCHEDULE_REQUESTED = true;
								return;
							}

							try {
								REFRESH_JOB.schedule(REFRESH_DEBOUNCE_MS);
							} catch (final IllegalStateException ex) {
								// Job got scheduled concurrently, try again when it completes.
								REFRESH_RESCHEDULE_REQUESTED = true;
							}
						}
					}
				});
			}

			// Debounce: keep only the latest refresh request.
			//
			// Avoid (re-)scheduling while RUNNING; schedule() would throw
			// IllegalStateException.
			// Instead, mark pending and let the JobChangeListener reschedule once it
			// completes.
			if (REFRESH_JOB.getState() == Job.RUNNING) {
				REFRESH_RESCHEDULE_REQUESTED = true;
				return;
			}

			try {
				REFRESH_JOB.cancel();
				REFRESH_JOB.schedule(REFRESH_DEBOUNCE_MS);
			} catch (final IllegalStateException ex) {
				REFRESH_RESCHEDULE_REQUESTED = true;
			}
		}
	}

	public static void refreshAllOpenMarkdownFiles(final LanguageServer languageServer) {
		scheduleRefreshAllOpenMarkdownFiles(languageServer);
	}

	public static void refreshFile(final IFile file) {
		try {
			if (file == null || !file.exists())
				return;

			// Keep the original session even if server discovery completes after close/reopen.
			final var buffer = getSharedBuffer(file);

			LanguageServers.forProject(file.getProject())
					.withPreferredServer(
							LanguageServersRegistry.getInstance().getDefinition(MarkdownLanguageServer.MARKDOWN_LANGUAGE_SERVER_ID))
					.excludeInactive()
					.collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
					.thenAccept(lss -> lss.forEach(ls -> refreshFile(file, ls, buffer, false)));
		} catch (final Exception ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}
	}

	private static CompletableFuture<Void> refreshFile(final IFile file, final LanguageServer languageServer,
			final ITextFileBuffer requestBuffer, final boolean serverInvalidation) {
		if (file == null || !file.exists() || languageServer == null)
			return CompletableFuture.completedFuture(null);
		if (getSharedBuffer(file) != requestBuffer)
			return CompletableFuture.completedFuture(null);

		// Include language server identity so de-duping does not hide refreshes across different server instances.
		final String key = file.getFullPath().toString() + "@" + System.identityHashCode(languageServer);
		final var refresh = IN_FLIGHT_REFRESHES.compute(key, (k, existing) -> {
			// Reopening must bypass an old session's request even while that request is pending.
			if (existing != null && existing.buffer() == requestBuffer && !existing.completion().isDone()) {
				// A server invalidation needs fresh diagnostics after the current request. Parser calls can
				// be caused by that request itself, so retrying those would create a diagnostic loop.
				if (serverInvalidation)
					existing.invalidated().set(true);
				return existing;
			}

			final String uri = toLspFileUri(file);
			final var params = new DocumentDiagnosticParams();
			params.setTextDocument(new TextDocumentIdentifier(uri));

			final CompletableFuture<Void> started = languageServer.getTextDocumentService().diagnostic(params)
					.orTimeout(DIAGNOSTICS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
					// Keep de-duplication active while marker work is queued, not just during the LS call.
					.thenCompose(report -> handleDiagnosticReport(file, requestBuffer, report)).exceptionally(ex -> {
						ILog.get().warn(ex.getMessage(), ex);
						return null;
					});

			return new DiagnosticRefresh(requestBuffer, started, new AtomicBoolean());
		});
		// Register outside compute: an already-completed response must not recursively update the map.
		// Conditional removal protects newer sessions and lets only one caller start the coalesced follow-up.
		return refresh.completion().whenComplete((v, ex) -> {
			if (IN_FLIGHT_REFRESHES.remove(key, refresh) && refresh.invalidated().get()) {
				// Reuse the captured buffer so the entry check discards invalidations after close/reopen.
				refreshFile(file, languageServer, requestBuffer, false);
			}
		}).exceptionally(ex -> {
			// Starting the follow-up can throw before a response future is returned.
			ILog.get().warn(ex.getMessage(), ex);
			return null;
		});
	}

	private static int toIMarkerSeverity(final DiagnosticSeverity sev) {
		if (sev == null)
			return IMarker.SEVERITY_INFO;
		return switch (sev) {
			case Error -> IMarker.SEVERITY_ERROR;
			case Warning -> IMarker.SEVERITY_WARNING;
			case Information, Hint -> IMarker.SEVERITY_INFO;
		};
	}

	private static String toLspFileUri(final IFile file) {
		String s = file.getLocationURI().toString();
		// Normalize Windows drive URIs to file:///C:/...
		if (s.startsWith("file:/") && !s.startsWith("file:///") && s.length() >= 8 && Character.isLetter(s.charAt(6))
				&& s.charAt(7) == ':') {
			return "file:///" + s.substring("file:/".length());
		}
		return s;
	}

	private static int[] toOffsets(final IFile file, final Range range) throws CoreException, BadLocationException {
		// Use the live document so offsets include unsaved editor changes, without taking ownership.
		final var sharedBuffer = getSharedBuffer(file);
		if (sharedBuffer != null)
			return toOffsets(sharedBuffer.getDocument(), range);

		// Temporary reads must not fire shared-buffer lifecycle events and schedule cleanup of
		// the markers being applied. A private manager still preserves file encoding handling.
		final var mgr = FileBuffers.createTextFileBufferManager();
		final var path = file.getFullPath();
		mgr.connect(path, LocationKind.IFILE, null);
		try {
			final var buf = mgr.getTextFileBuffer(path, LocationKind.IFILE);
			return toOffsets(buf != null ? buf.getDocument() : null, range);
		} finally {
			mgr.disconnect(path, LocationKind.IFILE, null);
		}
	}

	private static int[] toOffsets(final IDocument doc, final Range range) throws BadLocationException {
		if (doc == null)
			return new int[] { 0, 0 };
		final int startLine = Math.max(0, range.getStart().getLine());
		final int startCol = Math.max(0, range.getStart().getCharacter());
		final int endLine = Math.max(0, range.getEnd().getLine());
		final int endCol = Math.max(0, range.getEnd().getCharacter());
		int start = Math.min(doc.getLength(), doc.getLineOffset(startLine) + startCol);
		int end = Math.min(doc.getLength(), doc.getLineOffset(endLine) + endCol);
		if (end < start)
			end = start;
		return new int[] { start, end };
	}

	private MarkdownDiagnosticsManager() {
	}
}
