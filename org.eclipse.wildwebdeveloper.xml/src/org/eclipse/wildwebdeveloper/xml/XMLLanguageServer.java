/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class XMLLanguageServer extends ProcessStreamConnectionProvider {

	public XMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(computeJavaPath());
		commands.add("-classpath");
		try {
			URL url = FileLocator
					.toFileURL(getClass().getResource("/language-servers/server/org.eclipse.lsp4xml-0.6.0-uber.jar"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("org.eclipse.lsp4xml.XMLServerLauncher");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	private String computeJavaPath() {
		String javaPath = "java";
		boolean existsInPath = Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator))).map(Paths::get)
				.anyMatch(path -> Files.exists(path.resolve("java")));
		if (!existsInPath) {
			File f = new File(System.getProperty("java.home"),
					"bin/java" + (Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : ""));
			javaPath = f.getAbsolutePath();
		}
		return javaPath;
	}

	@Override
	public String toString() {
		return "XML Language Server: " + super.toString();
	}

}
