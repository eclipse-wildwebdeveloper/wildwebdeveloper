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

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
	
	public static String NPMLaunchTab_argumentLabel;
	public static String NPMLaunchTab_notPackageJSONFile;
	public static String NPMLaunchTab_programPathLabel;
	public static String NPMLaunchTab_selectPackageJSON;
	public static String NpmLaunchDelegate_npmError;
	public static String NpmLaunchDelegate_npmInstallFor;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
