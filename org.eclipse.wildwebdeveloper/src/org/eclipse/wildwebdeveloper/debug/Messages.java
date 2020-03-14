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
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wildwebdeveloper.debug.messages"; //$NON-NLS-1$
	public static String AbstractRunHTMLDebugTab_browse;
	public static String AbstractRunHTMLDebugTab_fileRadioToolTip;
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
	public static String RunProgramTab_program;
	public static String RunProgramTab_title;
	public static String RunProgramTab_workingDirectory;
	public static String firefoxAttachNote;
	public static String NodeAttach_rootMapDescription;
	public static String NodeAttach_localRoot;
	public static String NodeAttach_remoteRoot;
	public static String NodeAttach_invalidLocalRootDirectory;
	public static String ChromeAttachTab_runWith;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
