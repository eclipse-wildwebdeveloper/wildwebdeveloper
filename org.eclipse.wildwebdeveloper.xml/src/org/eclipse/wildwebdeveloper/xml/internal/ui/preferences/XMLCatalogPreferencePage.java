/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

public class XMLCatalogPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Set<File> entries;
	private File selectedEntry;
	private boolean isDirty;
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		isDirty = false;
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Group entriesGroup = new Group(composite, SWT.NONE);
		entriesGroup.setText(Messages.XMLCatalogPreferencePage_Entries);
		GridLayout gl = new GridLayout(2, false);
		entriesGroup.setLayout(gl);
		entriesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		TreeViewer viewer = new TreeViewer(entriesGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new EntriesContentProvider());
		viewer.setLabelProvider(new EntriesLabelProvider());
		
		entries = XMLCatalogs.getAllCatalogs(getPreferenceStore());

		viewer.setInput(entries);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.expandAll();

		Composite buttonComposite = new Composite(entriesGroup, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(Messages.Add_button);
		addButton.addSelectionListener(widgetSelectedAdapter(e -> {
			File result = openSelectFileDialog();
			if (result == null) {
				return;
			}
			entries.add(result);
			viewer.refresh();
			viewer.setSelection(new TreeSelection(new TreePath(new File[] {result})));
			isDirty = true;
		}));
		
		final Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText(Messages.Remove_button);
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(widgetSelectedAdapter(e -> {
			if (selectedEntry != null) {
				entries.remove(selectedEntry);
				viewer.refresh();
				isDirty = true;
			}
		}));

		final Button editButton = new Button(buttonComposite, SWT.PUSH);
		editButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, 
				true, false, 1, 18));
		editButton.setText(Messages.XMLCatalogPreferencePage_Edit);
		editButton.setEnabled(false);
		editButton.addSelectionListener(widgetSelectedAdapter(e -> {
			if (selectedEntry != null) {
				MessageDialog dialog = new MessageDialog(getShell(), 
						Messages.XMLCatalogPreferencePage_OpenInEditorTitle, 
						null, 
						Messages.XMLCatalogPreferencePage_OpenInEditorMessage, 
						MessageDialog.CONFIRM,
						1,
						Messages.XMLCatalogPreferencePage_OpenInEditorApplyAndEdit,
						IDialogConstants.NO_LABEL
						);

				int result = (isDirty ? dialog.open() : IDialogConstants.OK_ID);
				if (result == IDialogConstants.OK_ID) {
					XMLCatalogPreferencePage.this.performOk();
					if (getContainer() instanceof Window window) {
						window.close();
					}
					
					try {
						IDE.openEditor(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
							selectedEntry.toURI(), 
							"org.eclipse.ui.genericeditor.GenericEditor",
							true);
					} catch (PartInitException e1) {
						ILog.get().error(e1.getMessage(), e1);
					}
				}
			}
		}));
		
		viewer.addSelectionChangedListener(event -> {
			removeButton.setEnabled(!event.getSelection().isEmpty());
			editButton.setEnabled(!event.getSelection().isEmpty());
			selectedEntry = null;
			ISelection selection = event.getSelection();
			if (selection instanceof ITreeSelection treeSelection) {
				Object object = treeSelection.getFirstElement();
				if (object instanceof File file) {
					selectedEntry = file;
				}
			}
		});

		return composite;
	}

	@Override
	public boolean performOk() {
		XMLCatalogs.storeUserCatalogs(getPreferenceStore(), entries);
		isDirty = false;
		return super.performOk();
	}

	private File openSelectFileDialog() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
		dialog.setFilterExtensions(new String[] {"*.xml"});
		dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toString());
		String fileName = dialog.open();
		return fileName == null || fileName.trim().length() == 0 ? null : new File(fileName);
	}
	
	class EntriesContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Set<?> set) {
				return set.toArray();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element instanceof Set;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class EntriesLabelProvider extends LabelProvider {
		private Image image;

		public EntriesLabelProvider() {
			try (InputStream imageResource = getClass().getResourceAsStream("/icons/xmlEditorIcon.png")) {
				image = new Image(Display.getDefault(), imageResource);
			} catch (IOException e) {
				ILog.get().error(e.getMessage(), e);
			}
		}
		
		@Override
		public Image getImage(Object element) {
			return image;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof File file) {
				return file.getAbsolutePath();
			}
			return super.getText(element);
		}
		
		protected final IWorkbenchAdapter getAdapter(Object o) {
			return Adapters.adapt(o, IWorkbenchAdapter.class);
		}
		
		@Override
		public void dispose() {
			if (image != null) {
				image.dispose();
			}
			image = null;
		}
	}
}
