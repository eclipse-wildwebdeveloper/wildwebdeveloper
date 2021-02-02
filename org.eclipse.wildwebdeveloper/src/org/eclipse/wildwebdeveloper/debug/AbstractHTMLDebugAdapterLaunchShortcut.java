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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wildwebdeveloper.Activator;

public abstract class AbstractHTMLDebugAdapterLaunchShortcut extends AbstractDebugAdapterLaunchShortcut {

	public AbstractHTMLDebugAdapterLaunchShortcut(String launchConfigTypeId) {
		super(launchConfigTypeId, "org.eclipse.wildwebdeveloper.html", true);
	}

	@Override
	public boolean canLaunchResource(IResource resource) {
		if (resource instanceof IContainer) {
			return getLaunchableResource(Adapters.adapt(resource, IContainer.class)) != null;
		}
		return super.canLaunchResource(resource);
	}

	@Override
	public void configureLaunchConfiguration(File file, ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(AbstractHTMLDebugDelegate.PROGRAM, file.getAbsolutePath());
		wc.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, file.getParentFile().getAbsolutePath());
	}

	@Override
	public boolean match(ILaunchConfiguration launchConfig, File selectedFile) {
		try {
			return launchConfig.getAttribute(AbstractHTMLDebugDelegate.PROGRAM, "").equals(selectedFile.getAbsolutePath()); //$NON-NLS-1$
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return false;
		}
	}

	/**
	 * Finds "index.html" resource in a container (project or folder), null if it
	 * can't be found. If the container has a single .html file, it is returned
	 * regardless of it being called "index.html"
	 * 
	 * @param container to search for index.html
	 * @return IResource index.html file contained in the project or null if none
	 *         exist
	 */
	@Override
	public IResource getLaunchableResource(IContainer container) {
		try {
			if (container.members().length == 1 && container.members()[0].getName().matches(".*\\.html$")) {
				return container.members()[0];
			}
			for (IResource projItem : container.members()) {
				if (projItem.getName().equals("index.html")) { //$NON-NLS-1$
					return projItem;
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}


}
