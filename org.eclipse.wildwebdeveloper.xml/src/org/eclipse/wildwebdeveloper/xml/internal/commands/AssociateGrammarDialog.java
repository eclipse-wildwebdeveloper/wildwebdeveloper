/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * Dialog to select a grammar file (XSD, DTD, RelaxNG) and a binding type
 * -standard, xml-model)
 * 
 * @author Angelo ZERR
 *
 */
public class AssociateGrammarDialog extends TitleAreaDialog {

	private final static List<String> W3C_GRAMMAR_FILE_EXTENSION = Arrays.asList("xsd", "dtd");

	private final static List<String> RELAXNG_GRAMMAR_FILE_EXTENSION = Arrays.asList("rng", "rnc");

	private final static List<String> ALL_GRAMMAR_FILE_EXTENSION = Stream
			.concat(W3C_GRAMMAR_FILE_EXTENSION.stream(), RELAXNG_GRAMMAR_FILE_EXTENSION.stream()).toList();

	public static enum BindingType {

		standard("standard", "Standard (xsi, DOCTYPE)"), xmlmodel("xml-model", "XML Model association");

		private final String code;
		private final String label;

		private BindingType(String code, String label) {
			this.code = code;
			this.label = label;
		}

		public String getCode() {
			return code;
		}

		public String getLabel() {
			return label;
		}
	}

	private final IPath xmlFilePath;
	private final IContainer xmlResourceContainer;

	private Text grammarURIText;
	private ComboViewer bindingTypeViewer;
	private String grammarURI;
	private BindingType bindingType;

	public AssociateGrammarDialog(Shell parentShell, IPath xmlFilePath) {
		super(parentShell);
		this.xmlFilePath = xmlFilePath;
		this.xmlResourceContainer = getResourceContainer(xmlFilePath);
	}

	private IContainer getResourceContainer(IPath xmlFilePath) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(xmlFilePath);
		if (file != null && file.exists()) {
			return file.getProject();
		}
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.AssociateGrammarDialog_title);
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);

		createGrammarURIField(container);
		createLastName(container);
		return area;
	}

	/**
	 * Create the Grammar text field with a Browse button.
	 * 
	 * @param parent the parent composite.
	 */
	private void createGrammarURIField(Composite parent) {
		// Label
		Label grammarURILabel = new Label(parent, SWT.NONE);
		grammarURILabel.setText(Messages.AssociateGrammarDialog_grammar_field);

		// Text field
		grammarURIText = new Text(parent, SWT.BORDER);
		grammarURIText.addModifyListener(e -> validate());
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		grammarURIText.setLayoutData(data);

		// Browse button
		Button browseButton = new Button(parent, SWT.NONE);
		browseButton.setText(Messages.Browse_button);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(getParentShell(), false,
						xmlResourceContainer, IResource.FILE);
				dialog.setInitialPattern("*.");
				if (dialog.open() == OK) {
					IFile selectedFile = (IFile) dialog.getFirstResult();
					IPath absolutePath = selectedFile.getFullPath().makeRelativeTo(xmlFilePath.removeLastSegments(1));
					grammarURIText.setText(absolutePath.toString());
				}
			}
		});
	}

	/**
	 * Create the association type combo.
	 * 
	 * @param parent the parent composite.
	 */
	private void createLastName(Composite parent) {
		// Label
		Label bindingTypeLabel = new Label(parent, SWT.NONE);
		bindingTypeLabel.setText(Messages.AssociateGrammarDialog_bindingType_field);

		// Combo
		bindingTypeViewer = new ComboViewer(parent);
		bindingTypeViewer.setContentProvider(ArrayContentProvider.getInstance());
		bindingTypeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BindingType) {
					BindingType bindingType = (BindingType) element;
					return bindingType.getLabel();
				}
				return super.getText(element);
			}
		});
		bindingTypeViewer.addSelectionChangedListener(e -> validate());

		GridData daya = new GridData();
		daya.grabExcessHorizontalSpace = true;
		daya.horizontalAlignment = GridData.FILL;
		daya.horizontalSpan = 2;
		bindingTypeViewer.getCombo().setLayoutData(daya);
		bindingTypeViewer.setInput(BindingType.values());
		bindingTypeViewer.setSelection(new StructuredSelection(BindingType.standard));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		grammarURI = grammarURIText.getText();
		bindingType = (BindingType) bindingTypeViewer.getStructuredSelection().getFirstElement();
	}

	/**
	 * Validate fields and display a proper message in the dialog.
	 * 
	 */
	private void validate() {
		IStatus status = performValidation();
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			setErrorMessage(status.getMessage());
			break;
		default:
			setErrorMessage(null);
			if (status.isOK()) {
				setMessage("");
			} else {
				setMessage(status.getMessage(), getMessageType(status.getSeverity()));
			}
			break;
		}
	}

	/**
	 * Validate grammar file text and binding type combo selection.
	 * 
	 * @return the status of the validation.
	 */
	private IStatus performValidation() {
		String grammarURI = grammarURIText.getText();
		// Test if grammar file is filled
		if (grammarURI.isBlank()) {
			return Status.error(Messages.AssociateGrammarDialog_validation_grammar_file_required);
		}
		// Test if grammar file exists
		IFile file = xmlResourceContainer.getFile(new Path(grammarURI));
		if (file == null || !file.exists()) {
			return Status.warning(Messages.bind(Messages.AssociateGrammarDialog_validation_grammar_file_notExists, grammarURI));
		}
		// In case of RelaxNG file, only xml-model association is allowed
		if (isRelaxNGGrammarFile(file.getFileExtension())) {
			BindingType bindingType = (BindingType) bindingTypeViewer.getStructuredSelection().getFirstElement();
			if (bindingType == BindingType.standard) {
				return Status.error(Messages.bind(
								Messages.AssociateGrammarDialog_validation_grammar_file_invalid_association_for_relaxng,
								grammarURI, BindingType.standard.getLabel()));
			}
			return Status.OK_STATUS;
		}
		// Test if selected grammar file is a XSD, DTD or RelaxNG (rnc, rng) file
		if (!isStandardGramarFile(file.getFileExtension())) {
			return Status.warning(Messages.bind(Messages.AssociateGrammarDialog_validation_grammar_file_invalid_fileExtension,
							grammarURI, ALL_GRAMMAR_FILE_EXTENSION.stream().collect(Collectors.joining(", "))));
		}
		return Status.OK_STATUS;
	}

	private static int getMessageType(int severity) {
		switch (severity) {
		case IStatus.INFO:
			return IMessageProvider.INFORMATION;
		case IStatus.WARNING:
			return IMessageProvider.WARNING;
		case IStatus.ERROR:
			return IMessageProvider.ERROR;
		}
		return IMessageProvider.NONE;
	}

	private static boolean isStandardGramarFile(String fileExtension) {
		return W3C_GRAMMAR_FILE_EXTENSION.contains(fileExtension);
	}

	private static boolean isRelaxNGGrammarFile(String fileExtension) {
		return RELAXNG_GRAMMAR_FILE_EXTENSION.contains(fileExtension);
	}

	/**
	 * Returns the grammar file URI to associate.
	 * 
	 * @return the grammar file URI to associate.
	 */
	public String getGrammarURI() {
		return grammarURI;
	}

	/**
	 * Returns the binding type to use for association.
	 * 
	 * @return the binding type to use for association.
	 */
	public BindingType getBindingType() {
		return bindingType;
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

}