package org.eclipse.wildwebdeveloper.xml.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface XMLLanguageClient {
	
	@JsonRequest("xml/executeClientCommand")
	CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params);

}
