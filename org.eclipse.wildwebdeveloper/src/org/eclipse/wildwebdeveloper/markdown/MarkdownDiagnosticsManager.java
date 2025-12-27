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

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.FullDocumentDiagnosticReport;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.UnchangedDocumentDiagnosticReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Pulls diagnostics from the Markdown language server and maps them to Eclipse problem markers.
 */
public final class MarkdownDiagnosticsManager {

	private static final String MARKDOWN_CONTENT_TYPE_ID = "org.eclipse.tm4e.language_pack.markdown";
	public static final String MARKDOWN_MARKER_TYPE = "org.eclipse.wildwebdeveloper.markdown.problem";

	private static final long REFRESH_DEBOUNCE_MS = 250;
	private static final long DIAGNOSTICS_TIMEOUT_SECONDS = 30;

	private static final Set<IFile> OPEN_MARKDOWN_FILES = ConcurrentHashMap.newKeySet();

	/** De-dupes diagnostic pulls so repeated refresh requests do not start overlapping diagnostics for the same file/server */
	private static final ConcurrentHashMap<String, CompletableFuture<Void>> IN_FLIGHT_REFRESHES = new ConcurrentHashMap<>();

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

					/*
					 * remove all problem markers on editor close
					 */
					clearMarkers(file);
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

	private static synchronized void applyMarkers(final IFile file, final List<Diagnostic> diagnostics) {
		try {
			final var markdownMarkers = new HashMap<String, IMarker>();
			for (final IMarker m : file.findMarkers(MARKDOWN_MARKER_TYPE, true, IResource.DEPTH_ZERO)) {
				markdownMarkers.putIfAbsent(markerKey(m), m);
			}

			for (final Diagnostic d : diagnostics) {
				final String msg = d.getMessage();
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

	private static void handleDiagnosticReport(final IFile file, final DocumentDiagnosticReport report) {
		if (file == null || !file.exists() || report == null)
			return;

		if (report.isRight()) {
			// Unchanged for the main document: do not touch markers
			return;
		}

		if (report.isLeft()) {
			applyMarkers(file, extractDiagnostics(report.getLeft()));
		}
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
								refreshFile(file, ls);
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
			// Avoid (re-)scheduling while RUNNING; schedule() would throw IllegalStateException.
			// Instead, mark pending and let the JobChangeListener reschedule once it completes.
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

			LanguageServers.forProject(file.getProject())
					.withPreferredServer(
							LanguageServersRegistry.getInstance().getDefinition(MarkdownLanguageServer.MARKDOWN_LANGUAGE_SERVER_ID))
					.excludeInactive()
					.collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
					.thenAccept(lss -> lss.forEach(ls -> refreshFile(file, ls)));
		} catch (final Exception ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}
	}

	private static void refreshFile(final IFile file, final LanguageServer languageServer) {
		if (file == null || !file.exists() || languageServer == null)
			return;

		// Include language server identity so de-duping does not hide refreshes across different server instances.
		final String key = file.getFullPath().toString() + "@" + System.identityHashCode(languageServer);
		IN_FLIGHT_REFRESHES.compute(key, (k, existing) -> {
			if (existing != null && !existing.isDone())
				return existing;

			final String uri = toLspFileUri(file);
			final var params = new DocumentDiagnosticParams();
			params.setTextDocument(new TextDocumentIdentifier(uri));

			final CompletableFuture<Void> started = languageServer.getTextDocumentService()
					.diagnostic(params)
					.orTimeout(DIAGNOSTICS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
					.thenAccept(report -> handleDiagnosticReport(file, report))
					.exceptionally(ex -> {
						ILog.get().warn(ex.getMessage(), ex);
						return null;
					});

			started.whenComplete((v, ex) -> IN_FLIGHT_REFRESHES.remove(k, started));
			return started;
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
		// Connect ensures a document is available even if no editor is open
		final var mgr = FileBuffers.getTextFileBufferManager();
		final var path = file.getFullPath();
		mgr.connect(path, LocationKind.IFILE, null);
		try {
			final var buf = mgr.getTextFileBuffer(path, LocationKind.IFILE);
			final IDocument doc = buf != null ? buf.getDocument() : null;
			if (doc == null) {
				return new int[] { 0, 0 };
			}
			final int startLine = Math.max(0, range.getStart().getLine());
			final int startCol = Math.max(0, range.getStart().getCharacter());
			final int endLine = Math.max(0, range.getEnd().getLine());
			final int endCol = Math.max(0, range.getEnd().getCharacter());
			int start = Math.min(doc.getLength(), doc.getLineOffset(startLine) + startCol);
			int end = Math.min(doc.getLength(), doc.getLineOffset(endLine) + endCol);
			if (end < start)
				end = start;
			return new int[] { start, end };
		} finally {
			mgr.disconnect(path, LocationKind.IFILE, null);
		}
	}

	private MarkdownDiagnosticsManager() {
	}
}
