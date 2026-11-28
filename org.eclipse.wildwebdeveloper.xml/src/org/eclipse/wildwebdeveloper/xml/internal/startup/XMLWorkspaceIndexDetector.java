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
package org.eclipse.wildwebdeveloper.xml.internal.startup;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants;

/**
 * Runs at Eclipse startup to detect grammar files in the workspace and prompt
 * the user to enable the XML workspace index (like VSCode XML does).
 */
public class XMLWorkspaceIndexDetector implements IStartup {

	public static final String PREF_WORKSPACE_INDEX_DETECTION_IGNORE = //
			"org.eclipse.wildwebdeveloper.xml.workspaceIndex.detection.ignore";

	private static final String[] GRAMMAR_EXTENSIONS = { "xsd", "dtd", "rng", "rnc" };
	private static final String[] EXCLUDED_DIRS = { "node_modules", "target", "build", ".git", ".hg", ".svn", "out",
			"dist", "bin", ".gradle" };

	@Override
	public void earlyStartup() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_ENABLED.preferenceId)) {
			return;
		}
		if (store.getBoolean(PREF_WORKSPACE_INDEX_DETECTION_IGNORE)) {
			return;
		}

		Job job = new Job("Detecting XML grammar files") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (hasGrammarFiles()) {
					Display.getDefault().asyncExec(() -> promptEnableWorkspaceIndex(store));
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(3000);
	}

	private static boolean hasGrammarFiles() {
		boolean[] found = { false };
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(resource -> {
				if (found[0]) {
					return false;
				}
				if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
					for (String excluded : EXCLUDED_DIRS) {
						if (excluded.equals(resource.getName())) {
							return false;
						}
					}
				}
				if (resource instanceof IFile file) {
					String ext = file.getFileExtension();
					if (ext != null) {
						for (String grammarExt : GRAMMAR_EXTENSIONS) {
							if (grammarExt.equals(ext)) {
								found[0] = true;
								return false;
							}
						}
					}
				}
				return true;
			});
		} catch (CoreException e) {
			// ignore
		}
		return found[0];
	}

	private static void promptEnableWorkspaceIndex(IPreferenceStore store) {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(
				window.getShell(),
				Messages.XMLWorkspaceIndexDetector_promptTitle,
				Messages.XMLWorkspaceIndexDetector_promptMessage,
				Messages.XMLWorkspaceIndexDetector_promptToggle,
				false,
				store,
				PREF_WORKSPACE_INDEX_DETECTION_IGNORE);
		if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
			store.setValue(XMLPreferenceServerConstants.XML_PREFERENCES_WORKSPACE_INDEX_ENABLED.preferenceId, true);
		}
	}
}
