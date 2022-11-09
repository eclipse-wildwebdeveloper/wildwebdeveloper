package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
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

public class AssociateGrammarDialog extends TitleAreaDialog {

	private final List<String> W3C_GRAMMAR_FILE_EXTENSION = Arrays.asList("xsd", "dtd");

	private final List<String> RELAXNG_GRAMMAR_FILE_EXTENSION = Arrays.asList("rng", "rnc");

	private final List<String> ALL_GRAMMAR_FILE_EXTENSION = Stream
			.concat(W3C_GRAMMAR_FILE_EXTENSION.stream(), RELAXNG_GRAMMAR_FILE_EXTENSION.stream()).toList();

	private final static String MESSAGE4 = "The grammar file is required.";

	private final static String MESSAGE = "The grammar file ''{0}'' doesn''t exists.";

	private final static String MESSAGE2 = "The grammar file ''{0}'' should have ''{1}'' file extension.";

	private final static String MESSAGE3 = "The RelaxNG grammar file ''{0}'' cannot be associated with ''{1}'' type.";

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

	private final IPath filePath;
	private final IContainer resourceContainer;

	private Text grammarURIText;
	private ComboViewer bindingTypeViewer;
	private String grammarURI;
	private BindingType bindingType;

	public AssociateGrammarDialog(Shell parentShell, IPath filePath, IContainer resourceContainer) {
		super(parentShell);
		this.filePath = filePath;
		this.resourceContainer = resourceContainer;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Associate grammar");
		setMessage("Fill the XSD, DTD, RelaxNG grammar URI", IMessageProvider.INFORMATION);
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);

		createFirstName(container);
		createLastName(container);
		return area;
	}

	private void createFirstName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Grammar URI");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		grammarURIText = new Text(container, SWT.BORDER);
		grammarURIText.addModifyListener(e -> validate());
		grammarURIText.setLayoutData(dataFirstName);

		Button browseButton = new Button(container, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(getParentShell(), false,
						resourceContainer, IResource.FILE);
				dialog.setInitialPattern("*.");
				if (dialog.open() == OK) {
					IFile selectedFile = (IFile) dialog.getFirstResult();
					IPath absolutePath = selectedFile.getFullPath().makeRelativeTo(filePath.removeLastSegments(1));
					grammarURIText.setText(absolutePath.toString());
				}
			}
		});
	}

	private void createLastName(Composite container) {
		Label lbtLastName = new Label(container, SWT.NONE);
		lbtLastName.setText("Association type:");

		GridData dataLastName = new GridData();
		dataLastName.grabExcessHorizontalSpace = true;
		dataLastName.horizontalAlignment = GridData.FILL;
		dataLastName.horizontalSpan = 2;
		bindingTypeViewer = new ComboViewer(container);
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
		bindingTypeViewer.getCombo().setLayoutData(dataLastName);
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

	private void validate() {
		try {
			String grammarURI = grammarURIText.getText();
			if (grammarURI.isBlank()) {
				setErrorMessage(MESSAGE4);
				return;
			}
			IFile file = resourceContainer.getFile(new Path(grammarURI));
			if (file == null || !file.exists()) {
				setMessage(MessageFormat.format(MESSAGE, grammarURI), IMessageProvider.WARNING);
				return;
			} else if (isRelaxNGGrammarFile(file.getFileExtension())) {
				BindingType bindingType = (BindingType) bindingTypeViewer.getStructuredSelection().getFirstElement();
				if (bindingType == BindingType.standard) {
					setErrorMessage(MessageFormat.format(MESSAGE3, grammarURI, BindingType.standard.getLabel()));
					return;
				}
			} else if (!isStandardGramarFile(file.getFileExtension())) {
				setMessage(
						MessageFormat.format(MESSAGE2, grammarURI,
								ALL_GRAMMAR_FILE_EXTENSION.stream().collect(Collectors.joining(","))),
						IMessageProvider.WARNING);
				return;
			}
			setMessage("OK", IMessageProvider.WARNING);
		} finally {
			super.getButton(IDialogConstants.OK_ID).setEnabled(getErrorMessage() == null);
		}
	}

	private boolean isStandardGramarFile(String fileExtension) {
		return W3C_GRAMMAR_FILE_EXTENSION.contains(fileExtension) || isRelaxNGGrammarFile(fileExtension);
	}

	private boolean isRelaxNGGrammarFile(String fileExtension) {
		return RELAXNG_GRAMMAR_FILE_EXTENSION.contains(fileExtension);
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getGrammarURI() {
		return grammarURI;
	}

	public BindingType getBindingType() {
		return bindingType;
	}
}