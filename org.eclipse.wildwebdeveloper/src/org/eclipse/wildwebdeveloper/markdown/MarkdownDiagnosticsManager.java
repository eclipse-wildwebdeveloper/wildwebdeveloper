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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

	public static final String MARKDOWN_MARKER_TYPE = "org.eclipse.wildwebdeveloper.markdown.problem";

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
				// no-op
			}

			@Override
			public void bufferDisposed(final IFileBuffer buffer) {
				final IPath location = buffer.getLocation();
				if (location == null)
					return;

				/*
				 * remove all problem markers on editor close
				 */
				try {
					final var root = ResourcesPlugin.getWorkspace().getRoot();
					final var res = root.findMember(location);
					if (res instanceof final IFile file && file.exists()) {
						final String name = file.getName().toLowerCase();
						if (name.endsWith(".md") || name.endsWith(".markdown") || name.endsWith(".mdown")) {
							clearMarkers(file);
						}
					}
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
				// no-op
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

	private static List<Diagnostic> extractDiagnostics(final DocumentDiagnosticReport report) {
		if (report == null)
			return List.of();
		if (report.isLeft()) {
			final RelatedFullDocumentDiagnosticReport full = report.getLeft();
			final var out = new ArrayList<Diagnostic>();
			if (full.getItems() != null)
				out.addAll(full.getItems());
			if (full.getRelatedDocuments() != null) {
				for (final Either<FullDocumentDiagnosticReport, UnchangedDocumentDiagnosticReport> rel : full.getRelatedDocuments()
						.values()) {
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
		} else if (report.isRight()) {
			// Unchanged for the main document: do not touch markers
		}
		return List.of();
	}

	public static void refreshAllOpenMarkdownFiles(final LanguageServer languageServer) {
		// Collect .md-like files from open workspace editors would be ideal; for simplicity, scan workspace for *.md, *.markdown, *.mdown
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(res -> {
				if (res.getType() == IResource.FILE) {
					final String name = res.getName().toLowerCase();
					if (name.endsWith(".md") || name.endsWith(".markdown") || name.endsWith(".mdown")) {
						refreshFile((IFile) res, languageServer);
					}
					return false;
				}
				return true;
			});
		} catch (final Exception ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}
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
		try {
			if (file == null || !file.exists())
				return;

			final String uri = toLspFileUri(file);
			final var params = new DocumentDiagnosticParams();
			params.setTextDocument(new TextDocumentIdentifier(uri));
			final DocumentDiagnosticReport report = languageServer.getTextDocumentService().diagnostic(params).get();
			applyMarkers(file, extractDiagnostics(report));
		} catch (final Exception ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}
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
