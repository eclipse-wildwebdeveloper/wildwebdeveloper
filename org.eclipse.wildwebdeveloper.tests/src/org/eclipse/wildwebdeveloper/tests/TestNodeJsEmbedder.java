/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wildwebdeveloper.embedder.node.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("restriction")
@ExtendWith(AllCleanRule.class)
public class TestNodeJsEmbedder {

	@Test
	public void testNodeJsEmbedder() throws Exception {
		URL nodeJsInfo = FileLocator.find(Activator.getDefault().getBundle(), new Path("nodejs-info.properties"));
		assertNotNull(nodeJsInfo, "NodeJs descriptor \"nodejs-info.properties\" not found!");

		Properties properties = new Properties();
		try (InputStream infoStream = nodeJsInfo.openStream()) {
			properties.load(infoStream);
		}

		assertTrue(properties.getProperty("archiveURL") != null && !properties.getProperty("archiveURL").isEmpty(),
				"Property \"archiveURL\" is not defined");
		assertTrue(properties.getProperty("archiveFile") != null && !properties.getProperty("archiveFile").isEmpty(),
				"Property \"archiveFile\" is not defined");
		assertTrue(properties.getProperty("nodePath") != null && !properties.getProperty("nodePath").isEmpty(),
				"Property \"nodePath\" is not defined");

		File nodePath = NodeJSManager.getNodeJsLocation();
		assertNotNull(nodePath, "Node.Js location cannot be found");

		IPath stateLocationPath = InternalPlatform.getDefault()
				.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));
		assertNotNull(stateLocationPath, "State location cannot be found for plugin \"" + Activator.PLUGIN_ID + "\"");

		File installationPath = stateLocationPath.toFile();
		File embeddedNodePath = new File(installationPath, properties.getProperty("nodePath"));
		assertTrue(nodePath.exists() && nodePath.canRead() && nodePath.canExecute(),
				"Embedded NodeJs is not extracted");
		assertEquals(nodePath, embeddedNodePath, "Embedded NodeJs installation is not used");
	}

	@Test
	public void testNodeJsEmbedderWhich() throws Exception {
		URL nodeJsInfo = FileLocator.find(Activator.getDefault().getBundle(), new Path("nodejs-info.properties"));
		assertNotNull(nodeJsInfo, "NodeJs descriptor \"nodejs-info.properties\" not found!");

		Properties properties = new Properties();
		try (InputStream infoStream = nodeJsInfo.openStream()) {
			properties.load(infoStream);
		}

		assertTrue(properties.getProperty("archiveURL") != null && !properties.getProperty("archiveURL").isEmpty(),
				"Property \"archiveURL\" is not defined");
		assertTrue(properties.getProperty("archiveFile") != null && !properties.getProperty("archiveFile").isEmpty(),
				"Property \"archiveFile\" is not defined");
		assertTrue(properties.getProperty("nodePath") != null && !properties.getProperty("nodePath").isEmpty(),
				"Property \"nodePath\" is not defined");

		File nodePath = NodeJSManager.getNodeJsLocation();
		assertNotNull(nodePath, "Node.Js location cannot be found");

		IPath stateLocationPath = InternalPlatform.getDefault()
				.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));
		assertNotNull(stateLocationPath, "State location cannot be found for plugin \"" + Activator.PLUGIN_ID + "\"");

		File installationPath = stateLocationPath.toFile();
		File embeddedNodePath = new File(installationPath, properties.getProperty("nodePath"));
		assertTrue(nodePath.exists() && nodePath.canRead() && nodePath.canExecute(),
				"Embedded NodeJs is not extracted");

		File whichNode = NodeJSManager.which("node");
		assertTrue(
				whichNode.exists() && whichNode.canRead() && whichNode.canExecute()
						&& embeddedNodePath.getParent().equals(whichNode.getParent()),
				"NodeJSManager.which(\"node\") didn't return an embedded NodeJs");

		File whichNpm = NodeJSManager.which("npm");
		assertTrue(
				whichNpm.exists() && whichNpm.canRead() && whichNpm.canExecute()
						&& embeddedNodePath.getParent().equals(whichNpm.getParent()),
				"NodeJSManager.which(\"npm\") didn't return an embedded NodeJs\'s NPM");

		File whichNpx = NodeJSManager.which("npx");
		assertTrue(
				whichNpx.exists() && whichNpx.canRead() && whichNpx.canExecute()
						&& embeddedNodePath.getParent().equals(whichNpx.getParent()),
				"NodeJSManager.which(\"npx\") didn't return an embedded NodeJs\\'s NPX");
	}

	@Test
	public void testNPMSeemsCorrect() {
		File npm = NodeJSManager.getNpmLocation();
		assertTrue(npm.isFile());
		assertTrue(npm.length() > 0);
		assertTrue(Platform.OS_WIN32.equals(Platform.getOS()) || Files.isSymbolicLink(npm.toPath()));
	}
}
