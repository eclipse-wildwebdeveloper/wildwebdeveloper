/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestAstro {
   static IProject project;
   static IFolder pagesFolder;

   @BeforeAll
   public static void setUp() throws Exception {
      AllCleanRule.closeIntro();
      AllCleanRule.enableLogging();

      project = Utils.provisionTestProject("astro-app");
      ProcessBuilder builder = NodeJSManager.prepareNPMProcessBuilder("install", "--no-bin-links", "--ignore-scripts").directory(project
         .getLocation().toFile());
      Process process = builder.start();
      System.out.println(builder.command().toString());
      String result = process.errorReader().lines().collect(Collectors.joining("\n"));
      System.out.println("Error Stream: >>>\n" + result + "\n<<<");

      result = process.inputReader().lines().collect(Collectors.joining("\n"));
      System.out.println("Output Stream: >>>\n" + result + "\n<<<");

      assertEquals(0, process.waitFor(), "npm install didn't complete property");

      project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
      assertTrue(project.exists());
      pagesFolder = project.getFolder("src").getFolder("pages");
      assertTrue(pagesFolder.exists());
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
   @SuppressWarnings("restriction")
   void testAstroPage() throws Exception {
      final var indexPageFile = project.getFile("src/pages/index.astro");
      final var indexPageEditor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
         indexPageFile);
      final var display = indexPageEditor.getSite().getShell().getDisplay();
      final var doc = indexPageEditor.getDocumentProvider().getDocument(indexPageEditor.getEditorInput());

      /*
       * ensure Astro Language Server is started and connected
       */
      final var astroLS = new AtomicReference<LanguageServerWrapper>();
      DisplayHelper.waitForCondition(display, 10_000, () -> {
         astroLS.set(LanguageServiceAccessor.getStartedWrappers(doc, null, false).stream() //
            .filter(w -> "org.eclipse.wildwebdeveloper.astro".equals(w.serverDefinition.id)) //
            .findFirst().orElse(null));
         return astroLS.get() != null //
               && astroLS.get().isActive() //
               && astroLS.get().isConnectedTo(LSPEclipseUtils.toUri(doc));
      });

      /*
       * ensure that a task marker is created for the unused node:path import statement
       */
      assertTrue(DisplayHelper.waitForCondition(display, 10_000, () -> {
         try {
            return Arrays.stream(indexPageFile.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO)) //
               .anyMatch(marker -> marker.getAttribute(IMarker.MESSAGE, "").contains("'path' is declared but its value is never read"));
         } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
         }
      }), "Diagnostic not published in standalone component file");

      /*
       * ensure "Open Declaration" works
       */
      final var baseLayoutFile = project.getFile("src/layouts/base.astro");
      final var baseLayoutPath = baseLayoutFile.getLocation().toPath();
      int offset = doc.get().indexOf("BaseLayout");

      // ensure "Open Definition" link exists
      assertTrue(DisplayHelper.waitForCondition(display, 10_000, () -> {
         try {
            final var params = LSPEclipseUtils.toTextDocumentPosistionParams(offset, doc);
            final var baseLayoutDefinitionLink = LanguageServers.forDocument(doc) //
               .withCapability(ServerCapabilities::getDefinitionProvider) //
               .collectAll(ls -> ls.getTextDocumentService().definition(LSPEclipseUtils.toDefinitionParams(params))) //
               .get(1, TimeUnit.SECONDS) //
               .stream().filter(Either::isRight) //
               .flatMap(e -> e.getRight().stream()) //
               .filter(locationLink -> Paths.get(URI.create(locationLink.getTargetUri())).equals(baseLayoutPath)) //
               .findFirst().orElse(null);
            return baseLayoutDefinitionLink != null;
         } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
         }
      }));

      // simulate pressing F3 "Open Declaration" for BaseLayout
      display.syncExec(() -> {
         try {
            indexPageEditor.selectAndReveal(offset, 0);

            final Method getSourceViewerMethod = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer"); //$NON-NLS-1$
            getSourceViewerMethod.setAccessible(true);
            final var viewer = (ITextViewer) getSourceViewerMethod.invoke(indexPageEditor);
            final var widget = viewer.getTextWidget();

            final var keyDown = new Event();
            keyDown.type = SWT.KeyDown;
            keyDown.keyCode = SWT.F3;
            keyDown.widget = widget;
            widget.notifyListeners(SWT.KeyDown, keyDown);
            final var keyUp = new Event();
            keyUp.type = SWT.KeyUp;
            keyUp.keyCode = SWT.F3;
            keyUp.widget = widget;
            widget.notifyListeners(SWT.KeyUp, keyUp);
         } catch (final Exception ex) {
            fail(ex);
         }
      });

      // ensure a new editor window was opened for "src/layouts/base.astro" file
      assertTrue(DisplayHelper.waitForCondition(display, 10_000, () -> {
         final var baseLayoutEditor = (TextEditor) PlatformUI.getWorkbench() //
            .getActiveWorkbenchWindow() //
            .getActivePage() //
            .findEditor(new FileEditorInput(baseLayoutFile));

         if (baseLayoutEditor != null) {
            baseLayoutEditor.close(false);
            return true;
         }
         return false;
      }));

      /*
       * cleanup
       */
      indexPageEditor.close(false);
   }
}
