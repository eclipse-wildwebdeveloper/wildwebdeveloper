/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class Utils {

	/**
	 * Provisions a project that's part of the "testProjects"
	 *
	 * @param folderName the folderName under "testProjects" to provision from
	 * @return the provisioned project
	 * @throws CoreException
	 * @throws IOException
	 */
	public static IProject provisionTestProject(String folderName) throws CoreException, IOException {
		URL url = FileLocator.find(Platform.getBundle("org.eclipse.wildwebdeveloper.tests"),
				Path.fromPortableString("testProjects/" + folderName), null);
		url = FileLocator.toFileURL(url);
		File folder = new File(url.getFile());
		if (folder.exists()) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject" + System.nanoTime());
			project.create(null);
			project.open(null);
			java.nio.file.Path sourceFolder = folder.toPath();
			java.nio.file.Path destFolder = project.getLocation().toFile().toPath();

			Files.walk(sourceFolder).forEach(source -> {
				try {
					Files.copy(source, destFolder.resolve(sourceFolder.relativize(source)),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (DirectoryNotEmptyException e) {
					// Ignore
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			return project;
		}
		return null;
	}

	public static ISourceViewer getViewer(AbstractTextEditor editor) {
		try {
			Method method = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
			method.setAccessible(true);
			return (ISourceViewer) method.invoke(editor);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
