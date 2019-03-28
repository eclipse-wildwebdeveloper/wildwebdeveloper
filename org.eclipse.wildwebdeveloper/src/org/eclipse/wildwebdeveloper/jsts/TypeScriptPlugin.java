/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;

public class TypeScriptPlugin {
	private String pluginName;
	private String pluginProbeLocation;
	
	public TypeScriptPlugin(String name) throws IOException {
		pluginName = name;
		pluginProbeLocation = FileLocator.toFileURL(getClass().getResource("/language-servers/node_modules/" + name)).toString();
	}
	
	public Map<String, String> toMap() {
		Map<String, String> tsPlugin = new HashMap<>();
		tsPlugin.put("name", pluginName);
		tsPlugin.put("location", pluginProbeLocation);
		return tsPlugin;
	}
}
