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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestLanguageServers {

    private IProject project;

    @BeforeEach
    public void setUpProject() throws Exception {
        this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
        project.create(null);
        project.open(null);
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (IViewReference ref : activePage.getViewReferences()) {
            activePage.hideView(ref);
        }
    }

    @Test
    public void testCSSFile() throws Exception {
        final IFile file = project.getFile("blah.css");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testHTMLFile() throws Exception {
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
    public void testYAMLFile() throws Exception {
        final IFile file = project.getFile("blah.yaml");
        file.create("FAIL".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("hello: '");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testJSONFile() throws Exception {
        final IFile file = project.getFile("blah.json");
        file.create("FAIL".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("ERROR");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testJSFile() throws Exception {
        final IFile file = project.getFile("blah.js");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        DisplayHelper.sleep(2000); // Give time for LS to initialize enough before making edit and sending a
                                   // didChange
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("a<");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testTSFile() throws Exception {
        final IFile file = project.getFile("blah.ts");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 15000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testJSXFile() throws Exception {
        final IFile file = project.getFile("blah.jsx");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("a<");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testTSXFile() throws Exception {
        final IFile file = project.getFile("blah.tsx");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");

        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 15000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");

        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("const x = <></>;export default x;");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 15000, () -> {
            try {
                IMarker[] markers = file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO);
                for (IMarker m : markers) {
                    if (((String) m.getAttribute(IMarker.MESSAGE)).contains("React")) {
                        return true;
                    }
                }
                return false;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not cleared");
    }

    @Test
    public void testResourcesPathIsntTooLong() throws Exception {
        // NOTE: this test does only work with jar file; when testing
        // from IDE, the too long folder isn't excluded so test fail

        final int MAX_ALLOWED_RELATIVE_PATH = 140; // that leaves 120 characters for the path to the bundle
//C:/Users/Jean-Jacques Saint-Romain/developpement/eclipse/plugins/org.eclipse.wildwebdeveloper_1.2.3.20201212_1605/
        String location = Platform.getBundle("org.eclipse.wildwebdeveloper").getLocation();

        if (location.startsWith("initial@")) {
            location = location.substring("initial@".length());
        }
        if (location.startsWith("reference:")) {
            location = location.substring("reference:".length());
        }
        if (location.startsWith("file:")) {
            location = location.substring("file:".length());
        }
        System.out.println("Location (" + location.length() + "): " + location);
        StringBuilder maxLocation = new StringBuilder();

        // This reflects the difference between executing the test from IDE and from a
        // command line
        File file = new File(location).isAbsolute() ? new File(location)
                : new File(new File(Platform.getInstallLocation().getURL().toURI()), location);
        assertTrue(file.isDirectory());
        Map<String, Integer> tooLongPaths = new TreeMap<>();
        Path pluginPath = file.toPath();
        Files.walkFileTree(pluginPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String relativePathInsideBundle = pluginPath.relativize(dir).toString();
                if (relativePathInsideBundle.startsWith("target")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (maxLocation.length() < relativePathInsideBundle.length()) {
                    maxLocation.setLength(0);
                    maxLocation.append(relativePathInsideBundle);
                }
                if (relativePathInsideBundle.length() > MAX_ALLOWED_RELATIVE_PATH) {
                    tooLongPaths.put(relativePathInsideBundle, relativePathInsideBundle.length());
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativePathInsideBundle = pluginPath.relativize(file).toString();
                if (maxLocation.length() < relativePathInsideBundle.length()) {
                    maxLocation.setLength(0);
                    maxLocation.append(relativePathInsideBundle);
                }
                if (relativePathInsideBundle.length() > MAX_ALLOWED_RELATIVE_PATH) {
                    tooLongPaths.put(relativePathInsideBundle, relativePathInsideBundle.length());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("Max Location found (" + maxLocation.length() + "): " + maxLocation.toString());
        assertEquals(Collections.emptyMap(), tooLongPaths);
    }

    @Test
    public void testSCSSFile() throws Exception {
        final IFile file = project.getFile("blah.scss");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }

    @Test
    public void testLESSFile() throws Exception {
        final IFile file = project.getFile("blah.less");
        file.create("ERROR".getBytes(), true, false, null);
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                return false;
            }
        }), "Diagnostic not published");
    }
}
