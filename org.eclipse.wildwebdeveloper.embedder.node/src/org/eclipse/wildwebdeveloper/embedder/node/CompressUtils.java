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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.dump.UnsupportedCompressionAlgorithmException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;

public class CompressUtils {
    public static void unarchive(URL archiveURL, File baseDir) throws IOException {
        if (archiveURL == null || baseDir == null) {
            return;
        }
        ArchiveInputStream<?> archive = null;
        try (InputStream input = archiveURL.openStream()) {
            if (archiveURL.getFile().endsWith(".tar.gz")) { //$NON-NLS-1$
                InputStream gz = new GzipCompressorInputStream(input);
                archive = new TarArchiveInputStream(gz);
            } else if (archiveURL.getFile().endsWith(".tar.xz")) { //$NON-NLS-1$
                InputStream xz = new XZCompressorInputStream(input);
                archive = new TarArchiveInputStream(xz);
            } else if (archiveURL.getFile().endsWith(".zip")) { //$NON-NLS-1$
                archive = new ZipArchiveInputStream(input);
            } else {
                throw new UnsupportedCompressionAlgorithmException("Unsupported archive file extension: " + archive); //$NON-NLS-1$
            }
            try {
                extractArchive(archive, baseDir);
            } finally {
                IOUtils.closeQuietly(archive);
            }
        }
    }

    /**
     * Extract zip/tar.gz/tar.xz file to destination folder.
     * Sets up 'executable' permission for TarAchiveEntry representing an
     * executable file.
     *
     * @param in
     *                    Zip/Tar Archive Input Stream to extract
     * @param destination
     *                    destination folder
     */
    private static void extractArchive(ArchiveInputStream<?> in, File destination) throws IOException {
        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            if (!in.canReadEntryData(entry)) {
                // log something?
                continue;
            }
            File f = new File(destination, entry.getName());
            f.delete();
            boolean symlink = entry instanceof TarArchiveEntry tarEntry && tarEntry.isSymbolicLink();
            if (entry.isDirectory()) {
                if (!f.isDirectory() && !f.mkdirs()) {
                    throw new IOException("failed to create directory " + f);
                }
            } else {
                File parent = f.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("failed to create directory " + parent);
                }
                if (symlink) {
                    String linkName = ((TarArchiveEntry) entry).getLinkName();
                    Files.createSymbolicLink(f.toPath(), Paths.get(linkName));
                } else {
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        in.transferTo(o);
                    }
                }
                if (entry instanceof TarArchiveEntry tarEntry) {
                    f.setExecutable((tarEntry.getMode() & 256) != 0);
                }
            }
        }
    }
}
