/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Handles the {@code xml.show.references} command sent by LemMinX via a code
 * lens, triggering a "Find References" search at the given position in the
 * given file.
 *
 * <pre>
 * "command": {
 *   "title": "1 reference",
 *   "command": "xml.show.references",
 *   "arguments": [
 *     "file:///c%3A/Users/AngeloZerr/git/vscode-xml/foo.xsd",
 *     { "line": 3, "character": 20 }
 *   ]
 * }
 * </pre>
 *
 * @author Angelo ZERR
 */
public class ShowReferencesHandler extends LSPCommandHandler {

	private static final String FIND_REFERENCES_COMMAND = "org.eclipse.ui.genericeditor.findReferences";

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String uri = ArgumentsUtils.getArgAt(command, 0, String.class);
		Position position = ArgumentsUtils.getArgAt(command, 1, Position.class);
		try {
			IFileStore fileStore = EFS.getStore(URI.create(uri));
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					// openEditorOnFileStore returns existing editor if already open.
					IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
					revealAndFindReferences(editor, position);
				} catch (Exception e) {
					ILog.get().error("Error showing references for URI: " + uri, e);
				}
			});
		} catch (Exception e) {
			ILog.get().error("Error showing references for URI: " + uri, e);
		}
		return null;
	}

	private static void revealAndFindReferences(IEditorPart editor, Position position) throws Exception {
		if (editor instanceof ITextEditor textEditor) {
			IDocument document = LSPEclipseUtils.getDocument(textEditor);
			if (document != null) {
				int offset = LSPEclipseUtils.toOffset(position, document);
				textEditor.selectAndReveal(offset, 0);
				IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
				handlerService.executeCommand(FIND_REFERENCES_COMMAND, null);
			}
		}
	}

}
