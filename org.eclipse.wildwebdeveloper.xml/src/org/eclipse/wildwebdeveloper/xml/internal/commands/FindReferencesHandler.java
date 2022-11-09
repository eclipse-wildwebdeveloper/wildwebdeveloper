package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

public class FindReferencesHandler extends LSPCommandHandler {

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part instanceof ITextEditor editor) {
			IDocument document = LSPEclipseUtils.getDocument(editor);
			if (document == null) {
				return null;
			}
			try {
				Map<String, Object> position = (Map<String, Object>) command.getArguments().get(1);
				int line = ((Number) position.get("line")).intValue();
				int character = ((Number) position.get("character")).intValue();
				int offset = LSPEclipseUtils.toOffset(new Position(line, character), document);
				LSPEclipseUtils.searchLSPReferences(document, offset, HandlerUtil.getActiveShell(event).getDisplay());
			} catch (BadLocationException e) {
				Activator.getDefault().getLog().error("Error while getting offset for references", e);
			}
		}
		return null;
	}
}
