/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wildwebdeveloper.Activator;

public class FileUtils {
	public static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	public static File uriToFile(String uri) {
		// not using `new File(new URI(uri))` here which does not support Windows UNC paths
		// and instead throws IllegalArgumentException("URI has an authority component")
		return Paths.get(URI.create(uri)).toFile();
	}

   public static Path uriToPath(String uri) {
      return Paths.get(URI.create(uri));
   }

	public static URI toUri(String filePath) {
		return toUri(new File(filePath));
	}

	public static URI toUri(File file) {
		// copied from org.eclipse.lsp4e.LSPEclipseUtils#toUri(File)

		// URI scheme specified by language server protocol and LSP
		try {
			final var path = file.getAbsoluteFile().toURI().getPath();
			if (path.startsWith("//")) { // UNC path like //localhost/c$/Windows/ //$NON-NLS-1$
				// split: authority = "localhost", absPath = "/c$/Windows/"
				final int slash = path.indexOf('/', 2);
				final String authority = slash > 2 ? path.substring(2, slash) : path.substring(2);
				final String absPath = slash > 2 ? path.substring(slash) : "/"; //$NON-NLS-1$
				return new URI(FILE_SCHEME, authority, absPath, null);
			}
			return new URI(FILE_SCHEME, "", path, null); //$NON-NLS-1$
		} catch (URISyntaxException ex) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, ex.getMessage(), ex));
			return file.getAbsoluteFile().toURI();
		}
	}

}
