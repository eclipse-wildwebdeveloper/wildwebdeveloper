/*******************************************************************************
 * Copyright (c) 2020, 2923 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alex Boyko - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4e.command.CommandExecutor;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandParams;

@SuppressWarnings("restriction")
public class XmlLanguageClientImpl extends LanguageClientImpl implements XMLLanguageClient{
		
	@Override
	public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params) {
		Command cmd = new Command();
		cmd.setCommand(params.getCommand());
		cmd.setArguments(params.getArguments());
		return CommandExecutor.executeCommandClientSide(cmd, (IDocument)null);
	}

}
