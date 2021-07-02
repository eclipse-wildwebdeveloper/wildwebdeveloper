/*******************************************************************************
 * Copyright (c) 2018, 2019 Red Hat Inc. and others.
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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	public void testRefactoringRename() throws Exception {
		final IFile file = project.getFile("TestJsTs.js");
		String content = "function testVar(test) {\n	if (\"truetrue\" == \"true\" + test ) {\n"
				+ "		return true;\n	}\n	return false;\n}\n"
				+ "print(\"Testing var with true argument == \" + testVar(true));\n"
				+ "print(\"Testing var with false argument == \" + testVar(false));\n";
		final String oldName = "testVar";
		final String newName = "newName";
		String newContent = content.replaceAll(oldName, newName);

		int offset = content.indexOf(oldName);
		file.create(new ByteArrayInputStream(content.getBytes()), true, null);
		AbstractTextEditor editor = (AbstractTextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		editor.getSelectionProvider().setSelection(new TextSelection(offset, 0));
		editor.setFocus();
		DisplayHelper.sleep(2000); // Give time for LS to initialize enough before making edit and sending a
									// didChange
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		Command command = commandService.getCommand(IWorkbenchCommandConstants.FILE_RENAME);
		assertTrue(command.isEnabled() && command.isHandled());
		Event e = new Event();
		e.widget = editor.getAdapter(Control.class);
		Shell ideShell = editor.getSite().getShell();
		Display display = ideShell.getDisplay();
		e.display = display;
		AtomicBoolean renameDialogOkPressed = new AtomicBoolean();
		AtomicBoolean renameDialogContinuePressed = new AtomicBoolean();
		AtomicBoolean renameDialogCancelPressed = new AtomicBoolean();
		AtomicBoolean errorDialogOkPressed = new AtomicBoolean();
		Listener pressOKonRenameDialogPaint = event -> {
			if (event.widget instanceof Composite) {
				Composite c = (Composite) event.widget;
				Shell shell = c.getShell();
				if (shell != ideShell) {
					if ("Rename".equals(shell.getText())) {
						if (!renameDialogOkPressed.get()) {
							if (hasButton(c, "OK")) {
								event.widget.getDisplay().asyncExec(() -> pressOk(shell));
								renameDialogOkPressed.set(true);
							}
						} else if (!renameDialogContinuePressed.get()) {
							if (hasButton(c, "Con&tinue")) {
								event.widget.getDisplay().asyncExec(() -> pressOk(shell));
								renameDialogContinuePressed.set(true);
							} else if (!renameDialogCancelPressed.get() && hasButton(c, "Cancel")
									&& hasButton(c, "< &Back")) {
								event.widget.getDisplay().asyncExec(() -> pressCancel(shell));
								renameDialogCancelPressed.set(true);
							}
						}
					} else if ("Refactoring".equals(shell.getText())) {
						if (!errorDialogOkPressed.get()) {
							if (hasButton(c, "OK")) {
								event.widget.getDisplay().asyncExec(() -> pressOk(shell));
								errorDialogOkPressed.set(true);
							}
						}
					}
				}
			}
		};
		try {
			display.addFilter(SWT.Paint, pressOKonRenameDialogPaint);
			ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, e);
			command.executeWithChecks(executionEvent);
			assertTrue(new DisplayHelper() {
				@Override
				protected boolean condition() {
					return renameDialogOkPressed.get();
				}
			}.waitForCondition(display, 2000), "Rename dialog not shown");

			assertTrue(new DisplayHelper() {
				@Override
				protected boolean condition() {
					return newContent.equals(document.get());
				}
			}.waitForCondition(display, 5000), "document not modified, rename not applied");
		} finally {
			ideShell.getDisplay().removeFilter(SWT.Paint, pressOKonRenameDialogPaint);
		}
	}

	private boolean hasButton(Widget w, String requiredText) {
		if (w instanceof Button) {
			Button b = (Button) w;
			return requiredText.equals(b.getText());
		}
		if (w instanceof Composite) {
			for (Control child : ((Composite) w).getChildren()) {
				if (hasButton(child, requiredText)) {
					return true;
				}
			}
		}
		return false;
	}

	private void pressOk(Shell dialogShell) {
		try {
			Dialog dialog = (Dialog) dialogShell.getData();
			Method okPressedMethod = Dialog.class.getDeclaredMethod("okPressed");
			okPressedMethod.setAccessible(true);
			okPressedMethod.invoke(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Error(ex);
		}
	}

	private void pressCancel(Shell dialogShell) {
		try {
			Dialog dialog = (Dialog) dialogShell.getData();
			Method cancelPressedMethod = Dialog.class.getDeclaredMethod("cancelPressed");
			cancelPressedMethod.setAccessible(true);
			cancelPressedMethod.invoke(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Error(ex);
		}
	}
}
