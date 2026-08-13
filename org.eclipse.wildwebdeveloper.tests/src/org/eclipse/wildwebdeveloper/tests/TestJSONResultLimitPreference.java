/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Lars Vogel (Vogella GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.json.JSonLanguageServer;
import org.eclipse.wildwebdeveloper.json.ui.preferences.JSonPreferenceServerConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
class TestJSONResultLimitPreference {

	private static final int KEYS = 60;
	private static final int REDUCED_LIMIT = 5;

	private IProject project;

	@BeforeEach
	void setUpProject() throws Exception {
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
	}

	@AfterEach
	void resetPreference() {
		Activator.getDefault().getPreferenceStore().setValue(
				JSonPreferenceServerConstants.JSON_PREFERENCES_MAXITEMSCOMPUTED,
				JSonPreferenceServerConstants.MAXITEMSCOMPUTED_DEFAULT);
	}

	@Test
	void testResultLimitPreferenceAppliesWithoutRestart() throws Exception {
		StringBuilder content = new StringBuilder("{\n");
		for (int i = 0; i < KEYS; i++) {
			content.append("  \"key_").append(i).append("\": ").append(i).append(i == KEYS - 1 ? "\n" : ",\n");
		}
		content.append("}\n");

		IFile file = project.getFile("limit.json");
		file.create(content.toString().getBytes(StandardCharsets.UTF_8), true, false, null);

		AbstractTextEditor editor = (AbstractTextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		IDocument document = LSPEclipseUtils.getDocument(editor);
		DisplayHelper.sleep(2000);

		assertEquals(KEYS, countSymbols(document), "Default limit should not truncate this document");

		Activator.getDefault().getPreferenceStore()
				.setValue(JSonPreferenceServerConstants.JSON_PREFERENCES_MAXITEMSCOMPUTED, REDUCED_LIMIT);
		DisplayHelper.sleep(1500); // allow the asynchronous preference broadcast

		// exact, so that an empty result does not silently satisfy the assertion
		assertEquals(REDUCED_LIMIT, countSymbols(document),
				"Lowering the limit should truncate the outline to exactly that many symbols");
	}

	private static int countSymbols(IDocument document) throws Exception {
		DocumentSymbolParams params = new DocumentSymbolParams(
				new TextDocumentIdentifier(LSPEclipseUtils.toUri(document).toString()));
		return jsonLanguageServer(document).getTextDocumentService().documentSymbol(params).get(10, TimeUnit.SECONDS)
				.size();
	}

	private static LanguageServer jsonLanguageServer(IDocument document) throws Exception {
		return LanguageServers.forDocument(document) //
				.withCapability(ServerCapabilities::getDocumentSymbolProvider) //
				.collectAll((wrapper, ls) -> CompletableFuture.completedFuture( //
						LanguageServersRegistry.getInstance()
								.getDefinition(JSonLanguageServer.JSON_LANGUAGE_SERVER_ID)
								.equals(wrapper.serverDefinition) ? ls : null)) //
				.get(10, TimeUnit.SECONDS).stream() //
				.filter(Objects::nonNull) //
				.findFirst() //
				.orElseThrow(() -> new AssertionError("Expected a running JSON language server for the document"));
	}
}
