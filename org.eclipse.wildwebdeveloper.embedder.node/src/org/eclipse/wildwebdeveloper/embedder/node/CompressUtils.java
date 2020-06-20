/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.embedder.node;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.dump.UnsupportedCompressionAlgorithmException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class CompressUtils {
	public static final String ZIP_EXTENSION = ".zip";
	public static final String TAR_GZ_EXTENSION = ".tar.gz";
	public static final String TAR_XZ_EXTENSION = ".tar.xz";
	
	public static void unarchive(URL find, File baseDir) throws IOException {
		String archive = find != null ? find.getFile() : "";
		if (archive.endsWith(TAR_GZ_EXTENSION) || archive.endsWith(TAR_XZ_EXTENSION)) {
			try (InputStream fi = find.openStream();
					InputStream bi = new BufferedInputStream(fi);
					InputStream gzi = archive.endsWith(TAR_XZ_EXTENSION) ? 
							new XZCompressorInputStream(bi) : new GzipCompressorInputStream(bi);
					TarArchiveInputStream in = new TarArchiveInputStream(gzi)) {
				extractArchive(in, baseDir);
			}
		} else if (archive.endsWith(ZIP_EXTENSION)) {
			try (InputStream fi = find.openStream();
					ZipArchiveInputStream in = new ZipArchiveInputStream(fi)) {
				extractArchive(in, baseDir);
			}
		} else {
			throw new UnsupportedCompressionAlgorithmException("Unsupported archive file extension: " + archive);
		}
		
	}
	
	/**
	 * Extract zip/tar.gz/tar.xz file to destination folder.
	 * Sets up 'executable' permission for TarAchiveEntry representing an 
	 * executable file.
	 *
	 * @param in
	 *            Zip/Tar Archive Input Stream to extract
	 * @param destination
	 *            destination folder
	 */
	public static void extractArchive(ArchiveInputStream in, File destination) throws IOException {
	    ArchiveEntry entry = null;
	    while ((entry = in.getNextEntry()) != null) {
	        if (!in.canReadEntryData(entry)) {
	            // log something?
	            continue;
	        }
	        File f = new File(destination, entry.getName());
	        if (entry.isDirectory()) {
	            if (!f.isDirectory() && !f.mkdirs()) {
	                throw new IOException("failed to create directory " + f);
	            }
	        } else {
	            File parent = f.getParentFile();
	            if (!parent.isDirectory() && !parent.mkdirs()) {
	                throw new IOException("failed to create directory " + parent);
	            }
	            try (OutputStream o = Files.newOutputStream(f.toPath())) {
	                IOUtils.copy(in, o);
	            }
	            if (entry instanceof TarArchiveEntry) {
	            	f.setExecutable((((TarArchiveEntry)entry).getMode() & 256) != 0);
	            }
	        }
	    }
	}
}
