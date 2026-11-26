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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants;

/**
 * Dialog to select a grammar file (XSD, DTD, RelaxNG) and a binding type
 * (standard, xml-model). Supports both local workspace files and remote URLs.
 *
 * @author Angelo ZERR
 */
public class AssociateGrammarDialog extends TitleAreaDialog {

	/** Preference key for the "Don't ask again" toggle on the download prompt. */
	public static final String PREF_ENABLE_DOWNLOAD_PROMPT = Activator.PLUGIN_ID + ".associateGrammar.enableDownloadPrompt"; //$NON-NLS-1$

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

	public static enum GrammarLocationType {
		local, remote;
	}

	private final IPath xmlFilePath;
	private final IContainer xmlResourceContainer;

	private Button localRadio;
	private Button remoteRadio;
	private Text localGrammarURIText;
	private Button browseButton;
	private Text remoteGrammarURIText;

	private Button standardRadio;
	private Button xmlModelRadio;

	// Output values
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
		getShell().setText(Messages.AssociateGrammarDialog_title);
		setTitle(Messages.AssociateGrammarDialog_title);
		setMessage(Messages.bind(Messages.AssociateGrammarDialog_description, xmlFilePath.lastSegment()));
		localRadio.setSelection(true);
		updateFieldStates();
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(1, false));

		createGrammarSection(container);
		createBindingTypeSection(container);

		return area;
	}

	private void createGrammarSection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.AssociateGrammarDialog_grammar_section);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setLayout(new GridLayout(3, false)); // radio | text | browse

		// Local row
		localRadio = new Button(group, SWT.RADIO);
		localRadio.setText(Messages.AssociateGrammarDialog_type_local);
		localRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (localRadio.getSelection()) {
					updateFieldStates();
					validate();
				}
			}
		});

		localGrammarURIText = new Text(group, SWT.BORDER);
		localGrammarURIText.addModifyListener(e -> {
			updateBindingTypeRadios(getFileExtension(localGrammarURIText.getText()));
			validate();
		});
		localGrammarURIText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		browseButton = new Button(group, SWT.NONE);
		browseButton.setText(Messages.Browse_button);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GrammarFileSelectionDialog dialog = new GrammarFileSelectionDialog(getParentShell(),
						xmlResourceContainer);
				if (dialog.open() == OK) {
					IFile selectedFile = (IFile) dialog.getFirstResult();
					IPath absolutePath = selectedFile.getFullPath().makeRelativeTo(xmlFilePath.removeLastSegments(1));
					localGrammarURIText.setText(absolutePath.toString());
					localGrammarURIText.getDisplay().asyncExec(localGrammarURIText::setFocus);
				}
			}
		});

		// Remote row
		remoteRadio = new Button(group, SWT.RADIO);
		remoteRadio.setText(Messages.AssociateGrammarDialog_type_remote);
		remoteRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (remoteRadio.getSelection()) {
					updateFieldStates();
					validate();
				}
			}
		});

		remoteGrammarURIText = new Text(group, SWT.BORDER);
		remoteGrammarURIText.setMessage("https://www.example.com/schema.xsd"); //$NON-NLS-1$
		remoteGrammarURIText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				remoteGrammarURIText.selectAll();
			}
		});
		remoteGrammarURIText.addModifyListener(e -> {
			updateBindingTypeRadios(getFileExtension(remoteGrammarURIText.getText()));
			validate();
		});
		GridData remoteGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		remoteGd.horizontalSpan = 2; // span browse column
		remoteGrammarURIText.setLayoutData(remoteGd);
	}

	private void createBindingTypeSection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.AssociateGrammarDialog_association_section);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setLayout(new GridLayout(1, false));

		standardRadio = new Button(group, SWT.RADIO);
		standardRadio.setText(BindingType.standard.getLabel());
		standardRadio.setSelection(true);
		standardRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});

		xmlModelRadio = new Button(group, SWT.RADIO);
		xmlModelRadio.setText(BindingType.xmlmodel.getLabel());
		xmlModelRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});
	}

	private void updateFieldStates() {
		boolean isLocal = localRadio.getSelection();
		localGrammarURIText.setEnabled(isLocal);
		browseButton.setEnabled(isLocal);
		remoteGrammarURIText.setEnabled(!isLocal);
		Text activeText = isLocal ? localGrammarURIText : remoteGrammarURIText;
		updateBindingTypeRadios(getFileExtension(activeText.getText()));
		activeText.getDisplay().asyncExec(activeText::setFocus);
	}

	private GrammarLocationType getSelectedGrammarLocationType() {
		return remoteRadio.getSelection() ? GrammarLocationType.remote : GrammarLocationType.local;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void saveInput() {
		if (getSelectedGrammarLocationType() == GrammarLocationType.local) {
			IFile grammarFile = findLocalGrammarFile(localGrammarURIText.getText());
			if (grammarFile != null && grammarFile.exists() && grammarFile.getLocation() != null) {
				grammarURI = LSPEclipseUtils.toUri(grammarFile.getLocation()).toASCIIString();
			} else {
				grammarURI = localGrammarURIText.getText();
			}
		} else {
			grammarURI = remoteGrammarURIText.getText();
		}
		bindingType = standardRadio.getSelection() ? BindingType.standard : BindingType.xmlmodel;
	}

	private void validate() {
		IStatus status = performValidation();
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(status.getSeverity() != IStatus.ERROR);
		}
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			setErrorMessage(status.getMessage());
			break;
		default:
			setErrorMessage(null);
			if (status.isOK()) {
				setMessage(Messages.bind(Messages.AssociateGrammarDialog_description, xmlFilePath.lastSegment()));
			} else {
				setMessage(status.getMessage(), getMessageType(status.getSeverity()));
			}
			break;
		}
	}

	private IStatus performValidation() {
		if (getSelectedGrammarLocationType() == GrammarLocationType.local) {
			return validateLocal();
		}
		return validateRemote();
	}

	private IStatus validateLocal() {
		String uri = localGrammarURIText.getText();
		if (uri.isBlank()) {
			return Status.error(Messages.AssociateGrammarDialog_validation_grammar_file_required);
		}
		IFile file = findLocalGrammarFile(uri);
		if (file == null || !file.exists()) {
			return Status.warning(
					Messages.bind(Messages.AssociateGrammarDialog_validation_grammar_file_notExists, uri));
		}
		if (isRelaxNGGrammarFile(file.getFileExtension())) {
			return Status.OK_STATUS;
		}
		if (!isStandardGramarFile(file.getFileExtension())) {
			return Status.warning(Messages.bind(
					Messages.AssociateGrammarDialog_validation_grammar_file_invalid_fileExtension,
					uri, ALL_GRAMMAR_FILE_EXTENSION.stream().collect(Collectors.joining(", "))));
		}
		return Status.OK_STATUS;
	}

	/**
	 * Resolves a workspace-relative or project-relative grammar path against the
	 * XML file's parent folder. Handles {@code ../} segments via
	 * {@link java.nio.file.Paths#normalize()}.
	 */
	private IFile findLocalGrammarFile(String relativeUri) {
		IFile xmlFile = ResourcesPlugin.getWorkspace().getRoot().getFile(xmlFilePath);
		if (xmlFile == null || !xmlFile.exists() || xmlFile.getLocation() == null) {
			return xmlResourceContainer.getFile(new Path(relativeUri));
		}
		java.nio.file.Path grammarFsPath = xmlFile.getLocation().toFile().getParentFile().toPath()
				.resolve(relativeUri).normalize();
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocationURI(grammarFsPath.toUri());
		return files.length > 0 ? files[0] : null;
	}

	/**
	 * Update binding type radios based on the detected grammar file extension.
	 * For RelaxNG files, Standard is disabled and deselected; xml-model is selected.
	 */
	private void updateBindingTypeRadios(String fileExtension) {
		if (standardRadio == null) {
			return;
		}
		if (isRelaxNGGrammarFile(fileExtension)) {
			standardRadio.setEnabled(false);
			standardRadio.setSelection(false);
			xmlModelRadio.setSelection(true);
		} else {
			standardRadio.setEnabled(true);
			if (!standardRadio.getSelection() && !xmlModelRadio.getSelection()) {
				standardRadio.setSelection(true);
			}
		}
	}

	/**
	 * Returns the file extension from a path or URL string, or null if none.
	 */
	private static String getFileExtension(String pathOrUrl) {
		if (pathOrUrl == null || pathOrUrl.isBlank()) {
			return null;
		}
		int dot = pathOrUrl.lastIndexOf('.');
		int slash = Math.max(pathOrUrl.lastIndexOf('/'), pathOrUrl.lastIndexOf('\\'));
		if (dot > slash && dot < pathOrUrl.length() - 1) {
			return pathOrUrl.substring(dot + 1);
		}
		return null;
	}

	private IStatus validateRemote() {
		String url = remoteGrammarURIText.getText();
		if (url.isBlank()) {
			return Status.error(Messages.AssociateGrammarDialog_validation_grammar_url_required);
		}
		try {
			URI uri = new URI(url);
			if (!uri.isAbsolute()) {
				return Status.error(
						Messages.bind(Messages.AssociateGrammarDialog_validation_grammar_url_invalid, url));
			}
			uri.toURL();
		} catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
			return Status.error(
					Messages.bind(Messages.AssociateGrammarDialog_validation_grammar_url_invalid, url));
		}
		if (!isDownloadExternalResourcesEnabled()) {
			return Status.warning(Messages.AssociateGrammarDialog_validation_download_external_disabled);
		}
		return Status.OK_STATUS;
	}

	private static boolean isDownloadExternalResourcesEnabled() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
				XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId);
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
	 * Returns the grammar URI to associate (workspace-relative path or remote URL).
	 */
	public String getGrammarURI() {
		return grammarURI;
	}

	/**
	 * Returns the binding type to use for association.
	 */
	public BindingType getBindingType() {
		return bindingType;
	}

	@Override
	protected void okPressed() {
		if (getSelectedGrammarLocationType() == GrammarLocationType.remote
				&& !isDownloadExternalResourcesEnabled()) {
			String dontAsk = Activator.getDefault().getPreferenceStore().getString(PREF_ENABLE_DOWNLOAD_PROMPT);
			if (MessageDialogWithToggle.ALWAYS.equals(dontAsk)) {
				Activator.getDefault().getPreferenceStore().setValue(
						XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId, true);
			} else if (!MessageDialogWithToggle.NEVER.equals(dontAsk)) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(
						getShell(),
						Messages.AssociateGrammarDialog_download_prompt_title,
						Messages.AssociateGrammarDialog_download_prompt_message,
						Messages.AssociateGrammarDialog_download_prompt_toggle,
						false,
						Activator.getDefault().getPreferenceStore(),
						PREF_ENABLE_DOWNLOAD_PROMPT);
				int code = dialog.getReturnCode();
				if (code == IDialogConstants.CANCEL_ID) {
					return;
				}
				if (code == IDialogConstants.YES_ID) {
					Activator.getDefault().getPreferenceStore().setValue(
							XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId,
							true);
				}
			}
			// NEVER → proceed without enabling download
		}
		saveInput();
		super.okPressed();
	}

	/**
	 * File selection dialog restricted to grammar extensions (xsd, dtd, rng, rnc).
	 * Subclasses {@link FilteredResourcesSelectionDialog} to call the protected
	 * {@code addListFilter} from within the class hierarchy.
	 */
	private static final class GrammarFileSelectionDialog extends FilteredResourcesSelectionDialog {

		GrammarFileSelectionDialog(Shell shell, IContainer container) {
			super(shell, false, container, IResource.FILE);
			setInitialPattern("*."); //$NON-NLS-1$
			addListFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (element instanceof IFile file) {
						return ALL_GRAMMAR_FILE_EXTENSION.contains(file.getFileExtension());
					}
					return true;
				}
			});
		}
	}

}
