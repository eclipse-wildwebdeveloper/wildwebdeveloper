/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug;

import java.text.MessageFormat;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationEditDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.WebBrowserPreferencePage;

@SuppressWarnings("restriction")
public class MessageUtils {
	private static final String BROWSER_TAB_NAME = "Browser"; //$NON-NLS-1$
	private static final String WEB_BROWSER_PREFERENCE_PAGE = "org.eclipse.ui.browser.preferencePage"; //$NON-NLS-1$
	
	private MessageUtils() {
		// stateless, don't instantiate
	}
	
	/**
	 * Shows an error message, suggesting to fix browser location either by
	 * - Selecting a Runtime executable in launch configuration (if applicable)
	 * - Adding/editing a browser in Web Browser preference page
	 * 
	 * @param parentShell  the parent shell of the dialog, or <code>null</code> if none
	 * @param configuration A launch configuration
	 * @param mode a launch mode 
	 * @param browserName a browser display name 
	 * @param suggestEditLaunch <code>true</code> in case of opening a launch configuration Browser tab is to be suggested, otherwise - <code>false</code>
	 * @return the <code>int</code> status of opening the dialog
	 */
	public static int showBrowserLocationsConfigurationError(Shell parentShell, ILaunchConfiguration configuration, String mode, String browserName, boolean suggestEditLaunch) {
		String title = MessageFormat.format(Messages.RuntimeExecutable_NotDefinedError_Title, browserName);
		String browserTabMessage = suggestEditLaunch ? MessageFormat.format(Messages.RuntimeExecutable_NotDefinedError_Message_1, browserName) : ""; //$NON-NLS-1$
		String webBrowsersMessage = MessageFormat.format(Messages.RuntimeExecutable_NotDefinedError_Message_2, browserName);
		String message = MessageFormat.format(Messages.RuntimeExecutable_NotDefinedError_Message, browserName, browserTabMessage, webBrowsersMessage);
		IStatus errorStatus = Status.error(title);
		ILog.get().log(errorStatus);

		int[] result = {Window.CANCEL};
		Display.getDefault().syncExec(() -> {
			int response = suggestEditLaunch ?
					MessageDialog.open(MessageDialog.ERROR, parentShell, title, message, SWT.NONE, 
							Messages.RuntimeExecutable_NotDefinedError_OpenLaunchConfigurationBrowserTab,
							Messages.RuntimeExecutable_NotDefinedError_OpenWebBrowserPreferencePage,
							IDialogConstants.CANCEL_LABEL) :
						MessageDialog.open(MessageDialog.ERROR, parentShell, title, message, SWT.NONE, 
									Messages.RuntimeExecutable_NotDefinedError_OpenWebBrowserPreferencePage,
									IDialogConstants.CANCEL_LABEL);
			if (!suggestEditLaunch) {
				response++;
			}
			if (response == 0) {
				ILaunchGroup group = DebugUITools.getLaunchGroup(configuration, mode);
				if (group != null) {
					LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(group.getIdentifier());
					if (groupExt != null) {
						final LaunchConfigurationEditDialog dialog = new LaunchConfigurationEditDialog(parentShell, configuration, groupExt, false);
						BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), () -> {
							dialog.create();

							ILaunchConfigurationTab[] tabs = dialog.getTabs();
							if(tabs != null) {
								for (int i = 0; i < tabs.length; i++) {
									if (tabs[i].getName().equals(BROWSER_TAB_NAME)) {
										dialog.setActiveTab(i);
										break;
									}
								}
							}
							result[0] = dialog.open();
						});
					}
				}
			} else if (response == 1) {
				WebBrowserPreferencePage page = new WebBrowserPreferencePage();
				page.setTitle(org.eclipse.ui.internal.browser.Messages.preferenceWebBrowserTitle);
				final IPreferenceNode targetNode = new PreferenceNode(WEB_BROWSER_PREFERENCE_PAGE, page);
				PreferenceManager manager = new PreferenceManager();
				manager.addToRoot(targetNode);
				final PreferenceDialog dialog = new PreferenceDialog(parentShell, manager);
				BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), () -> {
					dialog.create();
					dialog.setMessage(targetNode.getLabelText());
					result[0] = dialog.open();
				});
			}
		});
		return result[0];
	}
}
