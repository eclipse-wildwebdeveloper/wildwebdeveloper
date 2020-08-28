/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wildwebdeveloper.debug.node.NodeRunDebugLaunchShortcut;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class TestDebugTS extends TestDebug {
	@Override
	@Test
	public void testFindThreadsAndHitsBreakpoint() throws Exception {
		IProject project = Utils.provisionTestProject("HelloWorldTS");
		IFile tsFile = project.getFile("index.ts");
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), tsFile);
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TextSelection selection = new TextSelection(doc, doc.getLineOffset(1) + 1, 0);
		IToggleBreakpointsTarget toggleBreakpointsTarget = DebugUITools.getToggleBreakpointsTargetManager()
				.getToggleBreakpointsTarget(editor, selection);
		toggleBreakpointsTarget.toggleLineBreakpoints(editor, selection);
		Set<IDebugTarget> before = new HashSet<>(Arrays.asList(launchManager.getDebugTargets()));
		DisplayHelper.sleep(1000);
		new NodeRunDebugLaunchShortcut().launch(editor, ILaunchManager.DEBUG_MODE);
		assertTrue(new DisplayHelper() {
			@Override
			public boolean condition() {
				return launchManager.getDebugTargets().length > before.size();
			}
		}.waitForCondition(Display.getDefault(), 30000), "New Debug Target not created");
		Set<IDebugTarget> after = new HashSet<>(Arrays.asList(launchManager.getDebugTargets()));
		after.removeAll(before);
		assertEquals(1, after.size(), "Extra DebugTarget not found");
		IDebugTarget target = after.iterator().next();
		assertTrue(new DisplayHelper() {
			@Override
			public boolean condition() {
				try {
					return target.getThreads().length > 0;
				} catch (DebugException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(Display.getDefault(), 30000), "Debug Target shows no threads");
		assertTrue(new DisplayHelper() {
			@Override
			public boolean condition() {
				try {
					return Arrays.stream(target.getThreads()).anyMatch(ISuspendResume::isSuspended);
				} catch (DebugException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(Display.getDefault(), 3000), "No thread is suspended");
		IThread suspendedThread = Arrays.stream(target.getThreads()).filter(ISuspendResume::isSuspended).findFirst()
				.get();
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return suspendedThread.getStackFrames().length > 0
							&& suspendedThread.getStackFrames()[0].getVariables().length > 0;
				} catch (Exception ex) {
					// ignore
					return false;
				}
			}
		}.waitForCondition(Display.getDefault(), 3000), "Suspended Thread doesn't show variables");
		IVariable localVariable = suspendedThread.getStackFrames()[0].getVariables()[0];
		assertEquals("Local", localVariable.getName());
		IVariable nVariable = Arrays.stream(localVariable.getValue().getVariables()).filter(var -> {
			try {
				return "user".equals(var.getName());
			} catch (DebugException e) {
				return false;
			}
		}).findAny().get();
		assertEquals("\"Eclipse User\"", nVariable.getValue().getValueString());
	}
}
