/*******************************************************************************
 * Copyright (c) 2019, 2023 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.debug;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wildwebdeveloper.debug.messages"; //$NON-NLS-1$
	public static String AbstractRunHTMLDebugTab_browse;
	public static String AbstractRunHTMLDebugTab_fileRadioToolTip;
	public static String AbstractRunHTMLDebugTab_webRoot_folder;
	public static String AbstractRunHTMLDebugTab_browse_workspace;
	public static String AbstractRunHTMLDebugTab_select_webroot;
	public static String AbstractRunHTMLDebugTab_cannot_debug_without_webroot;
	public static String AbstractRunHTMLDebugTab_cannot_access_webroot_folder;
	public static String AbstractRunHTMLDebugTab_webroot_folder_is_not_a_directory;
	public static String AttachTab_address;
	public static String AttachTab_port;
	public static String AttachTab_title;
	public static String FirefoxDebugTab_File;
	public static String RunFirefoxDebugTab_ReloadOnChange;
	public static String RunFirefoxDebugTab_URL_Note;
	public static String RunProgramTab_argument;
	public static String RunProgramTab_error_nonReadableFile;
	public static String RunProgramTab_error_notJSFile;
	public static String RunProgramTab_error_unknownFile;
	public static String RunProgramTab_error_malformedUR;
	public static String RunProgramTab_program;
	public static String RunProgramTab_title;
	public static String RunProgramTab_workingDirectory;
	public static String firefoxAttachNote;
	public static String NodeAttach_rootMapDescription;
	public static String NodeAttach_localRoot;
	public static String NodeAttach_remoteRoot;
	public static String NodeAttach_invalidLocalRootDirectory;
	public static String ChromeAttachTab_runWith;
	public static String ChromeAttachTab_browserTab;
	public static String ChromeAttachTab_browserPreferences;
	
	public static String NodeDebug_TSConfirError_Title;
	public static String NodeDebug_TSConfirError_NoTsConfig;
	public static String NodeDebug_TSConfirError_SourceMapIsNotEnabled;
	public static String NodeDebug_TSConfigError_OutDirIsNotSet;
	public static String NodeDebug_TSConfigError_OutDirNotSupportedModule;
	public static String NodeDebug_TSConfirError_OpenTSConfigInEditor;
	public static String NodeDebug_TSConfirError_CreateAndOpenTSConfigInEditor;
	public static String NodeDebug_TSConfirError_StartDebuggingAsIs;
	public static String NodeDebug_TSConfirError_Cancel;
	
	public static String RuntimeExecutable_NotDefinedError_Title;
	public static String RuntimeExecutable_NotDefinedError_Message;
	public static String RuntimeExecutable_NotDefinedError_Message_1;
	public static String RuntimeExecutable_NotDefinedError_Message_2;
	public static String RuntimeExecutable_NotDefinedError_OpenWebBrowserPreferencePage;
	public static String RuntimeExecutable_NotDefinedError_OpenLaunchConfigurationBrowserTab;
	public static String RuntimeExecutable_Chrome;
	public static String RuntimeExecutable_Firefox;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
