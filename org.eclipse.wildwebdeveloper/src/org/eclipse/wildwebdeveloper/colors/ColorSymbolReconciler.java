/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.wildwebdeveloper.colors;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.Range;

/**
 * {@link IReconciler} which consumes language server to get color ranges and
 * update viewer by drawing colorized square.
 *
 */
public class ColorSymbolReconciler extends AbstractReconciler {

	private CompletableFuture<List<Range>> promise;

	private ColorSymbolSupport colorSupport;

	@Override
	public void install(ITextViewer viewer) {
		super.install(viewer);
		// Install color support
		colorSupport = new ColorSymbolSupport();
		colorSupport.install((ISourceViewer) viewer);
	}

	@Override
	public void uninstall() {
		super.uninstall();
		colorSupport.uninstall();
		cancel();
	}

	@Override
	protected void initialProcess() {
		process(null);
	}

	@Override
	protected void process(DirtyRegion dirtyRegion) {
		// FIXME: I don't know which capabilities I must test????
		List<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(getDocument(),
				capabilities -> true);
		// FIXME: lsp4e should provide a public API: see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=521925
		if (infos.isEmpty()) {
			return;
		}
		LSPDocumentInfo info = infos.get(0);
		// Cancel last call of findDocumentColors
		cancel();
		IDocument document = info.getDocument();
		// Search list of color range
		promise = ((DocumentColorProvider) info.getLanguageClient()).findDocumentColors(info.getFileUri());
		promise.thenAccept(ranges -> {
			// then update the UI
			colorSupport.colorize(ranges);
		});
	}

	@Override
	protected void reconcilerDocumentChanged(IDocument newDocument) {

	}

	/**
	 * Cancel the last call of 'documentHighlight'.
	 */
	private void cancel() {
		if (promise != null && !promise.isDone()) {
			promise.cancel(true);
			promise = null;
		}
	}

	@Override
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		return null;
	}

}
