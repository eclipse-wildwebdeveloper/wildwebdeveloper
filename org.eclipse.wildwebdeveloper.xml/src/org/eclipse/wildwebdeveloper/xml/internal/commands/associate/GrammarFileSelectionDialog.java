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

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

/**
 * Workspace file-picker dialog restricted to grammar extensions
 * (xsd, dtd, rng, rnc).
 */
public class GrammarFileSelectionDialog extends FilteredResourcesSelectionDialog {

	private final List<String> grammarExtensions;

	public GrammarFileSelectionDialog(Shell shell, IContainer container, List<String> grammarExtensions) {
		super(shell, false, container, IResource.FILE);
		this.grammarExtensions = grammarExtensions;
		setInitialPattern("*."); //$NON-NLS-1$
		addListFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IFile file) {
					return grammarExtensions.contains(file.getFileExtension());
				}
				return true;
			}
		});
	}
}
