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

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wildwebdeveloper.debug.RunHTMLDebugTab;

public class RunFirefoxDebugTab extends RunHTMLDebugTab {	
	private Button reloadOnChange;

	public RunFirefoxDebugTab() {
		 super.shortcut = new FirefoxRunDebugLaunchShortcut(); // contains many utilities
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		reloadOnChange = new Button(resComposite, SWT.CHECK);
		reloadOnChange.setText("Reload on change");
		reloadOnChange.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}));
		
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
		super.performApply(configuration);
		configuration.setAttribute(FirefoxRunDABDebugDelegate.RELOAD_ON_CHANGE, reloadOnChange.getSelection());
	}

}
