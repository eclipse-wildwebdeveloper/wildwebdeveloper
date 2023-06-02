/*******************************************************************************
 * Copyright (c) 2019, 2023 Red Hat Inc. and others.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestTypeScript {

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
	public void testHTMLinTSXFile() throws Exception {
		IFile file = project.getFile("test.tsx");
		file.create(getClass().getResourceAsStream("/testProjects/htmlIn.tsx"), true, null);
		AbstractTextEditor editor = (AbstractTextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		IDocument document = LSPEclipseUtils.getDocument(editor);
		DisplayHelper.sleep(2000); // Give time for LS to initialize enough before making edit and sending a
									// didChange
		HoverParams params = new HoverParams(new TextDocumentIdentifier(LSPEclipseUtils.toUri(document).toString()),
				new Position(0, 18));

		List<LanguageServer> languageServers = new ArrayList<>();
		LanguageServers.forDocument(document).withCapability(ServerCapabilities::getHoverProvider)
				.collectAll((w, ls) -> CompletableFuture.completedFuture(ls)).thenAccept(languageServers::addAll)
				.get(500, TimeUnit.MILLISECONDS);
		assertFalse(languageServers.isEmpty());

		Hover hover = languageServers.get(0).getTextDocumentService().hover(params).get();
		assertTrue(hover.getContents().toString().contains("button"));
	}
}
