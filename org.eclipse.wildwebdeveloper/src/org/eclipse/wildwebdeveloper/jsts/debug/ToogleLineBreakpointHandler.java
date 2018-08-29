/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.debug;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wst.jsdt.debug.core.model.JavaScriptDebugModel;

public class ToogleLineBreakpointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof AbstractTextEditor)) {
			return Status.CANCEL_STATUS;
		}
		AbstractTextEditor editor = (AbstractTextEditor)part;
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (!(selection instanceof ITextSelection)) {
			return Status.CANCEL_STATUS;
		}
		ITextSelection textSelection = (ITextSelection)selection;
		if (!(editor.getEditorInput() instanceof FileEditorInput)) {
			return Status.CANCEL_STATUS;
		}
		IFile file = ((FileEditorInput)editor.getEditorInput()).getFile();
		try {
			return JavaScriptDebugModel.createLineBreakpoint(file, textSelection.getStartLine() + 1, -1, -1, new HashMap<>(), true);
		} catch (DebugException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
			return null;
		}
	}

}
