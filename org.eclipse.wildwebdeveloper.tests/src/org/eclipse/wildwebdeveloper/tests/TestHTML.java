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

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Rule;
import org.junit.Test;

public class TestHTML {

	@Rule public AllCleanRule cleanRule = new AllCleanRule();

	@Test
	public void testHTMLFile() throws Exception {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testHTMLFile" + System.currentTimeMillis());
		project.create(null);
		project.open(null);
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
	public void testFormat() throws Exception {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testHTMLFile" + System.currentTimeMillis());
		project.create(null);
		project.open(null);
		final IFile file = project.getFile("blah.html");
		file.create(new ByteArrayInputStream("<html><body><a></a></body></html>".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.setFocus();
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0));
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		AtomicReference<Exception> ex = new AtomicReference<>();
		new DisplayHelper() {
			@Override protected boolean condition() {
				try {
					handlerService.executeCommand("org.eclipse.lsp4e.format", null);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		}.waitForCondition(editor.getSite().getShell().getDisplay(), 3000);
		if (ex.get() != null) {
			throw ex.get();
		}
		new DisplayHelper() {
			@Override protected boolean condition() {
				return editor.getDocumentProvider().getDocument(editor.getEditorInput()).getNumberOfLines() > 1;
			}
		}.waitForCondition(editor.getSite().getShell().getDisplay(), 3000);
	}
}
