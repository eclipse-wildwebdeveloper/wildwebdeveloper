package org.eclipse.wildwebdeveloper.xml.internal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.ExecuteCommandParams;

@SuppressWarnings("restriction")
public class XmlLanguageClientImpl extends LanguageClientImpl implements XMLLanguageClient{
	
	private CommandExecutor commandExecutor = new CommandExecutor();
	
	public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params) {
		String id = params.getCommand();
		List<Object> args = params.getArguments();
		return commandExecutor.executeClientCommand(id, args.toArray());
	}


}
