/*******************************************************************************
 * Copyright (c) 2023, 2024 Dawid Pakuła and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Dawid Pakuła <zulus@w3des.net> - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
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
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServiceAccessor;
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

public class TestVue {
    static IProject project;
    static IFolder componentFolder;

    @BeforeAll
    public static void setUp() throws Exception {
        AllCleanRule.closeIntro();
        AllCleanRule.enableLogging();

        project = Utils.provisionTestProject("vue-app");
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
        componentFolder = project.getFolder("src").getFolder("components");
        assertTrue(componentFolder.exists());
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
    void testVueApp() throws Exception {
        IFile appComponentFile = project.getFile("src/App.vue");
        TextEditor editor = (TextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), appComponentFile);
        LanguageServerWrapper lsWrapper = LanguageServiceAccessor.getLSWrapper(project,
                LanguageServersRegistry.getInstance().getDefinition("org.eclipse.wildwebdeveloper.vue"));

        assertTrue(DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 30000, () -> {
            try {
                return Arrays
                        .stream(appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
                                IResource.DEPTH_ZERO))
                        .anyMatch(marker -> marker.getAttribute(IMarker.MESSAGE, "").contains("never read"));
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }),
                "Diagnostic not published in standalone component file");
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        assertTrue(DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 30000,
                () -> lsWrapper.isActive() && lsWrapper.isConnectedTo(LSPEclipseUtils.toUri(document))
                        && lsWrapper.canOperate(project) && lsWrapper.canOperate(document)));
        LSContentAssistProcessor contentAssistProcessor = new LSContentAssistProcessor();
        ICompletionProposal[] proposals = contentAssistProcessor.computeCompletionProposals(Utils.getViewer(editor),
                document.get().indexOf(" }}"));
        Optional<ICompletionProposal> proposal = Arrays.stream(proposals)
                .filter(item -> item.getDisplayString().equals("appParameter")).findFirst();

        assertTrue(proposal.isPresent(), "Proposal not exists");
        proposal.get().apply(document);

        assertTrue(document.get().contains("{{ this.appParameter }}"), "Incorrect completion insertion");

        editor.close(false);
    }

    @Test
    void testVueTemplate() throws Exception {
        TextEditor editor = (TextEditor) IDE.openEditor(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                componentFolder.getFile("HelloWorld.vue"));
        LanguageServerWrapper lsWrapper = LanguageServiceAccessor.getLSWrapper(project,
                LanguageServersRegistry.getInstance().getDefinition("org.eclipse.wildwebdeveloper.vue"));
        IFile appComponentHTML = componentFolder.getFile("HelloWorld.vue");
        editor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                appComponentHTML);
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        String tagName = "only-start";
        String tag = '<' + tagName + '>';
        document.set(document.get().replace(tag, tag + "<"));
        assertTrue(DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 30000, () -> {
            IMarker[] markers;
            try {
                markers = appComponentHTML.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO);
                return Arrays.stream(markers).anyMatch(
                        marker -> marker.getAttribute(IMarker.MESSAGE, "").contains("Element is missing end tag."));
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }),
                "No error found on erroneous HTML component file");

        assertTrue(DisplayHelper.waitForCondition(editor.getSite().getShell().getDisplay(), 30000,
                () -> lsWrapper.isActive() && lsWrapper.isConnectedTo(LSPEclipseUtils.toUri(document))
                        && lsWrapper.canOperate(project) && lsWrapper.canOperate(document)));

        LSContentAssistProcessor contentAssistProcessor = new LSContentAssistProcessor();

        int pos = document.get().indexOf(tag) + tag.length();
        ICompletionProposal[] proposals = contentAssistProcessor.computeCompletionProposals(Utils.getViewer(editor),
                pos + 1);

        // Find closing tag proposal
        ICompletionProposal closingTagProposal = Arrays.stream(proposals)
                .filter(p -> p.getDisplayString().equals('/' + tagName)).findFirst().orElse(null);
        assertNotNull(closingTagProposal, "Closing tag proposal not found for '" + tag + "'");

        closingTagProposal.apply(document);
        assertEquals(new String(componentFolder.getFile("HelloWorldCorrect.vue").getContents().readAllBytes()).trim(),
                document.get().trim(), "Incorrect completion insertion");

        editor.close(false);
    }
}
