package org.eclipse.wildwebdeveloper.jsts;

import java.util.HashMap;
import java.util.Map;

public class TypeScriptPlugin {
	private String pluginName;
	private String pluginProbeLocation;
	
	public TypeScriptPlugin(String name, String location) {
		pluginName = name;
		pluginProbeLocation = location;
	}
	
	public Map<String, String> toMap() {
		Map<String, String> tsPlugin = new HashMap<>();
		tsPlugin.put("name", pluginName);
		tsPlugin.put("location", pluginProbeLocation);
		return tsPlugin;
	}
}
