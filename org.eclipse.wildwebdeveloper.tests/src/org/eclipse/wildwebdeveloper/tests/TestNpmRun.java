/*******************************************************************************
 * Copyright (c) 2022, 2024 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.wildwebdeveloper.debug.LaunchConstants;
import org.eclipse.wildwebdeveloper.debug.npm.NpmLaunchDelegate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("restriction")
@ExtendWith(AllCleanRule.class)
public class TestNpmRun {
    protected ILaunchManager launchManager;

    @BeforeEach
    public void setUpLaunch() throws DebugException {
        this.launchManager = DebugPlugin.getDefault().getLaunchManager();
        removeAllLaunches();
    }

    private void removeAllLaunches() throws DebugException {
        for (ILaunch launch : this.launchManager.getLaunches()) {
            launch.terminate();
            for (IDebugTarget debugTarget : launch.getDebugTargets()) {
                debugTarget.terminate();
                launch.removeDebugTarget(debugTarget);
            }
            for (IProcess process : launch.getProcesses()) {
                process.terminate();
            }
        }
    }

    @AfterEach
    public void tearDownLaunch() throws DebugException {
        removeAllLaunches();
    }

    @Test
    public void testNpmRunEnvVariables() throws Exception {
        IProject project = Utils.provisionTestProject("envtest");
        IFile projectFile = project.getFile("package.json");

        ILaunchConfigurationWorkingCopy launchConfig = launchManager.getLaunchConfigurationType(NpmLaunchDelegate.ID)
                .newInstance(ResourcesPlugin.getWorkspace().getRoot(), "npm.envtest");

        launchConfig.setAttribute(LaunchConstants.PROGRAM, projectFile.getLocation().toOSString());
        launchConfig.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, project.getLocation().toOSString());
        launchConfig.setAttribute(NpmLaunchDelegate.ARGUMENTS, "run envtest");
        launchConfig.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
                Map.of("ENVTEST_ENVIRONMENT_VARIABLE", "ENVTEST_VARIABLE_VALUE}"));
        launchConfig.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
        ILaunch launch = launchConfig.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
        while (!launch.isTerminated()) {
            DisplayHelper.sleep(Display.getDefault(), 50);
        }

        // Console message stream from a child process remains blocked until the test
        // case finish, so we'll test the output file created instead of trying to read
        // Console document
        final File envtestLogFile = new File(project.getLocation().toOSString(), "envtest.dump");
        assertTrue(envtestLogFile.exists() && envtestLogFile.canRead(),
                "Env variable test result dump file is not created");

        String content = Files.readString(envtestLogFile.toPath());
        assertTrue(content.contains("ENVTEST_ENVIRONMENT_VARIABLE") && content.contains("ENVTEST_VARIABLE_VALUE"),
                "Env variable aren't not passed to subprocess");
    }
}
