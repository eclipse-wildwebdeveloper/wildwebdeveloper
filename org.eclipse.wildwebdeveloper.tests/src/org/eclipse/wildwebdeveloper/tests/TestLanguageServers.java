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
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLanguageServers {

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
	}

	@After
	public void deleteProjectAndCloseEditors() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		this.project.delete(true, null);
	}

	@Test
	public void testCSSFile() throws Exception {
		final IFile file = project.getFile("blah.css");
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}

	@Test
	public void testHTMLFile() throws Exception {
		final IFile file = project.getFile("blah.html");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}
	
	@Test
	public void testYAMLFile() throws Exception {
		final IFile file = project.getFile("blah.yaml");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("hello: ");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}
	
	@Test
	public void testXMLFile() throws Exception {
		final IFile file = project.getFile("blah.xml");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("<plugin></");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}
	
	@Test
	public void testXSLFile() throws Exception {
		final IFile file = project.getFile("blah.xsl");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}
	
	@Test
	public void testXSDFile() throws Exception {
		final IFile file = project.getFile("blah.xsd");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}
	
	@Test
	public void testDTDFile() throws Exception {
		final IFile file = project.getFile("blah.dtd");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("<!");
		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}
	
	@Test
	public void testJSONFile() throws Exception {
		final IFile file = project.getFile("blah.json");
		file.create(new ByteArrayInputStream("FAIL".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}

	@Test
	public void testJSFile() throws Exception {
		final IFile file = project.getFile("blah.js");
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}

	@Test
	public void testTSFile() throws Exception {
		final IFile file = project.getFile("blah.ts");
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
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 3000));
	}

}
