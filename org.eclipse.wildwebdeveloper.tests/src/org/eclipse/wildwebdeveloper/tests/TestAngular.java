/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Rule;
import org.junit.Test;

public class TestAngular {

	@Rule public AllCleanRule cleanRule = new AllCleanRule();

	@Test
	public void testAngularTSFile() throws Exception {
		IProject project = Utils.provisionTestProject("angular-app");
		Process process = new ProcessBuilder(getNpmLocation(), "install", "--no-bin-links", "--ignore-scripts")
				.directory(project.getLocation().toFile()).start();
		assertEquals("npm install didn't complete property", 0, process.waitFor());
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		IFile appComponentFile = project.getFolder("src").getFolder("app").getFile("app.component.ts");
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), appComponentFile);
		DisplayHelper.sleep(4000); // Give time for LS to initialize enough before making edit and sending a didChange
		// make an edit
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		document.set(document.get() + "\n");

		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
							IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 50000));

		IMarker[] markers = appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
				IResource.DEPTH_ZERO);
		boolean foundError = false;
		for (IMarker marker : markers) {
			int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			if (lineNumber == 6 && marker.getAttribute(IMarker.MESSAGE, "").contains("template")) {
				foundError = true;
			}
		}
		assertTrue("No error found in line 6", foundError);
	}

	public static String getNpmLocation() {
		String res = "/path/to/npm";
		String[] command = new String[] { "/bin/bash", "-c", "which npm" };
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where npm" };
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			res = reader.readLine();
		} catch (IOException e) {
			return Platform.getOS().equals(Platform.OS_WIN32) ? "npm.cmd" : "npm";
		}

		// Try default install path as last resort
		if (res == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/usr/local/bin/npm";
		} else if (res == null && Platform.getOS().equals(Platform.OS_LINUX)) {
			res = "/usr/bin/npm";
		}

		if (res != null && Files.exists(Paths.get(res))) {
			return res;
		}
		return Platform.getOS().equals(Platform.OS_WIN32) ? "npm.cmd" : "npm";
	}

}
