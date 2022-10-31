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
package org.eclipse.wildwebdeveloper.debug.firefox;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.Messages;
import org.eclipse.wildwebdeveloper.debug.node.AttachTab;

class FirefoxAttachTab extends AttachTab {
	
	public FirefoxAttachTab() {
		super(6000);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Control control = getControl();
		if (control instanceof Composite composite) {
			Link label = new Link(composite, SWT.WRAP);
			label.setText(Messages.firefoxAttachNote);
			label.getAccessible().addAccessibleListener(new AccessibleAdapter() {
				@Override
				public void getDescription(AccessibleEvent event) {
					event.result = label.getText();
				}
			});
			Layout layout = composite.getLayout();
			if (layout instanceof GridLayout gridLayout) {
				GridDataFactory.swtDefaults()
					.align(SWT.BEGINNING, SWT.TOP)
					.grab(true, false)
					.indent(0, 10)
					.span(gridLayout.numColumns, 1)
					.applyTo(label);
			}
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(LaunchConstants.PORT, 6000);
	}
}
