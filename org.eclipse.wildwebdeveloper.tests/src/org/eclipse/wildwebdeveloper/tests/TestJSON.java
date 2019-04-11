/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.Command;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Rule;
import org.junit.Test;

public class TestJSON {

	@Rule public AllCleanRule rule = new AllCleanRule();

	@Test
	public void testFormatEnabled() throws IOException, PartInitException, CoreException {
		File file = File.createTempFile("test", ".json");
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ITextEditor editor = (ITextEditor) IDE
				.openEditorOnFileStore(activePage, EFS.getStore(file.toURI()));
		ICommandService service = activePage.getWorkbenchWindow().getService(ICommandService.class);
		Command formatCommand = service.getCommand("org.eclipse.lsp4e.format");
		assertNotNull("Format command not found", formatCommand);
		assertTrue("Format command not defined", formatCommand.isDefined());
		assertTrue("Format command not enabled", formatCommand.isEnabled());
		assertTrue("Format command not handled", formatCommand.isHandled());
	}

}
