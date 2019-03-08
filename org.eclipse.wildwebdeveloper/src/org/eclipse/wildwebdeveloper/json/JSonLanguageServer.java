/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Michal� Niewrzal� (Rogue Wave Software Inc.) - initial implementation
 *  Angelo Zerr <angelo.zerr@gmail.com> - JSON Schema support
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.json;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.InitializeLaunchConfigurations;
import org.osgi.framework.Bundle;

public class JSonLanguageServer extends ProcessStreamConnectionProvider {

	public JSonLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add(InitializeLaunchConfigurations.getNodeJsLocation());
		try {
			URL url = FileLocator.toFileURL(getClass()
					.getResource("/language-servers/node_modules/vscode-json-languageserver/out/jsonServerMain.js"));
			commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			commands.add("--stdio");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootUri) {
		if (message instanceof ResponseMessage) {
			ResponseMessage responseMessage = (ResponseMessage) message;
			if (responseMessage.getResult() instanceof InitializeResult) {
				// Send json/schemaAssociations notification to register JSON Schema on JSON
				// Language server side.
				JSonLanguageServerInterface server = (JSonLanguageServerInterface) languageServer;
				server.sendJSonchemaAssociations(getSchemaAssociations());
			}
		}
	}

	/**
	 * Returns JSON Schema associations defined in extension point
	 * 
	 * @return associations
	 */
	private Map<String, List<String>> getSchemaAssociations() {
		Map<String, List<String>> associations = new HashMap<>();
		
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.wildwebdeveloper.schemaassociations");
		if (point == null)
			return null;
		
		IExtension[] extensions = point.getExtensions();
		for (IExtension e : extensions) {
			IConfigurationElement[] schemaAssociation = e.getConfigurationElements();
			for (IConfigurationElement s : schemaAssociation) {
				
				// collect all file patterns
				IConfigurationElement[] filePatterns = s.getChildren("filePattern");
				List<String> filePatternList = new ArrayList<>();
				for (IConfigurationElement pattern: filePatterns) {
					String filePattern = pattern.getAttribute("filePattern");
					if (filePattern != null) {
						filePatternList.add(filePattern);
					} else {
						logExtensionWarning("File pattern must not be empty!", null);
					}
				}
				// collect all schema paths
				IConfigurationElement[] schemaPaths = s.getChildren();
				List<String> schemaPathList = new ArrayList<>();
				for (IConfigurationElement location : schemaPaths) {
					String filePath = null;
					switch (location.getName()) {
					case "filePattern":
						continue;
					case "bundleFilePath":
						filePath = getPathFromBundleFilePath(location, e);
						break;
					case "url":
						filePath = location.getAttribute("url");
						break;
					}
					if (filePath != null) {
						schemaPathList.add(filePath);
					} else {
						logExtensionWarning("Couldn't read file path from schema association extension " + e.getLabel(),
								null);
					}
				}

				for (String pattern : filePatternList) {
					associations.put(pattern, schemaPathList);
				}
			}
		}

		return associations;
	}

	/**
	 * Returns exact file location in a plug-in. If the bundle ID is not specified,
	 * the bundle ID of the plug-in registering this extension is used.
	 * 
	 * @param  location   the schema path configuration element
	 * @param  extension  the extension of the schema association extension point, where location is configured
	 * @return            the absolute file path
	 */
	private String getPathFromBundleFilePath(IConfigurationElement location, IExtension extension) {
		try {
			String relativePath = location.getAttribute("relativePath");
			String bundleId;
			if (location.getAttribute("bundleId") != null) {
				bundleId = location.getAttribute("bundleId");
			} else {
				bundleId = extension.getContributor().getName();
			}

			if (relativePath == null || bundleId == null) {
				return null;
			}

			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle == null) {
				return null;
			}

			URL url = FileLocator.find(bundle, new Path(relativePath), null);
			URL fileUrl = FileLocator.toFileURL(url);
			
			return "file://" +  new java.io.File(fileUrl.getPath()).getAbsolutePath();
		} catch (IOException e) {
			logExtensionWarning("Error while reading schema association extension " + extension.getLabel(), e);
			return null;
		}
	}

	private void logExtensionWarning(String msg, Exception ex) {
		Activator.getDefault().getLog()
				.log(new Status(IStatus.WARNING, Activator.getDefault().getBundle().getSymbolicName(), msg, ex));
	}
}
