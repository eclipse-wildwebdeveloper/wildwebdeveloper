/*******************************************************************************
 * Copyright (c) 2018, 2023 Red Hat Inc. and others.
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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
import org.eclipse.swt.widgets.Text;
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
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestJsTs {
    private static final String WIZARD_CLASSNAME_TEMPLATE = "org.eclipse.ltk.internal.ui.refactoring.Refactoring";
    private static final String WIZARD_RENAME = "Rename";
    private static final String WIZARD_REFACTORING = "Refactoring";
    private static final String BUTTON_OK = "OK";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String BUTTON_CONTINUE = "Con&tinue";
    private static final String BUTTON_BACK = "< &Back";

    private IProject project;

    @BeforeEach
    public void setUpProject() throws CoreException {
        this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
        project.create(null);
        project.open(null);
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public void testRefactoringRenameInTypeaScript() throws Exception {
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
        String newContent = content.replaceAll(oldName, newName);

        int offset = content.indexOf(oldName);
        file.create(content.getBytes(), true, false, null);
        AbstractTextEditor editor = (AbstractTextEditor) IDE.openEditor(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
                "org.eclipse.ui.genericeditor.GenericEditor");
        editor.getSelectionProvider().setSelection(new TextSelection(offset, 0));
        editor.setFocus();
        DisplayHelper.sleep(5000); // Give time for LS to initialize enough before making edit and sending a
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
        AtomicBoolean newTextIsSet = new AtomicBoolean();

        Listener pressOKonRenameDialogPaint = event -> {
            if (event.widget instanceof Composite c) {
                Shell shell = c.getShell();
                if (shell != ideShell) {
                    if (shell.getData().getClass().getName().startsWith(WIZARD_CLASSNAME_TEMPLATE)) {
                        if (!newTextIsSet.get()) {
                            newTextIsSet.set(setNewText(c, newName));
                            System.out.println("testRefactoringRename(): New name is set: " + newName);
                        }
                        Set<String> buttons = getButtons(c);
                        if (WIZARD_RENAME.equals(shell.getText())) {
                            if (!renameDialogOkPressed.get()) {
                                if (buttons.contains(BUTTON_OK)) {
                                    System.out.println(
                                            "testRefactoringRename(): WIZARD_RENAME Emulating pressOK when BUTTON_OK");
                                    event.widget.getDisplay().asyncExec(() -> pressOk(shell));
                                    renameDialogOkPressed.set(true);
                                }
                            } else if (!renameDialogContinuePressed.get()) {
                                if (buttons.contains(BUTTON_CONTINUE)) {
                                    System.out.println(
                                            "testRefactoringRename(): WIZARD_RENAME Emulating pressOK when BUTTON_CONTINUE");
                                    event.widget.getDisplay().asyncExec(() -> pressOk(shell));
                                    renameDialogContinuePressed.set(true);
                                } else if (!renameDialogCancelPressed.get() && buttons.contains(BUTTON_CANCEL)
                                        && buttons.contains(BUTTON_BACK)) {
                                    System.out.println(
                                            "testRefactoringRename(): WIZARD_RENAME Emulating pressCancel when BUTTON_CANCEL & BUTTON_BACK");
                                    event.widget.getDisplay().asyncExec(() -> pressCancel(shell));
                                    renameDialogCancelPressed.set(true);
                                }
                            }
                        } else if (WIZARD_REFACTORING.equals(shell.getText())) {
                            if (!errorDialogOkPressed.get()) {
                                if (buttons.contains(BUTTON_OK)) {
                                    System.out.println(
                                            "testRefactoringRename(): WIZARD_REFACTORING Emulating pressOK when BUTTON_OK");
                                    event.widget.getDisplay().asyncExec(() -> pressOk(shell));
                                    errorDialogOkPressed.set(true);
                                }
                            }
                        }
                    } else if (shell.getData().getClass().getName()
                            .startsWith("org.eclipse.jface.dialogs.MessageDialog")) {
                        // Most probably it's "The Rename request is not valid at the given position"
                        // -like error
                        Set<String> buttons = getButtons(c);
                        if (!errorDialogOkPressed.get()) {
                            if (buttons.contains(BUTTON_OK)) {
                                System.out.println(
                                        "testRefactoringRename(): MESSAGE_DIALOG Emulating pressOK when BUTTON_OK");
                                event.widget.getDisplay().asyncExec(() -> pressOk(shell));
                                errorDialogOkPressed.set(true);
                            } else if (buttons.contains(BUTTON_CANCEL)) {
                                System.out.println(
                                        "testRefactoringRename(): MESSAGE_DIALOG Emulating pressCancel when BUTTON_CANCEL");
                                event.widget.getDisplay().asyncExec(() -> pressCancel(shell));
                                errorDialogOkPressed.set(true); // Report as OK pressed just to say the dialog is closed
                            }
                        }
                    }
                }
            }
        };

        try {
            display.addFilter(SWT.Paint, pressOKonRenameDialogPaint);
            ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, e);
            System.out.println("testRefactoringRename(): Executing command: " + IWorkbenchCommandConstants.FILE_RENAME);
            command.executeWithChecks(executionEvent);
            assertTrue(DisplayHelper.waitForCondition(display, 2000, () -> renameDialogOkPressed.get()),
                    "Rename dialog not shown");
            System.out.println("testRefactoringRename(): Rename dialog is shown");
            assertTrue(DisplayHelper.waitForCondition(display, 5000, () -> newContent.equals(document.get())),
                    "document not modified, rename not applied");
            System.out.println("testRefactoringRename(): Executed command: " + IWorkbenchCommandConstants.FILE_RENAME);
        } finally {
            ideShell.getDisplay().removeFilter(SWT.Paint, pressOKonRenameDialogPaint);
        }
    }

    static private Set<String> getButtons(Widget w) {
        Set<String> result = new HashSet<>();
        if (w instanceof Button button) {
            result.add(button.getText());
        } else if (w instanceof Composite composite) {
            for (Control child : composite.getChildren()) {
                result.addAll(getButtons(child));
            }
        }
        return result;
    }

    static private boolean setNewText(Widget w, String newText) {
        Set<Text> textWidgets = getTextWidgets(w);
        if (!textWidgets.isEmpty()) {
            textWidgets.forEach(t -> t.setText(newText));
            return true;
        }
        return false;
    }

    static private Set<Text> getTextWidgets(Widget w) {
        Set<Text> result = new HashSet<>();
        if (w instanceof Text text) {
            result.add(text);
        } else if (w instanceof Composite composite) {
            for (Control child : composite.getChildren()) {
                result.addAll(getTextWidgets(child));
            }
        }
        return result;
    }

    static private void pressOk(Shell dialogShell) {
        try {
            Dialog dialog = (Dialog) dialogShell.getData();
            Method okPressedMethod = Dialog.class.getDeclaredMethod("okPressed");
            okPressedMethod.setAccessible(true);
            okPressedMethod.invoke(dialog);
            System.out.println("testRefactoringRename(): pressOK is executed");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Error(ex);
        }
    }

    static private void pressCancel(Shell dialogShell) {
        try {
            Dialog dialog = (Dialog) dialogShell.getData();
            Method cancelPressedMethod = Dialog.class.getDeclaredMethod("cancelPressed");
            cancelPressedMethod.setAccessible(true);
            cancelPressedMethod.invoke(dialog);
            System.out.println("testRefactoringRename(): pressCancel is executed");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Error(ex);
        }
    }
}
