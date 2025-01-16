/*******************************************************************************
 * Copyright (c) 2022, 2023 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 */
package org.eclipse.wildwebdeveloper.xml.internal.autoclose;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceClientConstants.XML_PREFERENCES_COMPLETION_AUTO_CLOSE_TAGS;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.XMLLanguageServerAPI;

/**
 * {@link IReconciler} implementation used to support auto close tags , features
 * provides by the LemMinx XML language server with the custom 'xml/closeTag'
 * LSP request.
 *
 */
public class XMLAutoCloseTagReconciler implements IReconciler {

	private IDocument document;

	private ITextViewer viewer;

	private Listener listener;

	private static int getReplaceLength(Range range, IDocument document) throws BadLocationException {
		if (range == null) {
			return 0;
		}
		Position start = range.getStart();
		Position end = range.getEnd();
		if (start.getLine() == end.getLine()) {
			return end.getCharacter() - start.getCharacter();
		}
		int startOffset = LSPEclipseUtils.toOffset(start, document);
		int endOffset = LSPEclipseUtils.toOffset(end, document);
		return endOffset - startOffset;
	}

	/**
	 * Internal document listener and text input listener.
	 */
	class Listener implements IDocumentListener, ITextInputListener {

		@Override
		public void documentAboutToBeChanged(DocumentEvent e) {
		}

		@Override
		public void documentChanged(DocumentEvent e) {
			autoInsert(e);
		}

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput == document) {
				if (document != null) {
					document.removeDocumentListener(this);
				}
				document = null;
			}
		}

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			document = newInput;
			if (document == null) {
				return;
			}
			document.addDocumentListener(this);
		}

		private void autoInsert(DocumentEvent event) {
			if (!isEnabled()) {
				return;
			}
			if (event == null || viewer == null) {
				return;
			}
			IDocument document = event.getDocument();
			if (document == null || event == null || event.getLength() != 0 || event.getText().length() != 1) {
				return;
			}

			int offset = event.getOffset() + 1;
			char c = event.getText().charAt(0);
			if (c != '>' && c != '/') {
				return;
			}
			URI uri = LSPEclipseUtils.toUri(document);
			if (uri == null) {
				return;
			}

			TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri.toString());
			LanguageServers.forDocument(document).collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
					.thenAccept(lss -> lss.stream().filter(XMLLanguageServerAPI.class::isInstance)
							.map(XMLLanguageServerAPI.class::cast).findAny().ifPresent(info -> {
								// The document is bound with XML language server, consumes the xml/closeTag
								final Display display = viewer.getTextWidget().getDisplay();
								CompletableFuture.supplyAsync(() -> {
									try {
										TextDocumentPositionParams params = LSPEclipseUtils
												.toTextDocumentPosistionParams(offset, document);
										// consumes xml/closeTag from XML language server
										info.closeTag(params).thenAccept(r -> {
											if (r != null) {
												display.asyncExec(() -> {
													try {
														// we receive a text like
														// $0</foo>
														// $0 should be used for set the cursor.
														String text = r.snippet.replace("$0", "");
														int replaceLength = getReplaceLength(r.range, document);
														document.replace(offset, replaceLength, text);
													} catch (BadLocationException e) {
														// Do nothing
													}
												});

											}
										});
									} catch (BadLocationException e) {
										// Do nothing
									}
									return null;
								});
							}));
		}

		private boolean isEnabled() {
			return Activator.getDefault().getPreferenceStore().getBoolean(XML_PREFERENCES_COMPLETION_AUTO_CLOSE_TAGS);
		}

	}

	@Override
	public void install(ITextViewer viewer) {
		this.viewer = viewer;
		listener = new Listener();
		viewer.addTextInputListener(listener);
	}

	@Override
	public void uninstall() {
		if (listener != null) {
			viewer.removeTextInputListener(listener);
			if (document != null) {
				document.removeDocumentListener(listener);
			}
			listener = null;
		}
		this.viewer = null;
	}

	@Override
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		return null;
	}

}
