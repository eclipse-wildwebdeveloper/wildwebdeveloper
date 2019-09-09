package org.eclipse.wildwebdeveloper.debug;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;

public final class AbstractDebugDelegate {
	public static final String PROGRAM = "program"; //$NON-NLS-1$
	public static final String ARGUMENTS = "runtimeArgs"; //$NON-NLS-1$
	public static final String CWD = DebugPlugin.ATTR_WORKING_DIRECTORY;
	public static final String ENV = ILaunchManager.ATTR_ENVIRONMENT_VARIABLES;
	public static final String SOURCE_MAPS = "sourceMaps";

}
