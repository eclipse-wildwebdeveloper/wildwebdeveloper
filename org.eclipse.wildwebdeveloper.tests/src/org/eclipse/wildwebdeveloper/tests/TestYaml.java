/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestYaml {

    @Test
    public void testFalseDetectionAsKubernetes() throws Exception {
        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("p");
        p.create(new NullProgressMonitor());
        p.open(new NullProgressMonitor());
        IFile file = p.getFile("blah.yaml");
        file.create(new byte[0], true, false, new NullProgressMonitor());
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ITextEditor editor = (ITextEditor) IDE.openEditor(activePage, file, true);
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        document.set("name: a\ndescrition: b");
        boolean markerFound = DisplayHelper.waitForCondition(activePage.getWorkbenchWindow().getShell().getDisplay(),
                3000, () -> {
                    try {
                        return file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO).length > 0;
                    } catch (CoreException e) {
                        return false;
                    }
                });
        assertFalse(markerFound, Arrays.stream(file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO))
                .map(Object::toString).collect(Collectors.joining("\n")));
    }

    private void testErrorFile(String name) throws Exception {
        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("p");
        p.create(new NullProgressMonitor());
        p.open(new NullProgressMonitor());
        IFile file = p.getFile(name);
        file.create(new byte[0], true, false, new NullProgressMonitor());
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ITextEditor editor = (ITextEditor) IDE.openEditor(activePage, file, true);
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        document.set("{version: 1}");
        boolean markerFound = DisplayHelper.waitForCondition(activePage.getWorkbenchWindow().getShell().getDisplay(),
                6000, () -> {
                    try {
                        return file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO).length > 0;
                    } catch (CoreException e) {
                        return false;
                    }

                });
        assertTrue(markerFound, Arrays.stream(file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO))
                .map(Object::toString).collect(Collectors.joining("\n")));
    }

    @Test
    public void testSchemaExtensionPoint() throws Exception {
        testErrorFile("dep.yml");
    }

    @Test
    public void testSchemaExtensionPointUsingPlatformURL() throws Exception {
        testErrorFile("depp.yml");
    }
}
