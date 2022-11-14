/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Angelo ZERR (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link BooleanFieldEditor} extension to display the checkbox with
 * indentation.
 * 
 */
public class IndentedBooleanFieldEditor extends BooleanFieldEditor {

	private static final int INDENT_SIZE = 20;

	/**
	 * Creates a boolean field editor in the default style.
	 *
	 * @param name   the name of the preference this field editor works on
	 * @param label  the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	public IndentedBooleanFieldEditor(String name, String label, Composite parent) {
		super(name, label, DEFAULT, parent);
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		super.doFillIntoGrid(parent, numColumns);
		Button checkBox = super.getChangeControl(parent);
		((GridData) checkBox.getLayoutData()).horizontalIndent = INDENT_SIZE;
	}
}
