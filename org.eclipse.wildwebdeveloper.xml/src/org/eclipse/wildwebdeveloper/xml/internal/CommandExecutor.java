package org.eclipse.wildwebdeveloper.xml.internal;

import static org.eclipse.lsp4e.command.LSPCommandHandler.LSP_COMMAND_PARAMETER_ID;
import static org.eclipse.lsp4e.command.LSPCommandHandler.LSP_PATH_PARAMETER_ID;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ITypedParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValuesException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

@SuppressWarnings("restriction")
class CommandExecutor {
	
	private static final String LSP_COMMAND_CATEGORY_ID = "org.eclipse.lsp4e.commandCategory"; //$NON-NLS-1$
	private static final String LSP_COMMAND_PARAMETER_TYPE_ID = "org.eclipse.lsp4e.commandParameterType"; //$NON-NLS-1$
	private static final String LSP_PATH_PARAMETER_TYPE_ID = "org.eclipse.lsp4e.pathParameterType"; //$NON-NLS-1$

	public CompletableFuture<Object> executeClientCommand(String id, Object... params) {
		List<LanguageServer> commandHandlers = LanguageServiceAccessor.getActiveLanguageServers(handlesCommand(id));
		if (commandHandlers != null && !commandHandlers.isEmpty()) {
			if (commandHandlers.size() == 1) {
				LanguageServer handler = commandHandlers.get(0);
				return handler
						.getWorkspaceService()
						.executeCommand(new ExecuteCommandParams(id, Arrays.asList(params)));
			} else if (commandHandlers.size() > 1) {
				throw new IllegalStateException("Multiple language servers have registered to handle command '"+id+"'");
			}
		} else {
			return executeCommandClientSide(new Command("Java LS command", id, Arrays.asList(params)));
		}
		return null;
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
	
	@SuppressWarnings("unused") // ECJ compiler for some reason thinks handlerService == null is always false
	private static CompletableFuture<Object> executeCommandClientSide(Command command) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return null;
		}
		IPath context = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		ParameterizedCommand parameterizedCommand = createEclipseCoreCommand(command, context, workbench);
		if (parameterizedCommand == null) {
			return null;
		}
		IHandlerService handlerService = workbench.getService(IHandlerService.class);
		if (handlerService == null) {
			return null;
		}
		return CompletableFuture.supplyAsync(() -> {
			try {
				return handlerService.executeCommand(parameterizedCommand, null);
			} catch (ExecutionException | NotDefinedException e) {
				Activator.getDefault().getLog().log(
						new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
				return null;
			} catch (NotEnabledException | NotHandledException e2) {
				return null;
			}
		});
	}

	private static ParameterizedCommand createEclipseCoreCommand(Command command, IPath context,
			IWorkbench workbench) {
		// Usually commands are defined via extension point, but we synthesize one on
		// the fly for the command ID, since we do not want downstream users
		// having to define them.
		String commandId = command.getCommand();
		ICommandService commandService = workbench.getService(ICommandService.class);
		org.eclipse.core.commands.Command coreCommand = commandService.getCommand(commandId);
		if (!coreCommand.isDefined()) {
			ParameterType commandParamType = commandService.getParameterType(LSP_COMMAND_PARAMETER_TYPE_ID);
			ParameterType pathParamType = commandService.getParameterType(LSP_PATH_PARAMETER_TYPE_ID);
			Category category = commandService.getCategory(LSP_COMMAND_CATEGORY_ID);
			IParameter[] parameters = {
					new CommandEventParameter(commandParamType, command.getTitle(), LSP_COMMAND_PARAMETER_ID),
					new CommandEventParameter(pathParamType, command.getTitle(), LSP_PATH_PARAMETER_ID)};
			coreCommand.define(commandId, null, category, parameters);
		}

		Map<Object, Object> parameters = new HashMap<>();
		parameters.put(LSP_COMMAND_PARAMETER_ID, command);
		parameters.put(LSP_PATH_PARAMETER_ID, context);
		ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(coreCommand, parameters);
		return parameterizedCommand;
	}


	static class CommandEventParameter implements IParameter, ITypedParameter {

		private final ParameterType paramType;
		private final String name;
		private String id;

		public CommandEventParameter(ParameterType paramType, String name, String id) {
			super();
			this.paramType = paramType;
			this.name = name;
			this.id = id;
		}

		@Override
		public ParameterType getParameterType() {
			return paramType;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public IParameterValues getValues() throws ParameterValuesException {
			return () -> Collections.emptyMap();
		}

		@Override
		public boolean isOptional() {
			return false;
		}

	}

}
