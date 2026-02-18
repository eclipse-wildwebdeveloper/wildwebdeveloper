/*******************************************************************************
 * Copyright (c) 2018, 2026 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestJsTs {

	private IProject project;

	@BeforeEach
	public void setUpProject() throws CoreException {
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	public void testRefactoringRenameInTypeScript() throws Exception {
		final IFile file = project.getFile("TestJsTs.ts");
		String content = "function testVar(test) {\n	if (\"truetrue\" == \"true\" + test ) {\n"
				+ "		return true;\n	}\n	return false;\n}\n"
				+ "print(\"Testing var with true argument == \" + testVar(true));\n"
				+ "print(\"Testing var with false argument == \" + testVar(false));\n";
		final String oldName = "testVar";
		final String newName = "newName";
		internalTestRename(file, content, oldName, newName);
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	public void testRefactoringRenameInJavaScript() throws Exception {
		final IFile file = project.getFile("TestJsTs.js");
		String content = "function testVar(test) {\n	if (\"truetrue\" == \"true\" + test ) {\n"
				+ "		return true;\n	}\n	return false;\n}\n"
				+ "print(\"Testing var with true argument == \" + testVar(true));\n"
				+ "print(\"Testing var with false argument == \" + testVar(false));\n";
		final String oldName = "testVar";
		final String newName = "newName";
		internalTestRename(file, content, oldName, newName);
	}

	private void internalTestRename(IFile file, String content, String oldName, String newName) throws Exception {
		String expectedContent = content.replace(oldName, newName);

		int offset = content.indexOf(oldName);
		file.create(content.getBytes(), true, false, null);

		AbstractTextEditor editor = (AbstractTextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");

		editor.getSelectionProvider().setSelection(new TextSelection(offset, 0));
		editor.setFocus();

		Display display = editor.getSite().getShell().getDisplay();

		// Give LS time to initialize
		DisplayHelper.sleep(3000);

		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		Command command = commandService.getCommand(IWorkbenchCommandConstants.FILE_RENAME);
		assertTrue(command.isEnabled() && command.isHandled());

		Event event = new Event();
		event.widget = editor.getAdapter(Control.class);
		event.display = display;

		ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, event);
		command.executeWithChecks(executionEvent);

		assertTrue(
				DisplayHelper.waitForCondition(display, 5000, () -> LinkedModeModel.getModel(document, offset) != null),
				"Linked rename mode did not start");

		display.asyncExec(() -> {
			try {
				document.replace(offset, oldName.length(), newName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		assertTrue(DisplayHelper.waitForCondition(display, 5000, () -> expectedContent.equals(document.get())),
				"Rename not applied to document");
	}

}
