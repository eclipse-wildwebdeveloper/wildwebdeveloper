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
package org.eclipse.wildwebdeveloper.xml.internal.commands.associate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants;

/**
 * Dialog to select a grammar file (XSD, DTD, RelaxNG) and a binding type
 * (standard, xml-model). Supports local workspace files, remote URLs, and
 * catalog-based binding.
 *
 * @author Angelo ZERR
 */
public class AssociateGrammarDialog extends TitleAreaDialog {

	/** Preference key for the "Don't ask again" toggle on the download prompt. */
	public static final String PREF_ENABLE_DOWNLOAD_PROMPT = Activator.PLUGIN_ID + ".associateGrammar.enableDownloadPrompt"; //$NON-NLS-1$

	static final List<String> W3C_EXTENSIONS = Arrays.asList("xsd", "dtd"); //$NON-NLS-1$ //$NON-NLS-2$
	static final List<String> RELAXNG_EXTENSIONS = Arrays.asList("rng", "rnc"); //$NON-NLS-1$ //$NON-NLS-2$
	static final List<String> ALL_GRAMMAR_EXTENSIONS = Arrays.asList("xsd", "dtd", "rng", "rnc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public enum BindingType {

		standard("standard", "Standard (xsi, DOCTYPE)"), //$NON-NLS-1$
		xmlmodel("xml-model", "XML Model association"); //$NON-NLS-1$

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

	public enum GrammarLocationType {
		local, remote, catalog
	}

	private final IPath xmlFilePath;
	private final IContainer xmlResourceContainer;
	private final Supplier<CompletableFuture<List<BindableCatalogEntry>>> catalogEntriesSupplier;
	/** Cached future — set on first Browse click, reused on subsequent clicks within this dialog. */
	private CompletableFuture<List<BindableCatalogEntry>> catalogEntriesFuture;
	private List<BindableCatalogEntry> catalogEntries = Collections.emptyList();

	// Grammar section widgets
	private Button localRadio;
	private Button remoteRadio;
	private Button catalogRadio;
	private Text localGrammarURIText;
	private Button browseLocalButton;
	private Text remoteGrammarURIText;
	private Text selectedCatalogText;
	private Button browseCatalogButton;

	// Binding section widgets
	private Group bindingTypeGroup;
	private Button standardRadio;
	private Button xmlModelRadio;

	// Output values — saved in saveInput() while widgets are still alive
	private boolean catalogMode;
	private String grammarURI;
	private BindingType bindingType;
	private BindableCatalogEntry selectedCatalogEntry;

	public AssociateGrammarDialog(Shell parentShell, IPath xmlFilePath,
			Supplier<CompletableFuture<List<BindableCatalogEntry>>> catalogEntriesSupplier) {
		super(parentShell);
		this.xmlFilePath = xmlFilePath;
		this.xmlResourceContainer = resolveResourceContainer(xmlFilePath);
		this.catalogEntriesSupplier = catalogEntriesSupplier;
	}

	private static IContainer resolveResourceContainer(IPath xmlFilePath) {
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
		group.setLayout(new GridLayout(3, false)); // col: radio | text | browse

		createLocalRow(group);
		createRemoteRow(group);
		createCatalogRow(group);
	}

	private void createLocalRow(Composite parent) {
		localRadio = new Button(parent, SWT.RADIO);
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

		localGrammarURIText = new Text(parent, SWT.BORDER);
		localGrammarURIText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		localGrammarURIText.addModifyListener(e -> {
			updateBindingTypeRadios(getFileExtension(localGrammarURIText.getText()));
			validate();
		});

		browseLocalButton = new Button(parent, SWT.NONE);
		browseLocalButton.setText(Messages.Browse_button);
		browseLocalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GrammarFileSelectionDialog dialog = new GrammarFileSelectionDialog(getParentShell(),
						xmlResourceContainer, ALL_GRAMMAR_EXTENSIONS);
				if (dialog.open() == OK) {
					IFile selectedFile = (IFile) dialog.getFirstResult();
					IPath relativePath = selectedFile.getFullPath()
							.makeRelativeTo(xmlFilePath.removeLastSegments(1));
					localGrammarURIText.setText(relativePath.toString());
					localGrammarURIText.getDisplay().asyncExec(localGrammarURIText::setFocus);
				}
			}
		});
	}

	private void createRemoteRow(Composite parent) {
		remoteRadio = new Button(parent, SWT.RADIO);
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

		remoteGrammarURIText = new Text(parent, SWT.BORDER);
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
		remoteGd.horizontalSpan = 2;
		remoteGrammarURIText.setLayoutData(remoteGd);
	}

	private void createCatalogRow(Composite parent) {
		catalogRadio = new Button(parent, SWT.RADIO);
		catalogRadio.setText(Messages.AssociateGrammarDialog_type_catalog);
		catalogRadio.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		catalogRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (catalogRadio.getSelection()) {
					updateFieldStates();
					validate();
				}
			}
		});

		selectedCatalogText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		selectedCatalogText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		selectedCatalogText.setMessage(Messages.AssociateGrammarDialog_catalog_no_selection);

		browseCatalogButton = new Button(parent, SWT.NONE);
		browseCatalogButton.setText(Messages.Browse_button);
		browseCatalogButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browseCatalogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (catalogEntriesFuture == null) {
					catalogEntriesFuture = catalogEntriesSupplier.get();
				}
				CatalogEntrySelectionDialog dialog = new CatalogEntrySelectionDialog(getShell(), catalogEntriesFuture);
				if (dialog.open() == OK) {
					selectedCatalogEntry = dialog.getSelectedEntry();
					if (selectedCatalogEntry != null) {
						catalogEntriesFuture.thenAccept(entries -> {
							getShell().getDisplay().asyncExec(() -> {
								if (!getShell().isDisposed()) {
									selectedCatalogText.setText(new CatalogEntryLabelProvider(entries).getText(selectedCatalogEntry));
									validate();
								}
							});
						});
					}
				}
			}
		});
	}

	private void createBindingTypeSection(Composite parent) {
		bindingTypeGroup = new Group(parent, SWT.NONE);
		bindingTypeGroup.setText(Messages.AssociateGrammarDialog_association_section);
		bindingTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		bindingTypeGroup.setLayout(new GridLayout(1, false));

		standardRadio = new Button(bindingTypeGroup, SWT.RADIO);
		standardRadio.setText(BindingType.standard.getLabel());
		standardRadio.setSelection(true);
		standardRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});

		xmlModelRadio = new Button(bindingTypeGroup, SWT.RADIO);
		xmlModelRadio.setText(BindingType.xmlmodel.getLabel());
		xmlModelRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});
	}

	private void updateFieldStates() {
		GrammarLocationType type = getSelectedGrammarLocationType();
		boolean isLocal = type == GrammarLocationType.local;
		boolean isRemote = type == GrammarLocationType.remote;
		boolean isCatalog = type == GrammarLocationType.catalog;

		localGrammarURIText.setEnabled(isLocal);
		browseLocalButton.setEnabled(isLocal);
		remoteGrammarURIText.setEnabled(isRemote);
		selectedCatalogText.setEnabled(isCatalog);
		browseCatalogButton.setEnabled(isCatalog);

		// Hide binding type section in catalog mode (binding is implicit in entry type)
		setGridVisible(bindingTypeGroup, !isCatalog);
		bindingTypeGroup.getParent().layout(true, true);

		if (!isCatalog) {
			Text activeText = isLocal ? localGrammarURIText : remoteGrammarURIText;
			updateBindingTypeRadios(getFileExtension(activeText.getText()));
			activeText.getDisplay().asyncExec(activeText::setFocus);
		} else {
			browseCatalogButton.getDisplay().asyncExec(browseCatalogButton::setFocus);
		}
	}

	/** Toggles visibility and excludes the control from grid layout when hidden. */
	private static void setGridVisible(Control control, boolean visible) {
		if (control.getLayoutData() instanceof GridData gd) {
			gd.exclude = !visible;
		}
		control.setVisible(visible);
	}

	private GrammarLocationType getSelectedGrammarLocationType() {
		if (catalogRadio != null && catalogRadio.getSelection()) {
			return GrammarLocationType.catalog;
		}
		return remoteRadio != null && remoteRadio.getSelection()
				? GrammarLocationType.remote
				: GrammarLocationType.local;
	}

	/**
	 * Returns true if the user chose to bind via a catalog entry.
	 * Safe to call after the dialog is closed.
	 */
	public boolean isCatalogMode() {
		return catalogMode;
	}

	/** Returns the selected catalog entry, or null if not in catalog mode. */
	public BindableCatalogEntry getSelectedCatalogEntry() {
		return selectedCatalogEntry;
	}

	/** Returns the grammar URI (local path or remote URL), or null in catalog mode. */
	public String getGrammarURI() {
		return grammarURI;
	}

	/** Returns the binding type, or null in catalog mode. */
	public BindingType getBindingType() {
		return bindingType;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/** Saves all output fields while widgets are still alive (before super.okPressed disposes them). */
	private void saveInput() {
		this.catalogMode = getSelectedGrammarLocationType() == GrammarLocationType.catalog;
		if (catalogMode) {
			// selectedCatalogEntry is already set when the user picked from the browse dialog
			return;
		}
		if (getSelectedGrammarLocationType() == GrammarLocationType.local) {
			IFile grammarFile = findLocalGrammarFile(localGrammarURIText.getText());
			if (grammarFile != null && grammarFile.exists() && grammarFile.getLocationURI() != null) {
				grammarURI = grammarFile.getLocationURI().toString();
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
		if (status.getSeverity() == IStatus.ERROR) {
			setErrorMessage(status.getMessage());
		} else {
			setErrorMessage(null);
			if (status.isOK()) {
				setMessage(Messages.bind(Messages.AssociateGrammarDialog_description, xmlFilePath.lastSegment()));
			} else {
				setMessage(status.getMessage(), toMessageType(status.getSeverity()));
			}
		}
	}

	private IStatus performValidation() {
		return switch (getSelectedGrammarLocationType()) {
		case local -> validateLocal();
		case remote -> validateRemote();
		case catalog -> validateCatalog();
		};
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
		if (RELAXNG_EXTENSIONS.contains(file.getFileExtension())) {
			return Status.OK_STATUS;
		}
		if (!W3C_EXTENSIONS.contains(file.getFileExtension())) {
			String allowed = ALL_GRAMMAR_EXTENSIONS.stream().collect(Collectors.joining(", ")); //$NON-NLS-1$
			return Status.warning(Messages.bind(
					Messages.AssociateGrammarDialog_validation_grammar_file_invalid_fileExtension, uri, allowed));
		}
		return Status.OK_STATUS;
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

	private IStatus validateCatalog() {
		if (selectedCatalogEntry == null) {
			return Status.error(Messages.AssociateGrammarDialog_validation_catalog_entry_required);
		}
		return Status.OK_STATUS;
	}

	@Override
	protected void okPressed() {
		if (getSelectedGrammarLocationType() == GrammarLocationType.remote
				&& !isDownloadExternalResourcesEnabled()) {
			String dontAsk = Activator.getDefault().getPreferenceStore().getString(PREF_ENABLE_DOWNLOAD_PROMPT);
			if (MessageDialogWithToggle.ALWAYS.equals(dontAsk)) {
				enableDownloadExternalResources();
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
					enableDownloadExternalResources();
				}
			}
			// NEVER → proceed without enabling download
		}
		saveInput(); // must be called before super.okPressed() disposes widgets
		super.okPressed();
	}

	// ---- Helpers -------------------------------------------------------------

	/**
	 * Resolves a workspace-relative grammar path against the XML file's parent
	 * folder. Handles {@code ../} segments via {@link java.nio.file.Path#normalize()}.
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
	 * Updates binding type radio buttons based on the detected grammar extension.
	 * RelaxNG files force xml-model; standard is disabled for them.
	 */
	private void updateBindingTypeRadios(String fileExtension) {
		if (standardRadio == null) {
			return;
		}
		if (RELAXNG_EXTENSIONS.contains(fileExtension)) {
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

	private static boolean isDownloadExternalResourcesEnabled() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
				XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId);
	}

	private static void enableDownloadExternalResources() {
		Activator.getDefault().getPreferenceStore().setValue(
				XMLPreferenceServerConstants.XML_PREFERENCES_DOWNLOAD_EXTERNAL_RESOURCES.preferenceId, true);
	}

	private static int toMessageType(int severity) {
		return switch (severity) {
		case IStatus.INFO -> IMessageProvider.INFORMATION;
		case IStatus.WARNING -> IMessageProvider.WARNING;
		case IStatus.ERROR -> IMessageProvider.ERROR;
		default -> IMessageProvider.NONE;
		};
	}
}
