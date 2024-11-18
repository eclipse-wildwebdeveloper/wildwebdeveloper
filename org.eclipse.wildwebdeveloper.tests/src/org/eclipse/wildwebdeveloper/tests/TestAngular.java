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
 *   Pierre-Yves B. - Issue #238 Why does wildweb do "/bin/bash -c which node" ?
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestAngular {
    static IProject project;
    static IFolder appFolder;

    @BeforeAll
    public static void setUp() throws Exception {
        AllCleanRule.closeIntro();
        AllCleanRule.enableLogging();

        project = Utils.provisionTestProject("angular-app");
        ProcessBuilder builder = NodeJSManager.prepareNPMProcessBuilder("install", "--no-bin-links", "--ignore-scripts")
                .directory(project.getLocation().toFile());
        Process process = builder.start();
        System.out.println(builder.command().toString());
        String result = process.errorReader().lines().collect(Collectors.joining("\n"));
        System.out.println("Error Stream: >>>\n" + result + "\n<<<");

        result = process.inputReader().lines().collect(Collectors.joining("\n"));
        System.out.println("Output Stream: >>>\n" + result + "\n<<<");

        assertEquals(0, process.waitFor(), "npm install didn't complete property");

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        assertTrue(project.exists());
        appFolder = project.getFolder("src").getFolder("app");
        assertTrue(appFolder.exists());
    }

    @BeforeEach
    public void setUpTestCase() {
        AllCleanRule.enableLogging();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        new AllCleanRule().afterEach(null);
    }

    @Test
    void testAngularTs() throws Exception {
        IFile appComponentFile = appFolder.getFile("app.component.ts");
        TextEditor editor = (TextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), appComponentFile);
        DisplayHelper.sleep(4000); // Give time for LS to initialize enough before making edit and sending a
                                   // didChange
        assertTrue(DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 30000, () -> {
            try {
                return Arrays
                        .stream(appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
                                IResource.DEPTH_ZERO))
                        .anyMatch(marker -> marker.getAttribute(IMarker.LINE_NUMBER, -1) == 5
                                && marker.getAttribute(IMarker.MESSAGE, "").contains("not exist"));
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }), "Diagnostic not published in standalone component file");
        editor.close(false);
    }

    @Test
    void testAngularHtml() throws Exception {
        TextEditor editor = (TextEditor) IDE.openEditor(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                appFolder.getFile("app.componentWithHtml.ts"));
        DisplayHelper.sleep(4000); // Give time for LS to initialize enough before making edit and sending a
                                   // didChange
        IFile appComponentHTML = appFolder.getFile("app.componentWithHtml.html");
        editor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                appComponentHTML);
        // Give some time for LS to load
        DisplayHelper.sleep(2000);
        // then make an edit
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        document.set(document.get() + "\n");
        assertTrue(DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 30000, () -> {
            IMarker[] markers;
            try {
                markers = appComponentHTML.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO);
                return Arrays.stream(markers)
                        .anyMatch(marker -> marker.getAttribute(IMarker.MESSAGE, "").contains("not exist"));
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }), "No error found on erroneous HTML component file");
        // test completion
        LSContentAssistProcessor contentAssistProcessor = new LSContentAssistProcessor();
        ICompletionProposal[] proposals = contentAssistProcessor.computeCompletionProposals(Utils.getViewer(editor),
                document.get().indexOf("}}"));
        proposals[0].apply(document);
        assertEquals("<h1>{{title}}</h1>\n", document.get(), "Incorrect completion insertion");
    }
}
