/*******************************************************************************
 * Copyright (c) 2022, 2024 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.html.autoinsert;

import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CLOSING_TAGS;
import static org.eclipse.wildwebdeveloper.html.ui.preferences.HTMLPreferenceClientConstants.HTML_PREFERENCES_AUTO_CREATE_QUOTES;

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
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.html.HTMLLanguageServerAPI;
import org.eclipse.wildwebdeveloper.html.autoinsert.AutoInsertParams.AutoInsertKind;

/**
 * {@link IReconciler} implementation used to support auto close tags / auto
 * insert quote, features provides by the vscode HTML language server with the
 * custom 'html/autoInsert' LSP request.
 *
 */
public class HTMLAutoInsertReconciler implements IReconciler {

	private IDocument document;

	private ITextViewer viewer;

	private Listener listener;

	private void autoInsert(DocumentEvent event) {
		boolean autoClosingTag = isAutoClosingTagEnabled();
		boolean autoCreateQuotes = isAutoCreateQuotesEnabled();
		if (!autoClosingTag && !autoCreateQuotes) {
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
		if (c != '>' && c != '/' && c != '=') {
			return;
		}
		URI uri = LSPEclipseUtils.toUri(document);
		if (uri == null) {
			return;
		}
		AutoInsertKind autoInsertKind = c == '=' ? AutoInsertKind.autoQuote : AutoInsertKind.autoClose;
		switch (autoInsertKind) {
		case autoClose:
			if (!autoClosingTag) {
				return;
			}
			break;
		case autoQuote:
			if (!autoCreateQuotes) {
				return;
			}
			break;
		}

		TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri.toString());
		
		LanguageServers.forDocument(document).collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
				.thenAccept(lss -> lss.stream().filter(HTMLLanguageServerAPI.class::isInstance)
						.map(HTMLLanguageServerAPI.class::cast).findAny().ifPresent(info -> {
							// The document is bound with HTML language server, consumes the html/autoInsert
							final Display display = viewer.getTextWidget().getDisplay();
							CompletableFuture.supplyAsync(() -> {
								try {
									AutoInsertParams params = new AutoInsertParams();
									params.setTextDocument(identifier);
									params.setKind(autoInsertKind.name());
									params.setPosition(LSPEclipseUtils.toPosition(offset, document));

									// consumes html/autoInsert from HTML language server
									info.autoInsert(params)
											.thenAccept(r -> {
												if (r != null) {
													display.asyncExec(() -> {
														try {
															// we receive a text like
															// $0</foo>
															// $0 should be used for set the cursor.
															String text = r.replace("$0", "").replace("$1", "");
															int index = r.indexOf("$1");

															int replaceLength = 0;
															document.replace(offset, replaceLength, text);
															if (index != -1) {
																viewer.setSelectedRange(offset + index, 0);
															}
															// viewer.setSelectedRange(offset, c)
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

	private boolean isAutoClosingTagEnabled() {
		return Activator.getDefault().getPreferenceStore().getBoolean(HTML_PREFERENCES_AUTO_CLOSING_TAGS);
	}

	private boolean isAutoCreateQuotesEnabled() {
		return Activator.getDefault().getPreferenceStore().getBoolean(HTML_PREFERENCES_AUTO_CREATE_QUOTES);
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
