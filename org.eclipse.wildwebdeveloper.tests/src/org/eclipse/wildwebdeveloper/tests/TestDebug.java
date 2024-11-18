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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.node.NodeRunDAPDebugDelegate;
import org.eclipse.wildwebdeveloper.debug.node.NodeRunDebugLaunchShortcut;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("restriction")
@ExtendWith(AllCleanRule.class)
public class TestDebug {

    protected ILaunchManager launchManager;

    @BeforeEach
    public void setUpLaunch() throws DebugException {
        this.launchManager = DebugPlugin.getDefault().getLaunchManager();
        removeAllLaunches();
        ScopedPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.debug.ui");
        prefs.setValue("org.eclipse.debug.ui.switch_perspective_on_suspend", MessageDialogWithToggle.ALWAYS);
    }

    private void removeAllLaunches() throws DebugException {
        for (ILaunch launch : this.launchManager.getLaunches()) {
            try {
                launch.terminate();
            } catch (DebugException e) {
                e.printStackTrace();
            }
            for (IDebugTarget debugTarget : launch.getDebugTargets()) {
                try {
                    debugTarget.terminate();
                } catch (DebugException e) {
                    e.printStackTrace();
                }
                launch.removeDebugTarget(debugTarget);
            }
            for (IProcess process : launch.getProcesses()) {
                process.terminate();
            }
            // workaround that some debugger process don't terminate as expected
            // LSP4E fixes it in later versions: https://github.com/eclipse/lsp4e/pull/122
            ProcessHandle.current().descendants()
                    .filter(process -> process.info().commandLine()
                            .filter(command -> command.contains("node") && command.contains("debug")).isPresent())
                    .forEach(ProcessHandle::destroyForcibly);
            launchManager.removeLaunch(launch);
        }
    }

    @AfterEach
    public void tearDownLaunch() throws DebugException {
        removeAllLaunches();
    }

    @Test
    public void testRunExpandEnv() throws Exception {
        File f = File.createTempFile("testEnv", ".js");
        f.deleteOnExit();
        Files.write(f.toPath(), "console.log(process.env.ECLIPSE_HOME);".getBytes());
        ILaunchConfigurationWorkingCopy launchConfig = launchManager
                .getLaunchConfigurationType(NodeRunDAPDebugDelegate.ID)
                .newInstance(ResourcesPlugin.getWorkspace().getRoot(), f.getName());
        launchConfig.setAttribute(LaunchConstants.PROGRAM, f.getAbsolutePath());
        launchConfig.setAttribute(LaunchManager.ATTR_ENVIRONMENT_VARIABLES, Map.of("ECLIPSE_HOME", "${eclipse_home}"));
        launchConfig.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
        ILaunch launch = launchConfig.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
        while (!launch.isTerminated()) {
            DisplayHelper.sleep(Display.getDefault(), 50);
        }
        // ensure last UI events are processed and console is visible and populated.
        assertFalse(
                DisplayHelper.waitForCondition(Display.getDefault(), 1000,
                        () -> Arrays.stream(ConsolePlugin.getDefault().getConsoleManager().getConsoles()) //
                                .filter(IOConsole.class::isInstance) //
                                .map(IOConsole.class::cast) //
                                .map(IOConsole::getDocument) //
                                .map(IDocument::get) //
                                .anyMatch(content -> content.contains("${eclipse_home}"))),
                "env variable is not replaced in subprocess");
    }

    @Test
    public void testRunExpandDebugVars() throws Exception {
        IProject project = Utils.provisionTestProject("helloWorldJS");
        IFile f = project.getFile("hello.js");
        ILaunchConfigurationWorkingCopy launchConfig = launchManager
                .getLaunchConfigurationType(NodeRunDAPDebugDelegate.ID)
                .newInstance(ResourcesPlugin.getWorkspace().getRoot(), f.getName());
        launchConfig.setAttribute(LaunchConstants.PROGRAM, "${workspace_loc:" + f.getFullPath() + '}');
        launchConfig.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
        ILaunch launch = launchConfig.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
        while (!launch.isTerminated()) {
            DisplayHelper.sleep(Display.getDefault(), 50);
        }
        // ensure last UI events are processed and console is visible and populated.
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 1000,
                () -> Arrays.stream(ConsolePlugin.getDefault().getConsoleManager().getConsoles()) //
                        .filter(IOConsole.class::isInstance) //
                        .map(IOConsole.class::cast) //
                        .map(IOConsole::getDocument) //
                        .map(IDocument::get) //
                        .anyMatch(content -> content.contains("Hello"))),
                "Missing log output");
    }

    @Test
    public void testFindThreadsAndHitsBreakpoint() throws Exception {
        IProject project = Utils.provisionTestProject("helloWorldJS");
        IFile jsFile = project.getFile("hello.js");
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), jsFile);
        IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        TextSelection selection = new TextSelection(doc, doc.getLineOffset(1) + 1, 0);
        IToggleBreakpointsTarget toggleBreakpointsTarget = DebugUITools.getToggleBreakpointsTargetManager()
                .getToggleBreakpointsTarget(editor, selection);
        toggleBreakpointsTarget.toggleLineBreakpoints(editor, selection);
        Set<IDebugTarget> before = new HashSet<>(Arrays.asList(launchManager.getDebugTargets()));
        DisplayHelper.sleep(1000);
        new NodeRunDebugLaunchShortcut().launch(editor, ILaunchManager.DEBUG_MODE);
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 30000,
                () -> launchManager.getDebugTargets().length > before.size()), "New Debug Target not created");
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 30000, () -> {
            try {
                return debugTargetWithThreads(before) != null;
            } catch (DebugException e) {
                e.printStackTrace();
                return false;
            }
        }), "Debug Target shows no threads");
        IDebugTarget target = debugTargetWithThreads(before);
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 3000, () -> {
            try {
                return Arrays.stream(target.getThreads()).anyMatch(ISuspendResume::isSuspended);
            } catch (DebugException e) {
                e.printStackTrace();
                return false;
            }
        }), "No thread is suspended");
        IThread suspendedThread = Arrays.stream(target.getThreads()).filter(ISuspendResume::isSuspended).findFirst()
                .get();
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 3000, () -> {
            try {
                return suspendedThread.getStackFrames().length > 0
                        && suspendedThread.getStackFrames()[0].getVariables().length > 0;
            } catch (Exception ex) {
                // ignore
                return false;
            }
        }), "Suspended Thread doesn't show variables");
        IVariable localVariable = suspendedThread.getStackFrames()[0].getVariables()[0];
        assertEquals("Local", localVariable.getName());
        IVariable nVariable = Arrays.stream(localVariable.getValue().getVariables()).filter(var -> {
            try {
                return "n".equals(var.getName());
            } catch (DebugException e) {
                return false;
            }
        }).findAny().get();
        assertEquals("1605", nVariable.getValue().getValueString());
    }

    private IDebugTarget debugTargetWithThreads(Collection<IDebugTarget> toExclude) throws DebugException {
        Set<IDebugTarget> current = new HashSet<>(Arrays.asList(launchManager.getDebugTargets()));
        current.removeAll(toExclude);
        for (IDebugTarget target : current) {
            if (target.getThreads().length > 0) {
                return target;
            }
        }
        return null;
    }

    @Test
    public void testFindThreadsAndHitsBreakpointTypeScript() throws Exception {
        IProject project = Utils.provisionTestProject("HelloWorldTS");
        IFile tsFile = project.getFile("index.ts");
        ITextEditor editor = (ITextEditor) IDE
                .openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), tsFile);
        IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        TextSelection selection = new TextSelection(doc, doc.getLineOffset(2) + 1, 0);
        IToggleBreakpointsTarget toggleBreakpointsTarget = DebugUITools.getToggleBreakpointsTargetManager()
                .getToggleBreakpointsTarget(editor, selection);
        toggleBreakpointsTarget.toggleLineBreakpoints(editor, selection);
        Set<IDebugTarget> before = new HashSet<>(Arrays.asList(launchManager.getDebugTargets()));
        DisplayHelper.sleep(1000);
        new NodeRunDebugLaunchShortcut().launch(editor, ILaunchManager.DEBUG_MODE);
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 30000, () -> {
            try {
                return debugTargetWithThreads(before) != null;
            } catch (DebugException e) {
                e.printStackTrace();
                return false;
            }
        }), "Debug Target shows no threads");
        IDebugTarget target = debugTargetWithThreads(before);
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 3000, () -> {
            try {
                return Arrays.stream(target.getThreads()).anyMatch(ISuspendResume::isSuspended);
            } catch (DebugException e) {
                e.printStackTrace();
                return false;
            }
        }), "No thread is suspended");
        IThread suspendedThread = Arrays.stream(target.getThreads()).filter(ISuspendResume::isSuspended).findFirst()
                .get();
        assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 3000, () -> {
            try {
                return suspendedThread.getStackFrames().length > 0
                        && suspendedThread.getStackFrames()[0].getVariables().length > 0;
            } catch (Exception ex) {
                // ignore
                return false;
            }
        }), "Suspended Thread doesn't show variables");
        IVariable closureVar = null;
        for (IVariable variable : suspendedThread.getStackFrames()[0].getVariables()) {
            if ("Closure".equals(variable.getName())) {
                closureVar = variable;
            }
        }
        IVariable userVariable = null;
        for (IVariable variable : closureVar.getValue().getVariables()) {
            if ("user".equals(variable.getName())) {
                userVariable = variable;
            }
        }
        assertEquals("'Eclipse User'", userVariable.getValue().getValueString());
    }

}
