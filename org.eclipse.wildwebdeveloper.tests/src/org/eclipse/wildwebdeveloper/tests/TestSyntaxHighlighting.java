/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllCleanRule.class)
public class TestSyntaxHighlighting {

	private IProject project;

	@BeforeEach
	public void initializeHostProject() throws CoreException {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("blah");
		project.create(null);
		project.open(null);
	}

	@Test
	public void testJSXHighlighting() throws CoreException {
		IFile file = project.getFile("test.jsx");
		file.create(new ByteArrayInputStream("var n = 4;\n".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		StyledText widget = (StyledText) editor.getAdapter(Control.class);
		Color defaultTextColor = widget.getForeground();
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return Arrays.stream(widget.getStyleRanges())
						.anyMatch(range -> range.foreground != null && !defaultTextColor.equals(range.foreground));
			}
		}.waitForCondition(widget.getDisplay(), 5000), "Missing syntax highlighting");
	}

}
