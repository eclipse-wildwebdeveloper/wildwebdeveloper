/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.npm;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugAdapterLaunchShortcut;

public class NpmLaunchShortcut extends AbstractDebugAdapterLaunchShortcut {

	private static final String PACKAGE_JSON = "package.json";

	public NpmLaunchShortcut() {
		super(NpmLaunchDelegate.ID, "org.eclipse.wildwebdeveloper.json.npmpackage", false);
	}

	@Override
	public void configureLaunchConfiguration(File file, ILaunchConfigurationWorkingCopy wc) {
		super.configureLaunchConfiguration(file, wc);
		wc.setAttribute(NpmLaunchDelegate.ARGUMENTS, "install");
	}

	@Override
	protected IResource getLaunchableResource(IContainer container) {
		if (container != null && container.getFile(new Path(PACKAGE_JSON)).exists()) {
			return container.getFile(new Path(PACKAGE_JSON));
		}
		return null;
	}
}
