/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.chrome;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.Messages;
import org.eclipse.wildwebdeveloper.debug.node.AttachTab;

public class ChromeAttachTab extends AttachTab {

	private Combo browserToUse;
	private Text urlText;
	
	public ChromeAttachTab() {
		super(9222);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		new Label(resComposite, SWT.NONE).setText("URL: ");
		this.urlText = new Text(resComposite, SWT.BORDER);
		this.urlText.setToolTipText(Messages.RunFirefoxDebugTab_URL_Note);
		this.urlText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		urlText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		new Label(resComposite, SWT.NONE).setText(Messages.ChromeAttachTab_runWith);
		browserToUse = new Combo(resComposite, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		browserToUse.add(ChromeRunDAPDebugDelegate.CHROMIUM);
		browserToUse.add(ChromeRunDAPDebugDelegate.CHROME);
		browserToUse.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			browserToUse.setText(configuration.getAttribute(ChromeRunDAPDebugDelegate.RUNTIME_EXECUTABLE, ChromeRunDAPDebugDelegate.CHROMIUM));
			urlText.setText(configuration.getAttribute(ChromeRunDAPDebugDelegate.URL, ""));
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		configuration.setAttribute(ChromeRunDAPDebugDelegate.RUNTIME_EXECUTABLE, browserToUse.getText());
		configuration.setAttribute(ChromeRunDAPDebugDelegate.URL, urlText.getText());
	}
}
