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
 *   Pierre-Yves B. - Issue #238 Why does wildweb do "/bin/bash -c which node" ?
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestAngular {

	@Test
	public void testAngular() throws Exception {
		IProject project = Utils.provisionTestProject("angular-app");
		Process process = new ProcessBuilder(getNpmLocation(), "install", "--no-bin-links", "--ignore-scripts")
				.directory(project.getLocation().toFile()).start();
		assertEquals(0, process.waitFor(), "npm install didn't complete property");
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		IFolder appFolder = project.getFolder("src").getFolder("app");

		IFile appComponentFile = appFolder.getFile("app.component.ts");
		TextEditor editor = (TextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), appComponentFile);
		DisplayHelper.sleep(4000); // Give time for LS to initialize enough before making edit and sending a
									// didChange
		// make an edit
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		document.set(document.get() + "\n");
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return Arrays
							.stream(appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
									IResource.DEPTH_ZERO))
							.anyMatch(marker -> marker.getAttribute(IMarker.LINE_NUMBER, -1) == 5
									&& marker.getAttribute(IMarker.MESSAGE, "").contains("template"));
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 50000),
				"Diagnostic not published in standalone component file");
		editor.close(false);

		editor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				appFolder.getFile("app.componentWithHtml.ts"));
		DisplayHelper.sleep(4000); // Give time for LS to initialize enough before making edit and sending a
									// didChange
		IFile appComponentHTML = appFolder.getFile("app.componentWithHtml.html");
		editor = (TextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				appComponentHTML);
		document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				IMarker[] markers;
				try {
					markers = appComponentHTML.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO);
					return Arrays.stream(markers)
							.anyMatch(marker -> marker.getAttribute(IMarker.MESSAGE, "").contains("template"));
				} catch (CoreException e) {
					return false;
				}
			}
		}.waitForCondition(editor.getSite().getShell().getDisplay(), 30000),
				"No error found on erroneous HTML component file");
		// test completion
		LSContentAssistProcessor contentAssistProcessor = new LSContentAssistProcessor();
		ICompletionProposal[] proposals = contentAssistProcessor.computeCompletionProposals(Utils.getViewer(editor),
				document.get().indexOf("}}"));
		proposals[0].apply(document);
		assertEquals("<h1>{{title}}</h1>", document.get(), "Incorrect completion insertion");
	}

	public static String getNpmLocation() {
		String res = "/path/to/npm";
		String[] command = new String[] { "/bin/bash", "-c", "-l", "which npm" };
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where npm" };
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			res = reader.readLine();
		} catch (IOException e) {
			return Platform.getOS().equals(Platform.OS_WIN32) ? "npm.cmd" : "npm";
		}

		// Try default install path as last resort
		if (res == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/usr/local/bin/npm";
		} else if (res == null && Platform.getOS().equals(Platform.OS_LINUX)) {
			res = "/usr/bin/npm";
		}

		if (res != null && Files.exists(Paths.get(res))) {
			return res;
		}
		return Platform.getOS().equals(Platform.OS_WIN32) ? "npm.cmd" : "npm";
	}

}
