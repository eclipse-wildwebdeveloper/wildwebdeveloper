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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.ILaunchable;

// Based off of https://github.com/eclipse/aCute/blob/master/org.eclipse.acute/src/org/eclipse/acute/Tester.java

public class PackageJSONAdapterFactory implements IAdapterFactory {

	private static ILaunchable DUMMY = new ILaunchable() {
	};

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (!ILaunchable.class.equals(adapterType)) {
			return null;
		}
		IResource resource = Adapters.adapt(adaptableObject, IResource.class);
		if (new NpmLaunchShortcut().canLaunch(resource.getLocation().toFile())) {
			return adapterType.cast(DUMMY);
		}

		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ILaunchable.class };
	}

}
