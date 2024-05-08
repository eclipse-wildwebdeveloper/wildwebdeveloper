/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.angular;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class AngularLanguageServer extends ProcessStreamConnectionProvider {

	private static final String LOG_TO_FILE_ANGULAR_LS_PREFERENCE = "org.eclipse.wildwebdeveloper.angular.file.logging.enabled";
	private static final String LOG_TO_CONSOLE_ANGULAR_LS_PREFERENCE = "org.eclipse.wildwebdeveloper.angular.stderr.logging.enabled";

	private boolean isLoggingToFileEnabled;
	private boolean isLoggingToConsoleEnabled;
	
	public AngularLanguageServer() {
		
		ScopedPreferenceStore scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.lsp4e");
		this.isLoggingToFileEnabled = scopedPreferenceStore.getBoolean(LOG_TO_FILE_ANGULAR_LS_PREFERENCE);
		this.isLoggingToConsoleEnabled = scopedPreferenceStore.getBoolean(LOG_TO_CONSOLE_ANGULAR_LS_PREFERENCE);
		
		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			URL url = FileLocator.toFileURL(getClass().getResource("/node_modules/@angular/language-server/index.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			File nodeModules = new File(url.getPath()).getParentFile().getParentFile().getParentFile();
			commands.add("--ngProbeLocations");
			commands.add(new File(nodeModules, "@angular/language-service").getAbsolutePath());
			commands.add("--tsProbeLocations");
			commands.add(new File(nodeModules, "typescript").getAbsolutePath());
			commands.add("--disableAutomaticNgcc"); // See: https://github.com/eclipse/wildwebdeveloper/issues/836
			commands.add("--stdio");
			if (isLoggingToFileEnabled) {
				commands.add("--logFile");
				commands.add(Platform.getLogFileLocation().removeLastSegments(1)
					.append("angular-language-server.log").toFile()
					.getAbsolutePath());
			}
			commands.add("--logVerbosity");
			commands.add("terse");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	@Override protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = super.createProcessBuilder();
		if (this.isLoggingToFileEnabled || this.isLoggingToConsoleEnabled) {
			builder.environment().put("NG_DEBUG", Boolean.toString(true));
		}
		builder.environment().put("TSC_NONPOLLING_WATCHER", Boolean.toString(true));
		return builder;
	}
	
	@Override
	public String toString() {
		return "Angular Language Server: " + super.toString();
	}
}
