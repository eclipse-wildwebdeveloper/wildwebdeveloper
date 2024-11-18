/*******************************************************************************
 * Copyright (c) 2019, 2024 Red Hat Inc. and others.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestHTML {

    @Test
    public void testHTMLFile() throws Exception {
        final IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("testHTMLFile" + System.currentTimeMillis());
        project.create(null);
        project.open(null);
        final IFile file = project.getFile("blah.html");
        file.create("FAIL".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("<style\n<html><");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testFormat() throws Exception {
        final IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("testHTMLFile" + System.currentTimeMillis());
        project.create(null);
        project.open(null);
        final IFile file = project.getFile("blah.html");
        file.create("<html><body><a></a></body></html>".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.setFocus();
        editor.getSelectionProvider().setSelection(new TextSelection(0, 0));
        IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
//		assertTrue(PlatformUI.getWorkbench().getService(ICommandService.class).getCommand("org.eclipse.lsp4e.format")
//				.isEnabled());
        DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 3000, () -> {
            try {
                return PlatformUI.getWorkbench().getService(ICommandService.class)
                        .getCommand("org.eclipse.lsp4e.format").isEnabled();
            } catch (Exception e) {
                return false;
            }
        });

//		AtomicReference<Exception> ex = new AtomicReference<>();
        DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 3000, () -> {
            try {
                handlerService.executeCommand("org.eclipse.lsp4e.format", null);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
//		if (ex.get() != null) {
//			throw ex.get();
//		}
        DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 3000,
                () -> editor.getDocumentProvider().getDocument(editor.getEditorInput()).getNumberOfLines() > 1);
    }

    @Test
    public void autoCloseTags() throws Exception {
        final IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("testHTMLFile" + System.currentTimeMillis());
        project.create(null);
        project.open(null);
        final IFile file = project.getFile("autoCloseTags.html");
        file.create("<foo".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        document.replace(4, 0, ">");
        assertTrue(
                DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000,
                        () -> "<foo></foo>".equals(document.get())),
                "Autoclose not done");
    }
}
