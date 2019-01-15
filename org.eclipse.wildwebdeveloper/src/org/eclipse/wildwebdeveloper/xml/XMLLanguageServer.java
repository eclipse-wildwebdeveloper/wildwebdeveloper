/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xi Yan (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.wildwebdeveloper.Activator;

public class XMLLanguageServer extends ProcessStreamConnectionProvider {

	public XMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-classpath");
		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/language-servers/org.eclipse.lsp4xml-all.jar"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("org.eclipse.lsp4xml.XMLServerLauncher");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	@Override
	public String toString() {
		return "XML Language Server: " + super.toString();
	}

}
