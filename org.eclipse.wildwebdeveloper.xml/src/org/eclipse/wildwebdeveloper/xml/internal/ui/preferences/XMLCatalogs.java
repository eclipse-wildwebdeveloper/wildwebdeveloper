/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants.XML_PREFERENCES_CATAGLOGS;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

public class XMLCatalogs {

	private static final Comparator<File> FILE_CASE_INSENSITIVE_ORDER = Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER);
	private static final File SYSTEM_CATALOG = Activator.getDefault().getStateLocation().append("system-catalog.xml").toFile();

	public static Set<File> getUserCatalogs(IPreferenceStore store) {
		Set<File> catalogs = new TreeSet<>(FILE_CASE_INSENSITIVE_ORDER);
		for (String filepath : store.getString(XML_PREFERENCES_CATAGLOGS.preferenceId).split(",")) {
			if (!filepath.isEmpty()) {
				catalogs.add(new File(filepath));
			}
		}
		return catalogs;
	}

	public static Set<File> getAllCatalogs(IPreferenceStore store) {
		Set<File> res = getUserCatalogs(store);
		res.add(getWTPExtensionCatalog());
		return res;
	}

	
	private static File getWTPExtensionCatalog() {
		StringBuilder catalogFile = new StringBuilder();
		catalogFile.append("""
			<?xml version="1.0"?>
			<!DOCTYPE catalog PUBLIC "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN" "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd">
			<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">
			""");
		Arrays.stream(Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.wst.xml.core.catalogContributions"))
				.filter(element -> "catalogContribution".equals(element.getName()))
				.flatMap(element -> Arrays.stream(element.getChildren())).forEach(element -> {
					switch (element.getName()) {
					case "public": {
						String publicId = element.getAttribute("publicId");
						URI uri = resolveURI(element);
						if (publicId != null && uri != null) {
							catalogFile.append("<public publicId=\"").append(publicId).append("\" uri=\"").append(uri)
									.append("\"/>\n");
						}
						break;
					}
					case "system": {
						String namespace = element.getAttribute("systemId");
						URI uri = resolveURI(element);
						if (namespace != null && uri != null) {
							catalogFile.append("<system systemId=\"").append(namespace).append("\" uri=\"").append(uri)
									.append("\"/>\n");
						}
						break;
					}
					case "uri": {
						String name = element.getAttribute("name");
						URI uri = resolveURI(element);
						if (name != null && uri != null) {
							catalogFile.append("<uri name=\"").append(name).append("\" uri=\"").append(uri)
									.append("\"/>\n");
						}
						break;
					}
					}
				});
		catalogFile.append("</catalog>");
		/* example
  <extension point="org.eclipse.wst.xml.core.catalogContributions">
    <catalogContribution>
      <system systemId="http://maven.apache.org/maven-v4_0_0.xsd"
              uri="xsd/maven-v4_0_0.xsd"/>
      <system systemId="http://maven.apache.org/xsd/maven-4.0.0.xsd"
              uri="xsd/maven-v4_0_0.xsd"/>
      <system systemId="http://maven.apache.org/xsd/settings-1.0.0.xsd"
              uri="xsd/settings-v1_0_0.xsd"/>
      <system systemId="http://maven.apache.org/xsd/profiles-1.0.0.xsd"
              uri="xsd/profiles-v1_0_0.xsd"/>
      <system systemId="http://maven.apache.org/xsd/archetype-1.0.0.xsd"
              uri="xsd/archetype-1.0.0.xsd"/>
      <system systemId="http://maven.apache.org/xsd/archetype-catalog-1.0.0.xsd"
              uri="xsd/archetype-catalog-1.0.0.xsd"/>
      <system systemId="http://maven.apache.org/xsd/archetype-descriptor-1.0.0.xsd"
              uri="xsd/archetype-descriptor-1.0.0.xsd"/>
    </catalogContribution>
  </extension>
		 */
		try {
			Files.writeString(SYSTEM_CATALOG.toPath(), catalogFile.toString());
		} catch (IOException e) {
			ILog.get().error(e.getMessage(), e);
		}
		return SYSTEM_CATALOG;
	}

	/**
	 * 
	 * @param element
	 * @return the resolved URI, or <code>null</code> is no URI could be resolved.
	 */
	private static URI resolveURI(IConfigurationElement element) {
		URI uri = URI.create(element.getAttribute("uri"));
		if (!uri.isAbsolute()) {
			try {
				String contributorName = element.getContributor().getName();
				URL url = FileLocator.find(Platform.getBundle(contributorName),
						Path.fromPortableString(uri.toString()));
				if (url != null) {
					uri = convertToURI(url);
				} else {
					ILog.get().warn("A URL object was not found for the given URI " + uri + " from " + contributorName);
					return null;
				}
			} catch (InvalidRegistryObjectException | URISyntaxException | MalformedURLException e) {
				ILog.get().error(e.getMessage(), e);
				return null;
			}
		}
		if (!isSchemeSupportedInCatalog(uri)) {
			try {
				String contributorName = element.getContributor().getName();

				// Try to resolve to a system supported Scheme
				URL url = FileLocator.resolve(uri.toURL());
				uri = convertToURI(url);
				if (isSchemeSupportedInCatalog(uri)) {
					return uri;
				}

				// Try to extract and cache the contents if necessary
				// This will possibly break includes with relative paths 
				url = FileLocator.toFileURL(url);
				uri = convertToURI(url);
				if (isSchemeSupportedInCatalog(uri)) {
					return uri;
				}

				// Could not convert to any supported scheme
				ILog.get().warn("The given URI " + element.getAttribute("uri")
								+ " from " + contributorName + " could not be resolved for local access");
				return null;
			} catch (InvalidRegistryObjectException | IOException | URISyntaxException e) {
				String plugin = element.getNamespaceIdentifier();
				String uriString = element.getAttribute("uri");
				ILog.get().error("Error while getting URI '" + uriString + "' from plugin '" + plugin + "' : " + e.getMessage(), e);
				return null;
			}
		}
		return uri;
	}

	/**
	 * Converts an URI to a URL.
	 * <p>
	 * Realized as a separate method, as URLs possibly include not properly encoded
	 * parts (Issue #756), that require special treatment.
	 * </p>
	 * 
	 * @param url URL to convert
	 * @return converted URI
	 * @throws MalformedURLException Error creating a
	 */
	private static URI convertToURI(URL url) throws URISyntaxException, MalformedURLException {
		if ("file".equals(url.getProtocol())) {
			return new URI(url.getProtocol(), url.getAuthority(), url.getPath(), null, null);
		}
		if ("jar".equals(url.getProtocol())) {
			/*
			 * JAR-URLs might be based on a file URL that as well possibly doesn't properly
			 * encode parts. Use our own conversion logic to get this fixed.
			 */

			// opaque part that contains the URL of the JAR
			String file = url.getFile();

			// determine the URL of the JAR
			String jarURLString = file;
			// possibly cut of the entry part
			int startEntry = file.indexOf("!/");
			if (startEntry >= 0) {
				jarURLString = jarURLString.substring(0, startEntry);
			}

			// convert JAR-URL
			URL jarURL = new URL(jarURLString);
			URI jarURI = convertToURI(jarURL);

			// build URI with valid JAR-URL by replacing the URL of the JAR with the valid
			// one
			return new URI(url.toExternalForm().replace(jarURLString, jarURI.toString()));
		}
		return url.toURI();
	}

	/**
	 * Check whether the provided scheme is supported for the XML-catalog
	 * 
	 * @param scheme scheme to test
	 * @return <code>true</code> if supported, otherwise <code>false</code>
	 */
	private static boolean isSchemeSupportedInCatalog(URI uri) {
		// are some other scheme supported directly by LemMinX ?
		// LemminX-Issue for supported protocols:
		// https://github.com/eclipse/lemminx/issues/1477
		return "file".equals(uri.getScheme()) || "jar".equals(uri.getScheme());
	}

	public static void storeUserCatalogs(IPreferenceStore store, Set<File> catalogs) {
		StringBuilder catalogsStr = new StringBuilder();
		if (!catalogs.isEmpty()) {
			for (File f : catalogs) {
				catalogsStr.append(f.getAbsolutePath()).append(',');
			}
		}
		store.setValue(XML_PREFERENCES_CATAGLOGS.preferenceId, catalogsStr.toString());
	}

}
