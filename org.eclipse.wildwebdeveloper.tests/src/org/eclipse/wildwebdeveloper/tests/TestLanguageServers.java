/*******************************************************************************
 * Copyright (c) 2018, 2019 Red Hat Inc. and others.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLanguageServers {

	private IProject project;

	@Before
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
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IViewReference ref : activePage.getViewReferences()) {
			activePage.hideView(ref);
		}
	}

	@After
	public void deleteProjectAndCloseEditors() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		this.project.delete(true, null);
		DisplayHelper.sleep(300);
	}

	@Test
	public void testCSSFile() throws Exception {
		final IFile file = project.getFile("blah.css");
		file.create(new ByteArrayInputStream("ERROR".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testHTMLFile() throws Exception {
		final IFile file = project.getFile("blah.html");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("<style\n<html><");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testYAMLFile() throws Exception {
		final IFile file = project.getFile("blah.yaml");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("hello: '");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testJSONFile() throws Exception {
		final IFile file = project.getFile("blah.json");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("ERROR");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testJSFile() throws Exception {
		final IFile file = project.getFile("blah.js");
		file.create(new ByteArrayInputStream("ERROR".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		DisplayHelper.sleep(2000); // Give time for LS to initialize enough before making edit and sending a didChange
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("a<");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testTSFile() throws Exception {
		final IFile file = project.getFile("blah.ts");
		file.create(new ByteArrayInputStream("ERROR".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testJSXFile() throws Exception {
		final IFile file = project.getFile("blah.jsx");
		file.create(new ByteArrayInputStream("ERROR".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("a<");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testTSXFile() throws Exception {
		final IFile file = project.getFile("blah.tsx");
		file.create(new ByteArrayInputStream("ERROR".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000));
	}

	@Test
	public void testResourcesPathIsntTooLong() throws Exception {
		final int MAX_ALLOWED_RELATIVE_PATH = 140; // that leaves 120 characters for the path to the bundle
//C:/Users/Jean-Jacques Saint-Romain/developpement/eclipse/plugins/org.eclipse.wildwebdeveloper_1.2.3.20201212_1605/
		String location = Platform.getBundle("org.eclipse.wildwebdeveloper").getLocation();
		File file = new File(URI.create(location.substring("reference:".length())));
		assertTrue(file.isDirectory());
		Set<String> tooLongPaths = new HashSet<>();
		Path pluginPath = file.toPath();
		Files.walkFileTree(pluginPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				String relativePathInsideBundle = pluginPath.relativize(dir).toString();
				if (relativePathInsideBundle.startsWith("target")) {
					return FileVisitResult.SKIP_SUBTREE;
				} else if (relativePathInsideBundle.toString().length() > MAX_ALLOWED_RELATIVE_PATH) {
					tooLongPaths.add(relativePathInsideBundle);
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String relativePathInsideBundle = pluginPath.relativize(file).toString();
				if (relativePathInsideBundle.length() > MAX_ALLOWED_RELATIVE_PATH) {
					tooLongPaths.add(relativePathInsideBundle);
				}
				return FileVisitResult.CONTINUE;
			}
		});
		assertEquals(Collections.emptySet(), tooLongPaths);
	}
}
