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
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * LSP command handler called by "Bind to grammar/schema..." code lens from an
 * XML file which is not associated with a grammar. This handler:
 * 
 * <ul>
 * <li>open a dialog to select the grammar file to associate and the binding
 * type (xsi, DOCTYPE, xml-model, ...)</li>
 * <li>insert in the XML the proper syntax to associate the selected grammar
 * file / binding type by consuming the "xml.associate.grammar.insert" XML
 * language server command.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class AssociateGrammarHandler extends LSPCommandHandler {

	private static final String ASSOCIATE_GRAMMAR_INSERT = "xml.associate.grammar.insert";

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String uri = (String) command.getArguments().get(0);
		// Open the association grammar dialog.
		AssociateGrammarDialog dialog = new AssociateGrammarDialog(HandlerUtil.getActiveShell(event), path);
		if (dialog.open() == IDialogConstants.OK_ID) {
			String grammarURI = dialog.getGrammarURI();
			String bindingType = dialog.getBindingType().getCode();
			TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
			try {
				executeServerCommand(ASSOCIATE_GRAMMAR_INSERT, identifier, grammarURI, bindingType) //
						.thenAccept(result -> {
							// Insert the proper syntax for binding
							Gson gson = new Gson();
							JsonObject jsonObject = gson.toJsonTree(result).getAsJsonObject();
							TextDocumentEdit edit = gson.fromJson(jsonObject, TextDocumentEdit.class);
							WorkspaceEdit workEdits = new WorkspaceEdit();
							workEdits.setDocumentChanges(new ArrayList<>());
							workEdits.getDocumentChanges().add(Either.forLeft(edit));
							LSPEclipseUtils.applyWorkspaceEdit(workEdits, ASSOCIATE_GRAMMAR_INSERT);
						});
			} catch (Exception e) {
				ILog.get().error("Error while insert grammar association", e);
			}
		}
		return null;
	}

	private static CompletableFuture<Object> executeServerCommand(String commandId, Object... params) throws Exception {
		List<LanguageServer> commandHandlers = new ArrayList<>();
		LanguageServers.forProject(null).withCapability(serverCapabilities -> {
				ExecuteCommandOptions executeCommandProvider = serverCapabilities.getExecuteCommandProvider();
				return  Either.forLeft(executeCommandProvider != null ? 
						executeCommandProvider.getCommands().contains(commandId) : false);
			}).excludeInactive().collectAll((w, ls) -> CompletableFuture.completedFuture(ls))
			.thenAccept(commandHandlers::addAll).get(100, TimeUnit.MILLISECONDS);

		if (commandHandlers.size() == 1) {
			LanguageServer handler = commandHandlers.get(0);
			return handler.getWorkspaceService()
					.executeCommand(new ExecuteCommandParams(commandId, Arrays.asList(params)));
		} else if (commandHandlers.size() > 1) {
			throw new IllegalStateException(
					"Multiple language servers have registered to handle command '" + commandId + "'");
		}

		throw new UnsupportedOperationException(
				"No language server has registered to handle command '" + commandId + "'");
	}
}
