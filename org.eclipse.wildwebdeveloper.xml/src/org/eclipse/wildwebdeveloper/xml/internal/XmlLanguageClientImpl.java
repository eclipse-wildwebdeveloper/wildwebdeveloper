package org.eclipse.wildwebdeveloper.xml.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4e.command.CommandExecutor;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandParams;

@SuppressWarnings("restriction")
public class XmlLanguageClientImpl extends LanguageClientImpl implements XMLLanguageClient{
		
	public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params) {
		Command cmd = new Command();
		cmd.setCommand(params.getCommand());
		cmd.setArguments(params.getArguments());
		return CommandExecutor.executeCommand(cmd, null, null);
	}

}
