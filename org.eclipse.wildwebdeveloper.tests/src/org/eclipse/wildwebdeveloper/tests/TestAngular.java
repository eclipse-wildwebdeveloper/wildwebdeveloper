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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAngular {

	private IProject project;

	@Before
	public void setUpProject() throws Exception {
		LanguageServerPlugin.getDefault().getPreferenceStore().putValue("org.eclipse.wildwebdeveloper.jsts.file.logging.enabled", Boolean.toString(true));
		LanguageServerPlugin.getDefault().getPreferenceStore().putValue("org.eclipse.wildwebdeveloper.css.file.logging.enabled", Boolean.toString(true));
		LanguageServerPlugin.getDefault().getPreferenceStore().putValue("org.eclipse.wildwebdeveloper.html.file.logging.enabled", Boolean.toString(true));
		LanguageServerPlugin.getDefault().getPreferenceStore().putValue("org.eclipse.wildwebdeveloper.json.file.logging.enabled", Boolean.toString(true));
		LanguageServerPlugin.getDefault().getPreferenceStore().putValue("org.eclipse.wildwebdeveloper.xml.file.logging.enabled", Boolean.toString(true));
		LanguageServerPlugin.getDefault().getPreferenceStore().putValue("org.eclipse.wildwebdeveloper.yaml.file.logging.enabled", Boolean.toString(true));
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IViewReference ref : activePage.getViewReferences()) {
			activePage.hideView(ref);
		}
	}

	@After
	public void deleteProjectAndCloseEditors() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		this.project.delete(true, null);
	}

	@Test
	public void testAngularTSFile() throws Exception {
		URL url = FileLocator.find(Platform.getBundle("org.eclipse.wildwebdeveloper.tests"),
				Path.fromPortableString("testProjects/angular-app"), null);
		url = FileLocator.toFileURL(url);
		File folder = new File(url.getFile());
		if (folder.exists()) {

			java.nio.file.Path sourceFolder = folder.toPath();
			java.nio.file.Path destFolder = project.getLocation().toFile().toPath();

			Files.walk(sourceFolder).forEach(source -> {
				try {
					Files.copy(source, destFolder.resolve(sourceFolder.relativize(source)), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			Process process = new ProcessBuilder(getNpmLocation(), "install", "--no-bin-links", "--ignore-scripts")
					.directory(project.getLocation().toFile()).start();
			while (process.isAlive()) {
				;
			}
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

			IFile file = project.getFolder("src").getFolder("app").getFile("app.component.ts.content");
			String fileContent = new BufferedReader(new InputStreamReader(file.getContents())).lines()
					.collect(Collectors.joining("\n"));

			IFile appComponentFile = project.getFolder("src").getFolder("app").getFile("app.component.ts");
			appComponentFile.create(new ByteArrayInputStream("".getBytes()), true, null);
			assertTrue("Diagnostic published on empty file", new DisplayHelper() {
				@Override
				protected boolean condition() {
					try {
						return appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
								IResource.DEPTH_ZERO).length == 0;
					} catch (CoreException e) {
						return false;
					}
				}
			}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));

			ITextEditor editor = (ITextEditor) IDE
					.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), appComponentFile);
			editor.getDocumentProvider().getDocument(editor.getEditorInput()).set(fileContent);
			assertTrue("Diagnostic not published", new DisplayHelper() {
				@Override
				protected boolean condition() {
					try {
						return appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
								IResource.DEPTH_ZERO).length != 0;
					} catch (CoreException e) {
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
