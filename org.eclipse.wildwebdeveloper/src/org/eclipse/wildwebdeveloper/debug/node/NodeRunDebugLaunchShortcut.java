/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.debug.node;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugAdapterLaunchShortcut;

public class NodeRunDebugLaunchShortcut extends AbstractDebugAdapterLaunchShortcut {

	public NodeRunDebugLaunchShortcut() {
		super(NodeRunDAPDebugDelegate.ID, "org.eclipse.wildwebdeveloper.js");
	}

	@Override public void configureLaunchConfiguration(File file, ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(NodeRunDAPDebugDelegate.PROGRAM, file.getAbsolutePath());
		wc.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, file.getParentFile().getAbsolutePath());
	}

	@Override public boolean match(ILaunchConfiguration launchConfig, File selectedFile) {
		try {
			return launchConfig.getAttribute(NodeRunDAPDebugDelegate.PROGRAM, "").equals(selectedFile.getAbsolutePath()); //$NON-NLS-1$
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return false;
		}
	}
}
