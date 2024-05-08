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

import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.wildwebdeveloper.debug.AbstractDebugAdapterLaunchShortcut;

public class NodeRunDebugLaunchShortcut extends AbstractDebugAdapterLaunchShortcut {

	public NodeRunDebugLaunchShortcut() {
		super(NodeRunDAPDebugDelegate.ID, new String[] {"org.eclipse.wildwebdeveloper.js", "org.eclipse.wildwebdeveloper.ts"}, true);
	}

	@Override
	protected IResource getLaunchableResource(IContainer container) {
		if (container == null) {
			return null;
		}
		try {
			IResource[] jsFiles = Arrays.stream(container.members()).filter(member -> member.getType() == IResource.FILE && member.getName().endsWith(".js")).toArray(IResource[]::new);
			if (jsFiles.length == 1) {
				return jsFiles[0];
			}
		} catch (CoreException e) {
			ILog.get().error(e.getMessage(), e);
		}
		return null;
		
	}
}
