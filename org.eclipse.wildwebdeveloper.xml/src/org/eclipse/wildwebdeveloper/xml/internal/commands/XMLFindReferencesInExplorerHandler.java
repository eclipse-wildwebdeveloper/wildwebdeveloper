/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants;

/**
 * Triggers "Find References" on a grammar file selected in the file explorer.
 * When the workspace index is not enabled, offers to enable it.
 */
public class XMLFindReferencesInExplorerHandler extends AbstractHandler {

	private static final String FIND_REFERENCES_COMMAND = "org.eclipse.ui.genericeditor.findReferences";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection structured)) {
			return null;
		}
		Object first = structured.getFirstElement();
		if (!(first instanceof IFile file)) {
			return null;
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (!store.getBoolean(XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_ENABLED.preferenceId)) {
			boolean enable = MessageDialog.openQuestion(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Workspace Index Not Enabled",
					"Find References on grammar files requires the XML workspace index.\n\nEnable it now?");
			if (enable) {
				store.setValue(XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_ENABLED.preferenceId, true);
			} else {
				return null;
			}
		}

		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			var editor = IDE.openEditor(page, file);
			if (editor instanceof ITextEditor) {
				IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
				handlerService.executeCommand(FIND_REFERENCES_COMMAND, null);
			}
		} catch (Exception e) {
			ILog.get().error("Error finding references for file: " + file.getName(), e);
		}
		return null;
	}
}
