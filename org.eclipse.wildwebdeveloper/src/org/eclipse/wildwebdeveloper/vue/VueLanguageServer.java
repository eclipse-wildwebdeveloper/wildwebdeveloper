/*******************************************************************************
 * Copyright (c) 2023 Dawid Pakuła and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Dawid Pakuła <zulus@w3des.net> - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.eclipse.wildwebdeveloper.jsts.JSTSLanguageServerAPI;
import org.eclipse.wildwebdeveloper.jsts.request.ExecuteInfo;

public class VueLanguageServer extends ProcessStreamConnectionProvider {
	private static String vuePath = null;
	private static String TS_REQUEST = "tsserver/request"; 

	public VueLanguageServer() {

		List<String> commands = new ArrayList<>();
		commands.add(NodeJSManager.getNodeJsLocation().getAbsolutePath());
		try {
			if (vuePath == null) {
				resolvePaths();
			}
			commands.add(vuePath);
			commands.add("--stdio");
			setCommands(commands);
			//setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	private void resolvePaths() throws IOException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/node_modules/@vue/language-server/bin/vue-language-server.js"));
		vuePath = new File(url.getPath()).getAbsolutePath();

	}
	

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = super.createProcessBuilder();
		builder.environment().put("VUE_NONPOLLING_WATCHER", Boolean.toString(true));
		builder.environment().put("NODE_ENV", "production");
		return builder;
	}

	@Override
	public String toString() {
		return "VUE Language Server: " + super.toString();
	}

	@Override
	public void handleMessage(Message message, LanguageServer languageServer, URI rootURI) {
		if (message instanceof NotificationMessage) {
			NotificationMessage msg = (NotificationMessage) message;
			if (msg.getMethod().equals(TS_REQUEST)) {
				forwardTS((Object[]) msg.getParams(), (VueLanguageServerAPI) languageServer, rootURI);
			}
		}
		super.handleMessage(message, languageServer, rootURI);
	}

	@SuppressWarnings("restriction")
	private void forwardTS(Object[] params, VueLanguageServerAPI languageServer, URI rootURI) {
		Object requestId = params[0];
		String commandId = (String) params[1];
		Object args = params.length > 2 ? params[2] : null;

		LanguageServers.forProject(LSPEclipseUtils.findResourceFor(rootURI).getProject())
				.collectAll((w, ls) -> CompletableFuture.completedFuture(ls)).thenAccept((lss) -> {
					lss.stream().filter(JSTSLanguageServerAPI.class::isInstance).map(JSTSLanguageServerAPI.class::cast)
							.findAny().ifPresent(jsts -> {
								jsts.getWorkspaceService()
										.executeCommand(new ExecuteCommandParams(JSTSLanguageServerAPI.TS_REQUEST_COMMAND,
												Arrays.asList(new Object[] { commandId, args, new ExecuteInfo() })))
										.whenComplete((result, e) -> {
											Object body = null;
											if (result instanceof Map) {
												body = ((Map<?, ?>)result).get("body");
											}
											languageServer.tsserverResponse(new Object[] {requestId, body});
										});
							});
				});
	}

}
