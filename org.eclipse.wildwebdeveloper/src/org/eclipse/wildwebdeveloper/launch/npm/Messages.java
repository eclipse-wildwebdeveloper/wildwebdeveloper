package org.eclipse.wildwebdeveloper.launch.npm;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wildwebdeveloper.launch.npm.messages"; //$NON-NLS-1$
	public static String NpmLaunchDelegate_npmError;
	public static String NpmLaunchDelegate_npmInstallFor;
	public static String NpmLaunchDelegate_npmOutput;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
