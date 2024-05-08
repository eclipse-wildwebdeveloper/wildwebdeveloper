/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.node;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wildwebdeveloper.debug.Messages;

import com.google.gson.Gson;

public class NodeAttachDebugTab extends AttachTab {
	public NodeAttachDebugTab() {
		super(9229);
	}

	private Text localRootText;
	private Text remoteRootText;

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite composite = (Composite)getControl();
		Composite rootMapComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).span(((GridLayout)composite.getLayout()).numColumns, 1).grab(true, false).indent(0, 40).applyTo(rootMapComposite);
		rootMapComposite.setLayout(new GridLayout(3, false));
		Label rootMapDescription = new Label(rootMapComposite, SWT.NONE);
		rootMapDescription.setText(Messages.NodeAttach_rootMapDescription);
		rootMapDescription.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
		GridDataFactory indentFactory = GridDataFactory.swtDefaults().indent(20, 0);
		Label remoteRootLabel = new Label(rootMapComposite, SWT.NONE);
		remoteRootLabel.setText(Messages.NodeAttach_remoteRoot);
		indentFactory.applyTo(remoteRootLabel);
		remoteRootText = new Text(rootMapComposite, SWT.BORDER);
		remoteRootText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		remoteRootText.addModifyListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
		Label localRootLabel = new Label(rootMapComposite, SWT.NONE);
		localRootLabel.setText(Messages.NodeAttach_localRoot);
		indentFactory.applyTo(localRootLabel);
		localRootText = new Text(rootMapComposite, SWT.BORDER);
		localRootText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		Button browseLocalButton = new Button(rootMapComposite, SWT.PUSH);
		browseLocalButton.setText(Messages.AbstractRunHTMLDebugTab_browse);
		browseLocalButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			DirectoryDialog directoryDialog = new DirectoryDialog(browseLocalButton.getShell());
			directoryDialog.setText(Messages.NodeAttach_localRoot);
			String selectedDirString = directoryDialog.open();
			if (selectedDirString != null) {
				localRootText.setText(selectedDirString);
			}
		}));
		ControlDecoration invalidDirectoryDecoration = new ControlDecoration(localRootText, SWT.TOP | SWT.LEFT);
		invalidDirectoryDecoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage());
		invalidDirectoryDecoration.setDescriptionText(Messages.NodeAttach_invalidLocalRootDirectory);
		invalidDirectoryDecoration.hide();
		localRootText.addModifyListener(e -> {
			if (!localRootText.getText().isEmpty() && !new File(localRootText.getText()).isDirectory()) {
				invalidDirectoryDecoration.show();
				setWarningMessage(Messages.NodeAttach_invalidLocalRootDirectory);
			} else {
				invalidDirectoryDecoration.hide();
				setWarningMessage(null);
			}
			setDirty(true);
			updateLaunchConfigurationDialog();
		});
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			Map<?, ?> map = new Gson().fromJson(configuration.getAttribute(DSPPlugin.ATTR_DSP_PARAM, ""), Map.class);
			if (map == null) {
				map = Collections.emptyMap();
			}
			String localRoot = configuration.getAttribute(NodeAttachDebugDelegate.LOCAL_ROOT, "");
			if (!localRoot.isEmpty()) {
				localRootText.setText(localRoot);
			} else {
				if (map.containsKey(NodeAttachDebugDelegate.LOCAL_ROOT)) {
					localRootText.setText(map.get(NodeAttachDebugDelegate.LOCAL_ROOT).toString());
				}
			}
			String remoteRoot = configuration.getAttribute(NodeAttachDebugDelegate.REMOTE_ROOT, "");
			if (!localRoot.isEmpty()) {
				remoteRootText.setText(remoteRoot);
			} else {
				if (map.containsKey(NodeAttachDebugDelegate.REMOTE_ROOT)) {
					remoteRootText.setText(map.get(NodeAttachDebugDelegate.REMOTE_ROOT).toString());
				}
			}
		} catch (CoreException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		if (!localRootText.getText().isEmpty()) {
			configuration.setAttribute(NodeAttachDebugDelegate.LOCAL_ROOT, localRootText.getText());
		}
		if (!remoteRootText.getText().isEmpty()) {
			configuration.setAttribute(NodeAttachDebugDelegate.REMOTE_ROOT, remoteRootText.getText());
		}
	}
}
