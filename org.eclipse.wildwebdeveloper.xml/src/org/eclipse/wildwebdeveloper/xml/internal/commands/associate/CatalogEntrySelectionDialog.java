/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.IStyledStringHighlighter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;
import org.eclipse.wildwebdeveloper.xml.internal.ui.Messages;

/**
 * Quick-open style dialog for selecting a bindable catalog entry.
 * If loading the entries fails, the dialog is closed and the error is shown with
 * the standard workbench error dialog (with expandable stack trace) and logged.
 * </p>
 */
public class CatalogEntrySelectionDialog extends FilteredItemsSelectionDialog {

	private static final String DIALOG_SETTINGS_ID = "CatalogEntrySelectionDialog"; //$NON-NLS-1$

	private final CompletableFuture<List<BindableCatalogEntry>> entriesFuture;
	private List<BindableCatalogEntry> entries = Collections.emptyList();
	private ProgressBar progressBar;
	private volatile ItemsFilter currentFilter;

	public CatalogEntrySelectionDialog(Shell shell, CompletableFuture<List<BindableCatalogEntry>> entriesFuture) {
		super(shell);
		this.entriesFuture = entriesFuture;
		setTitle(Messages.CatalogEntrySelectionDialog_title);
		setListLabelProvider(createListLabelProvider());
		setDetailsLabelProvider(new DetailLabelProvider());
		setInitialPattern("**", FULL_SELECTION); //$NON-NLS-1$
	}

	@Override
	public int open() {
		// Future already failed: don't even open the dialog, just report the error.
		if (entriesFuture.isCompletedExceptionally()) {
			try {
				entriesFuture.get();
			} catch (Exception ex) {
				reportError(ex);
			}
			return CANCEL;
		}
		return super.open();
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		boolean done = entriesFuture.isDone();
		progressBar = new ProgressBar(parent, SWT.INDETERMINATE);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.exclude = done;
		progressBar.setLayoutData(data);
		progressBar.setVisible(!done);
		return progressBar;
	}

	@Override
	public void create() {
		super.create();
		// Handle completion (waiting or already done)
		if (!entriesFuture.isDone()) {
			entriesFuture.whenComplete((loadedEntries, ex) -> {
				getShell().getDisplay().asyncExec(() -> handleCompletion(loadedEntries, ex));
			});
		} else {
			try {
				handleCompletion(entriesFuture.get(), null);
			} catch (Exception ex) {
				handleCompletion(null, ex);
			}
		}
	}

	/** Handles future completion: displays entries, or closes the dialog and reports the error. */
	private void handleCompletion(List<BindableCatalogEntry> loadedEntries, Throwable ex) {
		Shell shell = getShell();
		if (shell == null || shell.isDisposed()) {
			return;
		}
		if (ex != null) {
			Display display = shell.getDisplay();
			close(); // closes the selection dialog (Cancel)
			display.asyncExec(() -> reportError(ex));
			return;
		}
		progressBar.setVisible(false);
		((GridData) progressBar.getLayoutData()).exclude = true;
		progressBar.getParent().layout(true, true);
		entries = loadedEntries;
		setListLabelProvider(createListLabelProvider());
		scheduleRefresh();
	}

	/** Shows the standard workbench error dialog (stack trace in "Details") and logs the error. */
	private static void reportError(Throwable ex) {
		IStatus status = Status.error(Messages.CatalogEntrySelectionDialog_loadError, unwrap(ex));
		StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
	}

	/** Unwraps CompletionException / ExecutionException to get the real error. */
	private static Throwable unwrap(Throwable ex) {
		while ((ex instanceof CompletionException || ex instanceof ExecutionException) && ex.getCause() != null) {
			ex = ex.getCause();
		}
		return ex;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_ID);
		}
		return section;
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		ItemsFilter filter = new ItemsFilter() {
			@Override
			public boolean matchItem(Object item) {
				if (item instanceof BindableCatalogEntry entry) {
					return matchesAll()
							|| matches(entry.identifier)
							|| matches(catalogFileName(entry.catalogURI));
				}
				return false;
			}

			/** The initial pattern ("**") or an empty pattern must list everything. */
			private boolean matchesAll() {
				String pattern = getPattern();
				return pattern == null || pattern.isBlank() || pattern.chars().allMatch(c -> c == '*');
			}

			@Override
			public boolean isConsistentItem(Object item) {
				return item instanceof BindableCatalogEntry;
			}
		};
		currentFilter = filter;
		return filter;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Comparator getItemsComparator() {
		return Comparator.comparing((BindableCatalogEntry e) -> e.identifier, String.CASE_INSENSITIVE_ORDER);
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException {
		// This runs in a background job: wait for the entries instead of reading
		// a field that may still be empty when the first refresh happens.
		List<BindableCatalogEntry> loaded = awaitEntries(progressMonitor);
		progressMonitor.beginTask(null, loaded.size());
		for (BindableCatalogEntry entry : loaded) {
			contentProvider.add(entry, itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.done();
	}

	/**
	 * Waits for the entries, polling so that the job stays cancellable. On failure
	 * or cancellation returns an empty list (the error itself is reported by
	 * {@link #handleCompletion}).
	 */
	private List<BindableCatalogEntry> awaitEntries(IProgressMonitor monitor) {
		while (!monitor.isCanceled()) {
			try {
				return entriesFuture.get(100, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				// not ready yet, keep waiting
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) { // ExecutionException, CancellationException
				break;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String getElementName(Object item) {
		if (item instanceof BindableCatalogEntry entry) {
			return entry.identifier;
		}
		return ""; //$NON-NLS-1$
	}

	/** Returns the entry the user selected, or {@code null} if cancelled. */
	public BindableCatalogEntry getSelectedEntry() {
		Object[] result = getResult();
		if (result != null && result.length > 0 && result[0] instanceof BindableCatalogEntry entry) {
			return entry;
		}
		return null;
	}

	static String catalogFileName(String uri) {
		if (uri == null || uri.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		int slash = Math.max(uri.lastIndexOf('/'), uri.lastIndexOf('\\'));
		return slash >= 0 ? uri.substring(slash + 1) : uri;
	}

	/** Wraps the catalog label provider so that the text matching the pattern is bold. */
	private ILabelProvider createListLabelProvider() {
		return new MatchHighlightLabelProvider(new CatalogEntryLabelProvider(entries), () -> {
			ItemsFilter filter = currentFilter;
			return filter != null ? filter.getPattern() : null;
		}, this::getStyledStringHighlighter);
	}

	/** Decorates a label provider: the parts of the label matching the filter pattern are shown in bold. */
	private static final class MatchHighlightLabelProvider extends LabelProvider implements IStyledLabelProvider {

		private final ILabelProvider delegate;
		private final Supplier<String> patternSupplier;
		private final Supplier<IStyledStringHighlighter> highlighterSupplier;
		private final StyledString.Styler boldStyler = new StyledString.Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
			}
		};

		MatchHighlightLabelProvider(ILabelProvider delegate, Supplier<String> patternSupplier,
				Supplier<IStyledStringHighlighter> highlighterSupplier) {
			this.delegate = delegate;
			this.patternSupplier = patternSupplier;
			this.highlighterSupplier = highlighterSupplier;
		}

		@Override
		public Image getImage(Object element) {
			return delegate.getImage(element);
		}

		@Override
		public String getText(Object element) {
			return delegate.getText(element);
		}

		@Override
		public StyledString getStyledText(Object element) {
			StyledString result;
			if (delegate instanceof IStyledLabelProvider styled && styled.getStyledText(element) != null) {
				result = styled.getStyledText(element);
			} else {
				String text = delegate.getText(element);
				result = new StyledString(text != null ? text : ""); //$NON-NLS-1$
			}
			String pattern = patternSupplier.get();
			IStyledStringHighlighter highlighter = highlighterSupplier.get();
			if (highlighter != null && pattern != null) {
				// Same highlighter as the Open Resource dialog (handles wildcards, camelCase...)
				StyledString highlighted = highlighter.highlight(result.getString(), pattern, boldStyler);
				if (highlighted != null) {
					for (StyleRange range : highlighted.getStyleRanges()) {
						result.setStyle(range.start, range.length, boldStyler);
					}
				}
			} else {
				for (int[] region : matchingRegions(result.getString(), pattern)) {
					result.setStyle(region[0], region[1], boldStyler);
				}
			}
			return result;
		}

		/**
		 * Returns {offset, length} regions of {@code text} matching the literal parts
		 * of {@code pattern} (wildcards '*' and '?' and spaces separate the parts).
		 */
		private static List<int[]> matchingRegions(String text, String pattern) {
			List<int[]> regions = new ArrayList<>();
			if (text == null || pattern == null) {
				return regions;
			}
			String lowerText = text.toLowerCase();
			int from = 0;
			for (String token : pattern.toLowerCase().split("[*?\\s]+")) { //$NON-NLS-1$
				if (token.isEmpty()) {
					continue;
				}
				int index = lowerText.indexOf(token, from);
				if (index < 0) {
					continue;
				}
				regions.add(new int[] { index, token.length() });
				from = index + token.length();
			}
			return regions;
		}

		@Override
		public void dispose() {
			delegate.dispose();
			super.dispose();
		}
	}

	/** Shows the catalog filename and resolved target URI in the details pane. */
	private static final class DetailLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof BindableCatalogEntry entry) {
				String catalog = catalogFileName(entry.catalogURI);
				String target = entry.targetURI != null ? entry.targetURI : ""; //$NON-NLS-1$
				return catalog.isEmpty() ? target : catalog + " — " + target; //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}
	}
}