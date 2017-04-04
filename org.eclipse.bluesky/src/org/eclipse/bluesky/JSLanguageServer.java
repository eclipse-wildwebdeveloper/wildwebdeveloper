package org.eclipse.bluesky;

import java.io.File;
import java.util.Arrays;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class JSLanguageServer extends ProcessStreamConnectionProvider {

	public JSLanguageServer() {
		super(Arrays.asList(InitializeLaunchConfigurations.getNodeJsLocation(), " "), new File(".").getAbsolutePath());
	}
}
