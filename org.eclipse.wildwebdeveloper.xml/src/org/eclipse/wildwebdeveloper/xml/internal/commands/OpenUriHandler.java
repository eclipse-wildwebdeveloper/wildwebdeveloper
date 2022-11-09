package org.eclipse.wildwebdeveloper.xml.internal.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;

public class OpenUriHandler extends LSPCommandHandler {

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String uri = (String) command.getArguments().get(0);
		LSPEclipseUtils.open(uri, null);
		return null;
	}

}
