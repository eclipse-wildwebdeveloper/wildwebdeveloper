/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.launch.npm;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugAdapterLaunchShortcut;
import org.eclipse.wildwebdeveloper.debug.AbstractHTMLDebugDelegate;

public class NpmLaunchShortcut extends AbstractDebugAdapterLaunchShortcut {

	public NpmLaunchShortcut() {
		super(NpmLaunchDelegate.ID, "org.eclipse.wildwebdeveloper.json.npmpackage");
	}

	@Override
	public void configureLaunchConfiguration(File file, ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(AbstractHTMLDebugDelegate.PROGRAM, file.getAbsolutePath());
		wc.setAttribute(AbstractHTMLDebugDelegate.ARGUMENTS, "install");
		wc.setAttribute(AbstractHTMLDebugDelegate.CWD, file.getParentFile().getAbsolutePath());
	}

	@Override
	public boolean match(ILaunchConfiguration launchConfig, File selectedFile) {
		try {
			return launchConfig.getAttribute(AbstractHTMLDebugDelegate.PROGRAM, "") //$NON-NLS-1$
					.equals(selectedFile.getAbsolutePath());
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return false;
		}
	}
}
