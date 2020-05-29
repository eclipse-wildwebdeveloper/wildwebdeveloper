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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
	private TreeViewer viewer;
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
		entriesGroup.setText(Messages.XMLCatalogPreferencesPage_Entries);
		GridLayout gl = new GridLayout(2, false);
		entriesGroup.setLayout(gl);
		entriesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new TreeViewer(entriesGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new EntriesContentProvider());
		viewer.setLabelProvider(new EntriesLabelProvider());
		
		entries = XMLPreferenceInitializer.getCatalogs(getPreferenceStore());

		viewer.setInput(entries);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.expandAll();

		Composite buttonComposite = new Composite(entriesGroup, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(Messages.PreferencesPage_Add);
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
		removeButton.setText(Messages.PreferencesPage_Remove);
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
		editButton.setText(Messages.XMLCatalogPreferencesPage_Edit);
		editButton.setEnabled(false);
		editButton.addSelectionListener(widgetSelectedAdapter(e -> {
			if (selectedEntry != null) {
				MessageDialog dialog = new MessageDialog(getShell(), 
						Messages.XMLCatalogPreferencesPage_OpenInEditorTitle, 
						null, 
						Messages.XMLCatalogPreferencesPage_OpenInEditorMessage, 
						MessageDialog.QUESTION,
						1,
						Messages.XMLCatalogPreferencesPage_OpenInEditorApplyAndEdit,
						Messages.XMLCatalogPreferencesPage_OpenInEditorNo
						);

				int result = (isDirty ? dialog.open() : MessageDialog.OK);
				if (result == MessageDialog.OK) {
					XMLCatalogPreferencePage.this.performOk();
					if (getContainer() instanceof Window) {
						((Window)getContainer()).close();
					}
					
					try {
						IDE.openEditor(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
							selectedEntry.toURI(), 
							"org.eclipse.ui.genericeditor.GenericEditor",
							true);
					} catch (PartInitException e1) {
						Activator.getDefault().getLog().error(e1.getMessage(), e1);
					}
				}
			}
		}));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
				editButton.setEnabled(!event.getSelection().isEmpty());
				selectedEntry = null;
				ISelection selection = event.getSelection();
				if (selection instanceof ITreeSelection) {
					ITreeSelection treeSelection = (ITreeSelection) selection;
					Object object = treeSelection.getFirstElement();
					if (object instanceof File) {
						selectedEntry = (File) object;
					}
				}
			}
		});

		return composite;
	}

	@Override
	public boolean performOk() {
		XMLPreferenceInitializer.storeCatalogs(getPreferenceStore(), entries);
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
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Set) {
				return ((Set) parentElement).toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return element instanceof Set;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class EntriesLabelProvider extends LabelProvider {
		private Image image;

		public EntriesLabelProvider() {
			try (InputStream imageResource = getClass().getResourceAsStream("/icons/xmlEditorIcon.png")) {
				image = new Image(Display.getDefault(), imageResource);
			} catch (IOException e) {
				Activator.getDefault().getLog().error(e.getMessage(), e);
			}
		}
		
		@Override
		public Image getImage(Object element) {
			return image;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof File) {
				return ((File) element).getAbsolutePath();
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
