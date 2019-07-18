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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.lsp4e.debug.DSPPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wildwebdeveloper.debug.NodeRunDebugLaunchShortcut;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TestDebug {

	@Rule public AllCleanRule allClean = new AllCleanRule();
	private ILaunchManager launchManager;

	@Before
	public void setUpLaunch() throws DebugException {
		this.launchManager = DebugPlugin.getDefault().getLaunchManager();
		removeAllLaunches();
		ScopedPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.debug.ui");
		prefs.setValue("org.eclipse.debug.ui.switch_perspective_on_suspend", MessageDialogWithToggle.ALWAYS);
	}

	private void removeAllLaunches() throws DebugException {
		for (ILaunch launch : this.launchManager.getLaunches()) {
			launch.terminate();
		}
	}

	@After
	public void tearDownLaunch() throws DebugException {
		removeAllLaunches();
	}

	@Test
	public void testFindThreadsAndHitsBreakpoint() throws Exception {
		IProject project = Utils.provisionTestProject("helloWorldJS");
		IFile jsFile = project.getFile("hello.js");
		ITextEditor editor = (ITextEditor)IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), jsFile);
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TextSelection selection = new TextSelection(doc, 0, 0);
		IToggleBreakpointsTarget toggleBreakpointsTarget = DebugUITools.getToggleBreakpointsTargetManager().getToggleBreakpointsTarget(editor, selection);
		toggleBreakpointsTarget.toggleLineBreakpoints(editor, selection);
		assertFalse("Some existing DAP Launch config already running", Arrays.stream(launchManager.getDebugTargets()).map(IDebugTarget::getModelIdentifier).anyMatch(DSPPlugin.ID_DSP_DEBUG_MODEL::equals));
		new NodeRunDebugLaunchShortcut().launch(editor, ILaunchManager.DEBUG_MODE);
		assertTrue("New Debug Target not created", new DisplayHelper() {
			@Override
			public boolean condition() {
				return Arrays.stream(launchManager.getDebugTargets()).map(IDebugTarget::getModelIdentifier).anyMatch(DSPPlugin.ID_DSP_DEBUG_MODEL::equals);
			}
		}.waitForCondition(Display.getDefault(), 45000));
		IDebugTarget target = Arrays.stream(launchManager.getDebugTargets()).filter(debugTarget -> debugTarget.getModelIdentifier().equals(DSPPlugin.ID_DSP_DEBUG_MODEL)).findAny().get();
		assertTrue("Debug Target shows no threads", new DisplayHelper() {
			@Override
			public boolean condition() {
				try {
					return target.getThreads().length > 0;
				} catch (DebugException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(Display.getDefault(), 3000));target.getThreads();
		assertTrue("No thread is suspended", new DisplayHelper() {
			@Override
			public boolean condition() {
				try {
					return Arrays.stream(target.getThreads()).anyMatch(ISuspendResume::isSuspended);
				} catch (DebugException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(Display.getDefault(), 3000));
	}

}
