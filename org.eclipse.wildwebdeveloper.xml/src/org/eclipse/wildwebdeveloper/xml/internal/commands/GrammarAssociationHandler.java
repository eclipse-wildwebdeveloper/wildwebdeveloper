package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.command.LSPCommandHandler;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GrammarAssociationHandler extends LSPCommandHandler {

	private static final String ASSOCIATE_GRAMMAR_INSERT = "xml.associate.grammar.insert";

	@Override
	public Object execute(ExecutionEvent event, Command command, IPath path) throws ExecutionException {
		String uri = (String) command.getArguments().get(0);
		IContainer container = null;
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		if (file != null && file.exists()) {
			container = file.getProject();
		} else {
			container = ResourcesPlugin.getWorkspace().getRoot();
		}
		AssociateGrammarDialog dialog = new AssociateGrammarDialog(HandlerUtil.getActiveShell(event), path, container);
		if (dialog.open() == IDialogConstants.OK_ID) {
			String grammarURI = dialog.getGrammarURI();
			String bindingType = dialog.getBindingType().getCode();
			TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
			try {
				executeClientCommand(ASSOCIATE_GRAMMAR_INSERT, identifier, grammarURI, bindingType) //
						.thenAccept(result -> {
							// Insert the proper syntax for binding
							Gson gson = new Gson(); // TODO? retrieve the GSon used by LS
							JsonObject jsonObject = gson.toJsonTree(result).getAsJsonObject();
							TextDocumentEdit edit = gson.fromJson(jsonObject, TextDocumentEdit.class);
							WorkspaceEdit workEdits = new WorkspaceEdit();
							workEdits.setDocumentChanges(new ArrayList<>());
							workEdits.getDocumentChanges().add(Either.forLeft(edit));
							LSPEclipseUtils.applyWorkspaceEdit(workEdits, ASSOCIATE_GRAMMAR_INSERT);
						});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

	public CompletableFuture<Object> executeClientCommand(String id, Object... params) throws Exception {
		List<LanguageServer> commandHandlers = LanguageServiceAccessor.getActiveLanguageServers(handlesCommand(id));
		if (commandHandlers != null) {
			if (commandHandlers.size() == 1) {
				LanguageServer handler = commandHandlers.get(0);
				return handler.getWorkspaceService()
						.executeCommand(new ExecuteCommandParams(id, Arrays.asList(params)));
				// .get(2, TimeUnit.SECONDS);
			} else if (commandHandlers.size() > 1) {
				throw new IllegalStateException(
						"Multiple language servers have registered to handle command '" + id + "'");
			}
		}
		throw new UnsupportedOperationException("No language server has registered to handle command '" + id + "'");
	}

	private Predicate<ServerCapabilities> handlesCommand(String id) {
		return (serverCaps) -> {
			ExecuteCommandOptions executeCommandProvider = serverCaps.getExecuteCommandProvider();
			if (executeCommandProvider != null) {
				return executeCommandProvider.getCommands().contains(id);
			}
			return false;
		};
	}

}
