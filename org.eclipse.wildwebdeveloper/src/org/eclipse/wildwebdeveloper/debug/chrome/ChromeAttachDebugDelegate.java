/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *   Pierre-Yves B. - Issue #180 Wrong path to nodeDebug.js
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.chrome;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;

public class ChromeAttachDebugDelegate extends ChromeRunDAPDebugDelegate {
	
	static final String ID = "org.eclipse.wildwebdeveloper.launchConfiguration.chromeAttachDebug"; //$NON-NLS-1$

	static final String ADDRESS = "address"; //$NON-NLS-1$

	@Override
	protected boolean configureAdditionalParameters(ILaunchConfiguration configuration, Map<String, Object> param) throws CoreException {
		if (super.configureAdditionalParameters(configuration, param)) {
			param.remove("file");
			param.put("request", "attach");
			String url = configuration.getAttribute(ChromeRunDAPDebugDelegate.URL, "https://github.com/eclipse/wildwebdeveloper/"); //$NON-NLS-1$
			param.put(ChromeRunDAPDebugDelegate.URL, url);	
			param.put(ADDRESS, configuration.getAttribute(ADDRESS, "no address defined")); //$NON-NLS-1$
			param.put(LaunchConstants.PORT, configuration.getAttribute(LaunchConstants.PORT, 9229));
			return true;
		}
		return false;
	}

}
