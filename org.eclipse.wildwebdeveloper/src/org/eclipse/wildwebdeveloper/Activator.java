/*******************************************************************************
 * Copyright (c) 2016-2018 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.wildwebdeveloper.css.CSSLanguageServer;
import org.eclipse.wildwebdeveloper.html.HTMLLanguageServer;
import org.eclipse.wildwebdeveloper.yaml.YAMLLanguageServer;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.wildwebdeveloper"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ScopedPreferenceStore cssPreferenceStore;

	private ScopedPreferenceStore htmlPreferenceStore;

	private ScopedPreferenceStore yamlPreferenceStore;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns the currently active workbench window shell or <code>null</code> if
	 * none.
	 *
	 * @return the currently active workbench window shell or <code>null</code>
	 */
	public static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0) {
				return windows[0].getShell();
			}
		} else {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the CSS preference store.
	 * 
	 * @return the CSS preference store.
	 */
	public IPreferenceStore getCSSPreferenceStore() {
		// Create the preference store lazily.
		ScopedPreferenceStore result = cssPreferenceStore;
		if (result == null) { // First check (no locking)
			synchronized (this) {
				result = cssPreferenceStore;
				if (result == null) { // Second check (with locking)
					cssPreferenceStore = result = new ScopedPreferenceStore(InstanceScope.INSTANCE,
							CSSLanguageServer.LANGUAGE_SERVER_ID);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the HTML preference store.
	 * 
	 * @return the HTML preference store.
	 */
	public IPreferenceStore getHTMLPreferenceStore() {
		// Create the preference store lazily.
		ScopedPreferenceStore result = htmlPreferenceStore;
		if (result == null) { // First check (no locking)
			synchronized (this) {
				result = htmlPreferenceStore;
				if (result == null) { // Second check (with locking)
					htmlPreferenceStore = result = new ScopedPreferenceStore(InstanceScope.INSTANCE,
							HTMLLanguageServer.LANGUAGE_SERVER_ID);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the YAML preference store.
	 * 
	 * @return the YAML preference store.
	 */
	public IPreferenceStore getYAMLPreferenceStore() {
		// Create the preference store lazily.
		ScopedPreferenceStore result = yamlPreferenceStore;
		if (result == null) { // First check (no locking)
			synchronized (this) {
				result = yamlPreferenceStore;
				if (result == null) { // Second check (with locking)
					yamlPreferenceStore = result = new ScopedPreferenceStore(InstanceScope.INSTANCE,
							YAMLLanguageServer.LANGUAGE_SERVER_ID);
				}
			}
		}
		return result;
	}
}
