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
*   Andrew Obuchowicz (Red Hat Inc.) - initial implementation
*******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestESLint {

    private static IProject project;

    @BeforeAll
    public static void setUpProject() throws Exception {
        AllCleanRule.closeIntro();
        AllCleanRule.enableLogging();

        String projectName = TestESLint.class.getName() + System.nanoTime();
        IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
        IPath projectLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(projectName);
        desc.setLocation(projectLocation);
        File projectDirectory = projectLocation.toFile();
        projectDirectory.mkdir();
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/.eslintrc")) {
            Files.copy(eslintRc, new File(projectDirectory, ".eslintrc").toPath());
        }
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/tsconfig.json")) {
            Files.copy(eslintRc, new File(projectDirectory, "tsconfig.json").toPath());
        }
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/package.json")) {
            Files.copy(eslintRc, new File(projectDirectory, "package.json").toPath());
        }
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/ESLintProj.js")) {
            Files.copy(eslintRc, new File(projectDirectory, "ESLintProj.js").toPath());
        }
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/ESLintProj.js")) {
            Files.copy(eslintRc, new File(projectDirectory, "ESLintProj.jsx").toPath());
        }
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/ESLintProj.js")) {
            Files.copy(eslintRc, new File(projectDirectory, "ESLintProj.ts").toPath());
        }
        try (InputStream eslintRc = TestESLint.class.getResourceAsStream("/testProjects/eslint/ESLintProj.js")) {
            Files.copy(eslintRc, new File(projectDirectory, "ESLintProj.tsx").toPath());
        }
        ProcessBuilder builder = NodeJSManager.prepareNPMProcessBuilder("install", "--no-bin-links", "--ignore-scripts")
                .directory(projectDirectory);
        Process dependencyInstaller = builder.start();
        System.out.println(builder.command().toString());
        String result = dependencyInstaller.errorReader().lines().collect(Collectors.joining("\n"));
        System.out.println("Error Stream: >>>\n" + result + "\n<<<");

        result = dependencyInstaller.inputReader().lines().collect(Collectors.joining("\n"));
        System.out.println("Output Stream: >>>\n" + result + "\n<<<");

        assertEquals(0, dependencyInstaller.waitFor(), "npm install didn't complete properly");
        project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        project.create(desc, null);
        project.open(null);
    }

    @BeforeEach
    public void setUpTestCase() {
        AllCleanRule.enableLogging();
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        new AllCleanRule().afterEach(null);
    }

    @Test
    void testESLintDiagnosticsInJS() throws Exception {
        IFile file = project.getFile("ESLintProj.js");
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        assertESLintIndentMarkerExists(file);
    }

    @Test
    void testESLintDiagnosticsInTS() throws Exception {
        IFile file = project.getFile("ESLintProj.ts");
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        assertESLintIndentMarkerExists(file);
    }

    @Test
    void testESLintDiagnosticsInJSX() throws Exception {
        IFile file = project.getFile("ESLintProj.jsx");
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        assertESLintIndentMarkerExists(file);
    }

    @Test
    void testESLintDiagnosticsInTSX() throws Exception {
        IFile file = project.getFile("ESLintProj.tsx");
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        assertESLintIndentMarkerExists(file);
    }

    private void assertESLintIndentMarkerExists(IFile file) throws PartInitException {
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 10000, () -> {
            try {
                return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }), "Diagnostic not published");

        assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
            try {
                return Arrays.asList(file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO))
                        .stream().filter(Objects::nonNull)
                        .anyMatch(m -> m.getAttribute(IMarker.MESSAGE, null).toLowerCase().contains("indentation"));
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }), "Diagnostic content is incorrect");
    }
}
