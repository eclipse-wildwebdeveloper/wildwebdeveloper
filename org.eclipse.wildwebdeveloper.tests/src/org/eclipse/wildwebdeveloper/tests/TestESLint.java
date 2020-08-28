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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestESLint {

	private IProject project;

	@BeforeEach
	public void setUpProject() throws Exception {
		ScopedPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.lsp4e");
		prefs.putValue("org.eclipse.wildwebdeveloper.angular.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.jsts.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.css.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.html.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.json.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.xml.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.yaml.file.logging.enabled", Boolean.toString(true));
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);

		// Setup ESLint configuration and dependencies
		IFile eslintConfig = project.getFile(".eslintrc");
		eslintConfig.create(getClass().getResourceAsStream("/testProjects/eslint/.eslintrc"), true, null);
		IFile tsConfig = project.getFile("tsconfig.json");
		tsConfig.create(getClass().getResourceAsStream("/testProjects/eslint/tsconfig.json"), true, null);
		IFile packageJson = project.getFile("package.json");
		packageJson.create(getClass().getResourceAsStream("/testProjects/eslint/package.json"), true, null);
		Process dependencyInstaller = new ProcessBuilder(TestAngular.getNpmLocation(), "install")
				.directory(project.getLocation().toFile()).start();
		assertEquals(0, dependencyInstaller.waitFor(), "npm install didn't complete properly");

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IViewReference ref : activePage.getViewReferences()) {
			activePage.hideView(ref);
		}
	}

	@AfterEach
	public void deleteProjectAndCloseEditors() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		this.project.delete(true, null);
	}

	@Test
	public void testESLintDiagnosticsTS() throws Exception {
		IFile file = project.getFile("blah.ts");
		file.create(getClass().getResourceAsStream("/testProjects/eslint/ESLintProj.ts"), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		DisplayHelper.sleep(4000); // Give time for ESLint language service to initialize

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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000), "Diagnostic not published");

		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO)[0]
							.getAttribute(IMarker.MESSAGE, null).contains(
									"[@typescript-eslint/explicit-function-return-type] Missing return type on function.");
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000), "Diagnostic content is incorrect");
	}

	@Test
	public void testESLintDiagnosticsJS() throws Exception {
		IFile file = project.getFile("blah.js");
		file.create(getClass().getResourceAsStream("/testProjects/eslint/ESLintProj.js"), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		DisplayHelper.sleep(4000); // Give time for ESLint language service to initialize

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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000), "Diagnostic not published");

		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO)[0]
							.getAttribute(IMarker.MESSAGE, null)
							.contains("[@typescript-eslint/indent] Expected indentation of 0 spaces but found 9.");
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000), "Diagnostic content is incorrect");
	}

}
