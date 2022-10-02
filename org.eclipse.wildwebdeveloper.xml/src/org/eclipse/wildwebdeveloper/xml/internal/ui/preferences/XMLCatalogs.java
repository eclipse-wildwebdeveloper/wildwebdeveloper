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

import static org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceConstants.XML_PREFERENCES_CATAGLOGS;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
		catalogFile.append("<?xml version=\"1.0\"?>\n"
				+ "<!DOCTYPE catalog PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\" \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\n"
				+ "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">\n");
		Arrays.stream(Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.wst.xml.core.catalogContributions"))
				.filter(element -> "catalogContribution".equals(element.getName()))
				.flatMap(element -> Arrays.stream(element.getChildren())).forEach(element -> {
					switch (element.getName()) {
					case "public": {
						String publicId = element.getAttribute("publicId");
						URI uri = createURI(element);
						if (publicId != null && uri != null) {
							catalogFile.append("<public publicId=\"").append(publicId).append("\" uri=\"").append(uri)
									.append("\"/>\n");
						}
						break;
					}
					case "system": {
						String namespace = element.getAttribute("systemId");
						URI uri = createURI(element);
						if (namespace != null && uri != null) {
							catalogFile.append("<system systemId=\"").append(namespace).append("\" uri=\"").append(uri)
									.append("\"/>\n");
						}
						break;
					}
					case "uri": {
						String name = element.getAttribute("name");
						URI uri = createURI(element);
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
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return SYSTEM_CATALOG;
	}

	private static URI createURI(IConfigurationElement element) {
		URI uri = URI.create(element.getAttribute("uri"));
		if (!uri.isAbsolute()) {
			try {
				String contributorName = element.getContributor().getName();
				URL url = FileLocator.find(Platform.getBundle(contributorName),
						Path.fromPortableString(uri.toString()));
				if(Objects.nonNull(url)) {
					// this constructor will ensure parts are URI encoded correctly
					uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), null, null);
				} else {
					Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							"A URL object was not found for the given URI "+uri+ " from  "+contributorName));
				}
				
			} catch (InvalidRegistryObjectException | URISyntaxException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}
		}
		if (!"file".equals(uri.getScheme())) { // are some other scheme supported directly by LemMinX ?
			try {
				URL url = FileLocator.toFileURL(uri.toURL());
				// as above
				uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), null, null);
			} catch (InvalidRegistryObjectException | IOException | URISyntaxException e) {
				String plugin = element.getNamespaceIdentifier();
				String uriString = element.getAttribute("uri");
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Error while getting URI '" + uriString + "' from plugin '" + plugin + "' : " + e.getMessage(),
						e));
			}
		}
		return uri;
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
