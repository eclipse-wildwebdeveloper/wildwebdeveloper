package org.eclipse.wildwebdeveloper.jsts;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class JSTSLanguageServer extends ProcessStreamConnectionProvider {

	public JSTSLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/language-servers/node_modules/javascript-typescript-langserver/lib/language-server-stdio.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}
	
}
