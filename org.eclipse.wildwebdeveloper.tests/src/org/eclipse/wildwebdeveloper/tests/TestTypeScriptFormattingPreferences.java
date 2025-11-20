/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.jsts.JSTSLanguageServer;
import org.eclipse.wildwebdeveloper.jsts.ui.preferences.JSTSLanguagePreferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
class TestTypeScriptFormattingPreferences {

	private IProject project;

	@BeforeEach
	void setUpProject() throws Exception {
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
	}

	@AfterEach
	void tearDown() {
		// Reset preference toggled by this test to avoid cross-test interference
		Activator.getDefault().getPreferenceStore().setValue(JSTSLanguagePreferences.TS.format_indentSize, 4);
	}

	@Test
	void testFormatterPrefsApplyWithoutRestart() throws Exception {
		// Arrange: create a TS file with a function declaration where spacing can change
		IFile file = project.getFile("format.ts");
		String original = "function greet() {\n  return 1\n}\n";
		file.create(original.getBytes("UTF-8"), true, false, null);

		var editor = (AbstractTextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		IDocument document = LSPEclipseUtils.getDocument(editor);

		// Give the LS time to initialize and connect to the document
		DisplayHelper.sleep(2000);

		// Acquire the running JS/TS language server for this document (filter strictly by definition id)
		LanguageServerDefinition jstsDef = LanguageServersRegistry.getInstance().getDefinition(JSTSLanguageServer.JSTS_LANGUAGE_SERVER_ID);
		var servers = new ArrayList<LanguageServer>();
		LanguageServers.forDocument(document)
				.withCapability(ServerCapabilities::getDocumentFormattingProvider)
				.collectAll((wrapper, ls) -> CompletableFuture.completedFuture(
						jstsDef.equals(wrapper.serverDefinition) ? ls : null))
				.thenAccept(servers::addAll)
				.get(5, TimeUnit.SECONDS);
		assertFalse(servers.isEmpty(), "Expected running JSTS server for document");
		LanguageServer jsts = servers.get(0);

		// Helper to request formatting and return the formatted text without mutating the editor document
		Supplier<String> formatWithCurrentPrefs = () -> {
			try {
				var opts = new FormattingOptions(4, true);
				var params = new DocumentFormattingParams(
						new TextDocumentIdentifier(LSPEclipseUtils.toUri(document).toString()), opts);
				List<? extends TextEdit> edits = jsts.getTextDocumentService().formatting(params).get(10, TimeUnit.SECONDS);
				return applyTextEdits(original, edits);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		// Act 1: format with default prefs (indentSize=4)
		String formattedIndent4 = formatWithCurrentPrefs.get();
		int indent4 = leadingSpacesOfLineStartingWith(formattedIndent4, "return 1");
		assertTrue(indent4 >= 0, "Could not locate return line in formatted output");

		// Change preference: typescript.format.indentSize = 2
		Activator.getDefault().getPreferenceStore().setValue(JSTSLanguagePreferences.TS.format_indentSize, 2);
		DisplayHelper.sleep(1500); // Allow async preference broadcast

		String formattedIndent2 = formatWithCurrentPrefs.get();
		int indent2 = leadingSpacesOfLineStartingWith(formattedIndent2, "return 1");
		assertTrue(indent2 >= 0, "Could not locate return line after indentSize=2 change");

		// Change preference again: typescript.format.indentSize = 6
		Activator.getDefault().getPreferenceStore().setValue(JSTSLanguagePreferences.TS.format_indentSize, 6);
		DisplayHelper.sleep(1500);

		String formattedIndent6 = formatWithCurrentPrefs.get();
		int indent6 = leadingSpacesOfLineStartingWith(formattedIndent6, "return 1");
		assertTrue(indent6 >= 0, "Could not locate return line after indentSize=6 change");

		// Assertions: indentation should reflect the configured indent size without server restart
		assertNotEquals(indent4, indent2, "Indent should change when indentSize changes to 2");
		assertNotEquals(indent2, indent6, "Indent should change when indentSize changes to 6");
		assertTrue(indent6 > indent2, () -> "Expected indent6(" + indent6 + ") > indent2(" + indent2 + ")");
	}

	private static String applyTextEdits(String original, List<? extends TextEdit> edits) {
		if (edits == null || edits.isEmpty()) {
			return original;
		}
		var doc = new Document(original);
		// Apply edits from bottom to top to keep offsets valid
		edits.stream().sorted(Comparator
				.comparing((TextEdit e) -> e.getRange().getStart().getLine())
				.thenComparing(e -> e.getRange().getStart().getCharacter())
				.reversed())
				.forEach(e -> replace(doc, e.getRange(), e.getNewText()));
		return doc.get();
	}

	private static void replace(IDocument doc, Range range, String newText) {
		try {
			int start = LSPEclipseUtils.toOffset(range.getStart(), doc);
			int end = LSPEclipseUtils.toOffset(range.getEnd(), doc);
			doc.replace(start, end - start, newText);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	private static int leadingSpacesOfLineStartingWith(String text, String token) {
		String[] lines = text.split("\r?\n");
		for (String line : lines) {
			int i = 0;
			while (i < line.length() && line.charAt(i) == ' ')
				i++;
			if (line.substring(i).startsWith(token)) {
				return i;
			}
		}
		return -1;
	}
}
