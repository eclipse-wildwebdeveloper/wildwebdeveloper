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
package org.eclipse.wildwebdeveloper.yaml.ui.preferences;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wildwebdeveloper.yaml.ui.Messages;

/**
 * Field editor to fill YML customtags valid properties.
 *
 */
public class CustomTagsFieldEditor extends ListEditor {

	public CustomTagsFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder path = new StringBuilder("");//$NON-NLS-1$

		for (String item : items) {
			path.append(item);
			path.append(",");
		}
		return path.toString();
	}

	@Override
	protected String getNewInputObject() {
		InputDialog dialog = new InputDialog(getShell(), Messages.CustomTagsFieldEditor_inputDialog_title,
				Messages.CustomTagsFieldEditor_inputDialog_description, "", null);
		if (IDialogConstants.OK_ID == dialog.open()) {
			String dir = dialog.getValue();
			dir = dir.trim();
			if (dir.isEmpty()) {
				return null;
			}
			return dir;
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, ",");//$NON-NLS-1$
		ArrayList<Object> v = new ArrayList<>();
		while (st.hasMoreElements()) {
			v.add(st.nextElement());
		}
		return v.toArray(new String[v.size()]);
	}

}
