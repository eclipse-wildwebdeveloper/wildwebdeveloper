/*******************************************************************************
 * Copyright (c) 2019, 2023 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AllCleanRule implements BeforeEachCallback, AfterEachCallback {

	private void clearProjects() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		DisplayHelper.sleep(500); // Let the queues to be processed

		LanguageServiceAccessor.clearStartedServers();
		DisplayHelper.sleep(1000); // Let the queues to be processed

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				project.close(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		DisplayHelper.sleep(500); // Let the queues to be processed

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				project.delete(true, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		closeIntro();
		enableLogging();
		clearProjects();
	}

	public static void closeIntro() {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IViewReference ref : activePage.getViewReferences()) {
			activePage.hideView(ref);
		}
	}

	public static void enableLogging() {
		ScopedPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.lsp4e");
		prefs.putValue("org.eclipse.wildwebdeveloper.angular.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.jsts.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.css.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.html.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.json.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.xml.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.yaml.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.eslint.file.logging.enabled", Boolean.toString(true));
		prefs.putValue("org.eclipse.wildwebdeveloper.vue.file.logging.enabled", Boolean.toString(true));

	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		clearProjects();
	}
}
