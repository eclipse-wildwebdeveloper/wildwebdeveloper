/*******************************************************************************
 * Copyright (c) 2022 2023 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands.associate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * LSP command handler called by "Bind to grammar/schema..." code lens from an
 * XML file which is not associated with a grammar. This handler:
 *
 * <ul>
 * <li>Opens a dialog immediately (no blocking server call).</li>
 * <li>The dialog fetches bindable catalog entries asynchronously when it opens,
 * using the supplier provided by this handler. Each dialog open triggers a fresh
 * fetch so catalog changes are always reflected.</li>
 * <li>Inserts the proper syntax into the XML document by calling
 * {@code xml.associate.grammar.insert}.</li>
 * </ul>
 *
 * @author Angelo ZERR
 */
public class AssociateGrammarHandler extends LSPCommandHandler {

	private static final String ASSOCIATE_GRAMMAR_INSERT = "xml.associate.grammar.insert"; //$NON-NLS-1$
	private static final String LIST_BINDABLE_CATALOG_ENTRIES = "xml.catalog.entries.bindable"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String uri = (String) command.getArguments().get(0);
		TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);

		// Open the dialog immediately; it will call the supplier to start the fetch.
		// Each dialog instance fetches fresh entries so stale cache is never an issue.
		AssociateGrammarDialog dialog = new AssociateGrammarDialog(
				HandlerUtil.getActiveShell(event), path, this::catalogEntriesFuture);
		if (dialog.open() != IDialogConstants.OK_ID) {
			return null;
		}

		CompletableFuture<Object> future;
		if (dialog.isCatalogMode()) {
			BindableCatalogEntry entry = dialog.getSelectedCatalogEntry();
			future = executeServerCommand(ASSOCIATE_GRAMMAR_INSERT,
					identifier,
					entry.entryType,
					"catalog", //$NON-NLS-1$
					entry.identifier,
					entry.targetURI,
					entry.uriValue);
		} else {
			future = executeServerCommand(ASSOCIATE_GRAMMAR_INSERT,
					identifier, dialog.getGrammarURI(), dialog.getBindingType().getCode());
		}
		future.thenAccept(AssociateGrammarHandler::applyEdit)
				.exceptionally(e -> {
					ILog.get().error("Error while inserting grammar association", e); //$NON-NLS-1$
					return null;
				});
		return null;
	}

	/** Fetches bindable catalog entries from the server. No caching here — each dialog open gets fresh data. */
	private CompletableFuture<List<BindableCatalogEntry>> catalogEntriesFuture() {
		return executeServerCommand(LIST_BINDABLE_CATALOG_ENTRIES)
				.thenApply(result -> {
					if (result == null) {
						return Collections.<BindableCatalogEntry>emptyList();
					}
					Gson gson = new Gson();
					JsonArray array = gson.toJsonTree(result).getAsJsonArray();
					List<BindableCatalogEntry> entries = new ArrayList<>(array.size());
					for (int i = 0; i < array.size(); i++) {
						entries.add(gson.fromJson(array.get(i), BindableCatalogEntry.class));
					}
					return entries;
				});
	}

	private static void applyEdit(Object result) {
		Gson gson = new Gson();
		JsonObject jsonObject = gson.toJsonTree(result).getAsJsonObject();
		TextDocumentEdit edit = gson.fromJson(jsonObject, TextDocumentEdit.class);
		WorkspaceEdit workEdits = new WorkspaceEdit();
		workEdits.setDocumentChanges(new ArrayList<>());
		workEdits.getDocumentChanges().add(Either.forLeft(edit));
		LSPEclipseUtils.applyWorkspaceEdit(workEdits, ASSOCIATE_GRAMMAR_INSERT);
	}

	private static CompletableFuture<Object> executeServerCommand(String commandId, Object... params) {
		return LanguageServers.forProject(null)
				.withCapability(serverCapabilities -> {
					ExecuteCommandOptions executeCommandProvider = serverCapabilities.getExecuteCommandProvider();
					return Either.forLeft(executeCommandProvider != null
							? executeCommandProvider.getCommands().contains(commandId)
							: false);
				})
				.excludeInactive()
				.collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
				.thenCompose(commandHandlers -> {
					if (commandHandlers.size() == 1) {
						return commandHandlers.get(0).getWorkspaceService()
								.executeCommand(new ExecuteCommandParams(commandId, Arrays.asList(params)));
					}
					if (commandHandlers.size() > 1) {
						return CompletableFuture.failedFuture(new IllegalStateException(
								"Multiple language servers registered for command '" + commandId + "'")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					return CompletableFuture.failedFuture(new UnsupportedOperationException(
							"No language server registered for command '" + commandId + "'")); //$NON-NLS-1$ //$NON-NLS-2$
				});
	}
}
