/*******************************************************************************
 * Copyright (c) 2020, 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.debug.chrome;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.browser.BrowserManager;
import org.eclipse.ui.internal.browser.IBrowserDescriptor;
import org.eclipse.wildwebdeveloper.Activator;
import org.eclipse.wildwebdeveloper.debug.Messages;

public class ChromeExecutableTab extends AbstractLaunchConfigurationTab {

	private ComboViewer browserToUse;
	private Image image;
	private List<Object> proposals;

	public ChromeExecutableTab() {
		try (InputStream imageResource = getClass().getResourceAsStream("/icons/ChromeIcon.png")) {
			image = new Image(Display.getDefault(), imageResource);
		} catch (IOException e) {
			Activator.getDefault().getLog().error(e.getMessage(), e);
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(2, false));

		new Label(res, SWT.NONE).setText(Messages.ChromeAttachTab_runWith);
		browserToUse = new ComboViewer(res, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		browserToUse.setContentProvider(new ArrayContentProvider());
		browserToUse.setLabelProvider(new BrowserLabelProvider());
		proposals = new LinkedList<>();
		proposals.add(""); //$NON-NLS-1$
		proposals.addAll(BrowserManager.getInstance().getWebBrowsers().stream().filter(ChromeExecutableTab::isChrome).toList());
		browserToUse.setInput(proposals);
		browserToUse.addPostSelectionChangedListener(e -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		});

		Link link = new Link(res, SWT.NONE);
		link.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		link.setText(Messages.ChromeAttachTab_browserPreferences);
		link.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getDescription(AccessibleEvent event) {
				event.result = link.getText();
			}
		});
		link.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			Dialog dialog = PreferencesUtil.createPreferenceDialogOn(link.getShell(), "org.eclipse.ui.browser.preferencePage", null, null); //$NON-NLS-1$
			dialog.open();
			List<IBrowserDescriptor> previous = proposals.stream().filter(IBrowserDescriptor.class::isInstance).map(IBrowserDescriptor.class::cast).toList();
			List<IBrowserDescriptor> next = BrowserManager.getInstance().getWebBrowsers();
			List<IBrowserDescriptor> toRemove = new LinkedList<>(previous);
			toRemove.removeAll(next);
			proposals.removeAll(toRemove);
			List<IBrowserDescriptor> toAdd = new LinkedList<>(next);
			toAdd.removeAll(previous);
			toAdd.removeIf(browser -> !isChrome(browser));
			proposals.addAll(toAdd);
			if (!(toAdd.isEmpty() && toRemove.isEmpty())) {
				browserToUse.refresh();
				if (browserToUse.getSelection().isEmpty()) {
					browserToUse.setSelection(new StructuredSelection("")); //$NON-NLS-1$
				}
			}
		}));
		
		setControl(res);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.removeAttribute(ChromeRunDAPDebugDelegate.RUNTIME_EXECUTABLE);
	}


	public static boolean isChrome(IBrowserDescriptor desc) {
		return desc != null && (desc.getName().toLowerCase().contains("chrom") || (desc.getLocation() != null && desc.getLocation().toLowerCase().contains("chrom")));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String browserLocation = configuration.getAttribute(ChromeRunDAPDebugDelegate.RUNTIME_EXECUTABLE, "");
			if (browserLocation.isEmpty()) {
				browserToUse.setSelection(new StructuredSelection(browserLocation));
			} else {
				Optional<IBrowserDescriptor> desc = proposals.stream() //
						.filter(IBrowserDescriptor.class::isInstance) //
						.map(IBrowserDescriptor.class::cast) //
						.filter(it -> browserLocation.equals(it.getLocation())) //
						.findFirst();
				if (desc.isPresent()) {
					desc.ifPresent(it -> browserToUse.setSelection(new StructuredSelection(it)));
				} else {
					if (!proposals.contains(browserLocation)) {
						proposals.add(browserLocation);
					}
					browserToUse.refresh();
					browserToUse.setSelection(new StructuredSelection(browserLocation));
				}
			}
		} catch (CoreException ex) {
			Activator.getDefault().getLog().log(ex.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		Object selectedBrowser = ((IStructuredSelection)browserToUse.getSelection()).getFirstElement();
		if (selectedBrowser instanceof IBrowserDescriptor desc) {
			configuration.setAttribute(ChromeRunDAPDebugDelegate.RUNTIME_EXECUTABLE, desc.getLocation());
		} else if (selectedBrowser instanceof String) {
			configuration.setAttribute(ChromeRunDAPDebugDelegate.RUNTIME_EXECUTABLE, selectedBrowser);
		}
	}

	@Override
	public String getName() {
		return Messages.ChromeAttachTab_browserTab;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
		}
		image = null;
	}

	static class BrowserLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof IBrowserDescriptor browser) {
				return browser.getName();
			}
			if ("".equals(element)) {
				return "[Default]";
			}
			return super.getText(element);
		}
	}

}
