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
 *   Andrew Obuchowicz (Red Hat Inc.) 
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.firefox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wildwebdeveloper.debug.AbstractRunHTMLDebugTab;
import org.eclipse.wildwebdeveloper.debug.Messages;

public class RunFirefoxDebugTab extends AbstractRunHTMLDebugTab {	
	private Button reloadOnChange;

	public RunFirefoxDebugTab() {
		 super.shortcut = new FirefoxRunDebugLaunchShortcut(); // contains many utilities
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		reloadOnChange = new Button(resComposite, SWT.CHECK);
		reloadOnChange.setText(Messages.RunFirefoxDebugTab_ReloadOnChange); 
		reloadOnChange.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			boolean reloadOnChangeValue = configuration.getAttribute(FirefoxRunDABDebugDelegate.RELOAD_ON_CHANGE, false);
			reloadOnChange.setSelection(reloadOnChangeValue);
		} catch (CoreException ex) {
			ILog.get().log(ex.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		configuration.setAttribute(FirefoxRunDABDebugDelegate.RELOAD_ON_CHANGE, reloadOnChange.getSelection());
	}

}
