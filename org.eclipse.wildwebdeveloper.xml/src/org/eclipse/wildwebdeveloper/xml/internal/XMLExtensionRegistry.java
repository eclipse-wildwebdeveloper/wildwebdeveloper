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
 *   Andrew Obuchowicz (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wildwebdeveloper.xml.InitializationOptionsProvider;
import org.eclipse.wildwebdeveloper.xml.LemminxClasspathExtensionProvider;

public class XMLExtensionRegistry {

	private static final String EXTENSION_POINT_ID = Activator.PLUGIN_ID + ".lemminxExtension"; //$NON-NLS-1$
	private Map<IConfigurationElement, String> extensions = new HashMap<>();
	private boolean outOfSync = true;

	public XMLExtensionRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener((event -> outOfSync = true), EXTENSION_POINT_ID);
	}

	/**
	 * Returns the XML extension jar paths relative to the root of their
	 * contributing plug-in.
	 *
	 * @return List of XML extension jar paths (relative to the root of their
	 *         contributing plug-in).
	 */
	public List<String> getXMLExtensionJars() {
		if (this.outOfSync) {
			sync();
		}
		// Filter for .jar files and retrieve their paths relative to their contributing
		// plug-in
		return this.extensions.entrySet().stream().filter(extension -> extension.getValue().endsWith(".jar"))
				.map(extension -> {
					try {
						return new java.io.File(FileLocator.toFileURL(
								FileLocator.find(Platform.getBundle(extension.getKey().getContributor().getName()),
										new Path(extension.getValue())))
								.getPath()).getAbsolutePath();
					} catch (InvalidRegistryObjectException | IOException e) {
						ILog.get().error(e.getMessage(), e);
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public List<String> getXMLLSClassPathExtensions() {
		Map<IConfigurationElement, LemminxClasspathExtensionProvider> extensionProviders = getRegisteredClassPathProviders();

		List<String> classpathExtensions = new ArrayList<>();
		extensionProviders.entrySet().stream()
				.map(Entry<IConfigurationElement, LemminxClasspathExtensionProvider>::getValue)
				.map(LemminxClasspathExtensionProvider::get)
				.forEach(list -> list.forEach(jar -> classpathExtensions.add(jar.getAbsolutePath())));

		return classpathExtensions;
	}

	private Map<IConfigurationElement, LemminxClasspathExtensionProvider> getRegisteredClassPathProviders() {
		Map<IConfigurationElement, LemminxClasspathExtensionProvider> extensionProviders = new HashMap<>();
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			try {
				if (extension.getName().equals("classpathExtensionProvider") && extension.getAttribute("provider") != null) {
					final Object executableExtension = extension.createExecutableExtension("provider");
					if (executableExtension instanceof LemminxClasspathExtensionProvider extensionProvider) {
						extensionProviders.put(extension, extensionProvider);
					}
				}
			} catch (Exception ex) {
				ILog.get().error(ex.getMessage(), ex);

			}
		}
		return extensionProviders;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					if (extension.getAttribute("path") != null) {
						this.extensions.put(extension, extension.getAttribute("path"));
					}
				} catch (Exception ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.extensions.remove(toRemove);
		}
		this.outOfSync = false;
	}

	public Map<String, Object> getInitiatizationOptions() {
		Map<String, Object> res = new HashMap<>();
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			try {
				if (extension.getName().equals("initializationOptionsProvider") && extension.getAttribute("provider") != null) {
					final Object executableExtension = extension.createExecutableExtension("provider");
					if (executableExtension instanceof InitializationOptionsProvider opt) {
						Map<String, Object> options = opt.get();
						if (options != null) {
							res.putAll(options);
						}
					}
				}
			} catch (Exception ex) {
				ILog.get().error(ex.getMessage(), ex);

			}
		}
		return res;
	}
}
