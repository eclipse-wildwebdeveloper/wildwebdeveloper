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
*   Andrew Obuchowicz (Red Hat Inc.) - initial implementation
*******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestESLint {

	private IProject project;

	@BeforeEach
	public void setUpProject() throws Exception {
		String projectName = getClass().getName() + System.nanoTime();
		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		IPath projectLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(projectName);
		desc.setLocation(projectLocation);
		File projectDirectory = projectLocation.toFile();
		projectDirectory.mkdir();
		try (InputStream eslintRc = getClass().getResourceAsStream("/testProjects/eslint/.eslintrc")) {
			Files.copy(eslintRc, new File(projectDirectory, ".eslintrc").toPath());
		}
		try (InputStream eslintRc = getClass().getResourceAsStream("/testProjects/eslint/tsconfig.json")) {
			Files.copy(eslintRc, new File(projectDirectory, "tsconfig.json").toPath());
		}
		try (InputStream eslintRc = getClass().getResourceAsStream("/testProjects/eslint/package.json")) {
			Files.copy(eslintRc, new File(projectDirectory, "package.json").toPath());
		}
		try (InputStream eslintRc = getClass().getResourceAsStream("/testProjects/eslint/ESLintProj.js")) {
			Files.copy(eslintRc, new File(projectDirectory, "ESLintProj.js").toPath());
		}
		try (InputStream eslintRc = getClass().getResourceAsStream("/testProjects/eslint/ESLintProj.js")) {
			Files.copy(eslintRc, new File(projectDirectory, "ESLintProj.ts").toPath());
		}
		Process dependencyInstaller = new ProcessBuilder(NodeJSManager.getNpmLocation().getAbsolutePath(), "install")
				.directory(projectDirectory).start();
		assertEquals(0, dependencyInstaller.waitFor(), "npm install didn't complete properly");
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		this.project.create(desc, null);
		project.open(null);
	}

	@Test
	public void testESLintDiagnostics() throws Exception {
		IFile file = project.getFile("ESLintProj.js");
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		assertESLintIndentMarkerExists(file);

		file = project.getFile("ESLintProj.ts");
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		assertESLintIndentMarkerExists(file);
	}

	private void assertESLintIndentMarkerExists(IFile file) throws PartInitException {
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 10000), "Diagnostic not published");

		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO)[0]
							.getAttribute(IMarker.MESSAGE, null).toLowerCase().contains("indentation");
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000), "Diagnostic content is incorrect");
	}
}
