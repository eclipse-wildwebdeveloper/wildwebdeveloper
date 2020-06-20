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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wildwebdeveloper.embedder.node.Activator;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("restriction")
public class TestNodeJsEmbedder {
	@Rule public AllCleanRule allClean = new AllCleanRule();
	
	@Test
	public void testNodeJsEmbedder() throws Exception {
		URL nodeJsInfo = FileLocator.find(Activator.getDefault().getBundle(), new Path("nodejs-info.properties"));
		assertNotNull("NodeJs descriptor \"nodejs-info.properties\" not found!", nodeJsInfo);

		Properties properties = new Properties();
		try (InputStream infoStream = nodeJsInfo.openStream()) {
			properties.load(infoStream);
		}
		
		assertTrue("Property \"archiveURL\" is not defined", properties.getProperty("archiveURL") != null &&
				!properties.getProperty("archiveURL").isEmpty());
		assertTrue("Property \"archiveFile\" is not defined", properties.getProperty("archiveFile") != null &&
				!properties.getProperty("archiveFile").isEmpty());
		assertTrue("Property \"nodePath\" is not defined", properties.getProperty("nodePath") != null &&
				!properties.getProperty("nodePath").isEmpty());
		
		File nodePath = NodeJSManager.getNodeJsLocation();
		assertNotNull("Node.Js location cannot be found", nodePath);

		IPath stateLocationPath = InternalPlatform.getDefault().getStateLocation(Platform
				.getBundle(Activator.PLUGIN_ID));
		assertNotNull("State location cannot be found for plugin \"" + Activator.PLUGIN_ID + "\"",
				stateLocationPath);

		File installationPath = stateLocationPath.toFile();
		File embeddedNodePath = new File(installationPath, properties.getProperty("nodePath"));
		assertTrue("Embedded NodeJs is not extracted", nodePath.exists() && nodePath.canRead() && nodePath.canExecute());
		assertEquals("Embedded NodeJs installation is not used", nodePath, embeddedNodePath);
	}
	
	@Test
	public void testNodeJsEmbedderWhich() throws Exception {
		URL nodeJsInfo = FileLocator.find(Activator.getDefault().getBundle(), new Path("nodejs-info.properties"));
		assertNotNull("NodeJs descriptor \"nodejs-info.properties\" not found!", nodeJsInfo);

		Properties properties = new Properties();
		try (InputStream infoStream = nodeJsInfo.openStream()) {
			properties.load(infoStream);
		}
		
		assertTrue("Property \"archiveURL\" is not defined", properties.getProperty("archiveURL") != null &&
				!properties.getProperty("archiveURL").isEmpty());
		assertTrue("Property \"archiveFile\" is not defined", properties.getProperty("archiveFile") != null &&
				!properties.getProperty("archiveFile").isEmpty());
		assertTrue("Property \"nodePath\" is not defined", properties.getProperty("nodePath") != null &&
				!properties.getProperty("nodePath").isEmpty());
		
		File nodePath = NodeJSManager.getNodeJsLocation();
		assertNotNull("Node.Js location cannot be found", nodePath);

		IPath stateLocationPath = InternalPlatform.getDefault().getStateLocation(Platform
				.getBundle(Activator.PLUGIN_ID));
		assertNotNull("State location cannot be found for plugin \"" + Activator.PLUGIN_ID + "\"",
				stateLocationPath);

		File installationPath = stateLocationPath.toFile();
		File embeddedNodePath = new File(installationPath, properties.getProperty("nodePath"));
		assertTrue("Embedded NodeJs is not extracted", nodePath.exists() && nodePath.canRead() && nodePath.canExecute());

		File whichNode = NodeJSManager.which("node");
		assertTrue("NodeJSManager.which(\"node\") didn't return an embedded NodeJs", 
				whichNode.exists() && whichNode.canRead() && whichNode.canExecute() &&
				embeddedNodePath.getParent().equals(whichNode.getParent()));
			
		File whichNpm = NodeJSManager.which("npm");
		assertTrue("NodeJSManager.which(\"npm\") didn't return an embedded NodeJs\'s NPM", 
				whichNpm.exists() && whichNpm.canRead() && whichNpm.canExecute() &&
				embeddedNodePath.getParent().equals(whichNpm.getParent()));
		
		File whichNpx = NodeJSManager.which("npx");
		assertTrue("NodeJSManager.which(\"npx\") didn't return an embedded NodeJs\\'s NPX", 
				whichNpx.exists() && whichNpx.canRead() && whichNpx.canExecute() &&
				embeddedNodePath.getParent().equals(whichNpx.getParent()));
	}	
}
