/*******************************************************************************
 * Copyright (c) 2019, 2022 Red Hat Inc. and others.
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
package org.eclipse.wildwebdeveloper.debug.node;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.Messages;

public class AttachTab extends AbstractLaunchConfigurationTab {

	private static final String DEFAULT_ADDRESS = "localhost"; //$NON-NLS-1$
	
	private Text addressText;
	private Spinner portSpinner;
	private int defaultPort;
	protected Composite resComposite;
	
	public AttachTab(int defaultPort) {
		this.defaultPort = defaultPort;
	}

	@Override
	public void createControl(Composite parent) {
		resComposite = new Composite(parent, SWT.NONE);
		resComposite.setLayout(new GridLayout(2, false));
		resComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		new Label(resComposite, SWT.NONE).setText(Messages.AttachTab_address);
		this.addressText = new Text(resComposite, SWT.BORDER);
		this.addressText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		this.addressText.addModifyListener(event -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		new Label(resComposite, SWT.NONE).setText(Messages.AttachTab_port);
		this.portSpinner = new Spinner(resComposite, SWT.BORDER);
		this.portSpinner.setMinimum(0);
		this.portSpinner.setMaximum(65535);
		this.portSpinner.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		setControl(resComposite);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(NodeAttachDebugDelegate.ADDRESS, DEFAULT_ADDRESS);
		configuration.setAttribute(LaunchConstants.PORT, defaultPort);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			this.addressText.setText(configuration.getAttribute(NodeAttachDebugDelegate.ADDRESS, DEFAULT_ADDRESS));
			this.portSpinner.setSelection(configuration.getAttribute(LaunchConstants.PORT, defaultPort));
		} catch (CoreException e) {
			ILog.get().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(NodeAttachDebugDelegate.ADDRESS, this.addressText.getText());
		configuration.setAttribute(LaunchConstants.PORT, this.portSpinner.getSelection());
	}

	@Override
	public String getName() {
		return Messages.AttachTab_title;
	}

}
