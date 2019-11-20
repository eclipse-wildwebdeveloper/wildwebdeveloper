/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import java.io.File;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wildwebdeveloper.Activator;

public class SelectionUtils {

	private SelectionUtils() {
		// stateless, don't instantiate
	}

	private static IFile getSelectedIFile() {
		try {
			ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return Adapters.adapt(((IStructuredSelection)selection).getFirstElement(), IFile.class);
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput)input).getFile();
			}
		}
		return null;
	}

	public static File getSelectedFile(Predicate<File> condition) {
		IFile iFile = getSelectedIFile();
		if (iFile != null) {
			File file = iFile.getRawLocation().makeAbsolute().toFile();
			if (condition == null || condition.test(file)) {
				return file;
			}
		}
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			File file = getFile(editor.getEditorInput(), condition);
			if (file != null) {
				return file;
			}

		}
		return null;
	}
	
	public static File getSelectedProject() {
		IFile iFile = getSelectedIFile();
		if (iFile != null) {
			return iFile.getProject().getLocation().toFile();
		}
		File file = getSelectedFile(null);
		if (file != null) {
			return file.getParentFile();
		}
		return null;
	}

	public static String pathOrEmpty(File file) {
		return file != null ? file.getAbsolutePath() : ""; //$NON-NLS-1$
	}

	public static File getFile(ISelection selection, Predicate<File> condition) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (StructuredSelection)selection;
			Object firstElement = structuredSelection.getFirstElement();
			IResource resource = Adapters.adapt(firstElement, IResource.class);
			if (resource != null) {
				File file = resource.getLocation().toFile();
				if (condition == null || condition.test(file)) {
					return file;
				}
			}
		}
		if (selection instanceof TextSelection) {
			// check whether it comes from active editor
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (part instanceof ITextEditor) { // most likely the source of the selection
				return getFile(((IEditorPart)part).getEditorInput(), condition);
			}
		}
		return null;
	}

	public static File getFile(IEditorInput editorInput, Predicate<File> condition) {
		if (editorInput instanceof FileEditorInput) {
			File file = ((FileEditorInput)editorInput).getFile().getLocation().toFile();
			if (file != null && (condition == null || condition.test(file))) {
				return file;
			}
		}
		if (editorInput instanceof IURIEditorInput) {
			File file = new File(((IURIEditorInput)editorInput).getURI());
			if (condition == null || condition.test(file)) {
				return file;
			}
		}
		// see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=550804
		return null;
	}
}
