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
 *  Dawid Pakuła - modified copy for Vue
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue.autoinsert;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.core.runtime.ILog;
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
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.vue.VueLanguageServerAPI;

/**
 * {@link IReconciler} implementation used to support auto close tags / auto
 * insert quote, features provides by the vscode VUE language server with the
 * custom 'volar/client/autoInsert' LSP request.
 *
 */
public class VueAutoInsertReconciler implements IReconciler {

	private IDocument document;

	private ITextViewer viewer;

	private Listener listener;

	private void autoInsert(DocumentEvent event) {
		if (event == null || viewer == null) {
			return;
		}
		IDocument document = event.getDocument();
		if (document == null || event == null || event.getLength() != 0) {
			return;
		}

		int offset = event.getOffset();
		URI uri = LSPEclipseUtils.toUri(document);
		if (uri == null) {
			return;
		}
		
		
		TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri.toString());
		
		LanguageServers.forDocument(document).collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
				.thenAccept(lss -> lss.stream().filter(VueLanguageServerAPI.class::isInstance)
						.map(VueLanguageServerAPI.class::cast).findAny().ifPresent(info -> {
							// The document is bound with HTML language server, consumes the html/autoInsert
							final Display display = viewer.getTextWidget().getDisplay();
							CompletableFuture.supplyAsync(() -> {
								
								try {
									
									AutoInsertParams params = new AutoInsertParams();
									params.setTextDocument(identifier);
									params.setPosition(LSPEclipseUtils.toPosition(offset + event.getText().length(), document));
									
									AutoInsertOptions opts = new AutoInsertOptions();
									AutoInsertLastChange changeEvent = new AutoInsertLastChange();
									final var range = new Range(LSPEclipseUtils.toPosition(offset, document),
											LSPEclipseUtils.toPosition(offset + event.fLength, document));
									changeEvent.setRange(range);
									changeEvent.setText(event.getText());
									changeEvent.setRangeLength(event.fLength);
									changeEvent.setRangeOffset(offset);
									opts.setLastChange(changeEvent);
									params.setOptions(opts);

									// consumes String or AutoInsertResponse from Vue Server
									info.autoInsert(params)
											.thenAccept(response -> {
												if (response != null) { 
													display.asyncExec(() -> {
														try {
															// we receive a text like
															// $0</foo>
															// $0 should be used for set the cursor.
															String newText = response.map(Function.identity(), AutoInsertResponse::getNewText);
															
															String text = newText.replace("$0", "").replace("$1", "");
															
															
															int index = newText.indexOf("$0");

															int replaceLength = 0;
															int replacePosition = offset + event.getText().length();
															if (response.isRight()) {
																replacePosition = LSPEclipseUtils.toOffset(response.getRight().getRange().getStart(), document);
															}
															document.replace(replacePosition, replaceLength, text);
															if (index != -1) {
																viewer.setSelectedRange(replacePosition + index, 0);
															}
															// viewer.setSelectedRange(offset, c)
														} catch (BadLocationException e) {
															ILog.get().error(e.getMessage(), e);
														}
													});

												}
											});
								} catch (BadLocationException e) {
									// Do nothing
									ILog.get().error(e.getMessage(), e);
								}
								return null;
							});
						}));
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
			System.out.println(e.toString());
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
