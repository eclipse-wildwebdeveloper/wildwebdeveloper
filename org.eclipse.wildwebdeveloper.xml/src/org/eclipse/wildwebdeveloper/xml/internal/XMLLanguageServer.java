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
 *   Xi Yan (Red Hat Inc.) - initial implementation
 *   Andrew Obuchowicz (Red Hat Inc.) - Add support for XML LS extension jars
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class XMLLanguageServer extends ProcessStreamConnectionProvider {

	public XMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		List<String> jarPaths = new ArrayList<>();
		commands.add(computeJavaPath());
		commands.addAll(getProxySettings());
		String debugPortString = System.getProperty(getClass().getName() + ".debugPort");
		if (debugPortString != null) {
			commands.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPortString);
		}
		commands.add("-classpath");
		try {
			URL url = FileLocator
					.toFileURL(getClass().getResource("/language-servers/server/org.eclipse.lsp4xml-0.9.1-uber.jar"));
			List<String> extensionJarPaths = getExtensionJarPaths();
			String uberJarPath = new java.io.File(url.getPath()).getAbsolutePath();
			jarPaths.add(uberJarPath);
			jarPaths.addAll(extensionJarPaths);
			commands.add(String.join(System.getProperty("path.separator"), jarPaths));
			commands.add("org.eclipse.lsp4xml.XMLServerLauncher");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	private Collection<? extends String> getProxySettings() {
		Map<String, String> res = new HashMap<>();
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
				String property = (String) entry.getKey();
				if (property.toLowerCase().contains("proxy") || property.toLowerCase().contains("proxies")) {
					res.put(property, (String) entry.getValue());
				}
			}
		}
		BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<IProxyService> serviceRef = bundleContext.getServiceReference(IProxyService.class);
		if (serviceRef != null) {
			IProxyService service = bundleContext.getService(serviceRef);
			if (service != null) {
				for (IProxyData data : service.getProxyData()) {
					if (data.getHost() != null) {
						res.put(data.getType().toLowerCase() + ".proxyHost", data.getHost());
						res.put(data.getType().toLowerCase() + ".proxyPort", Integer.toString(data.getPort()));
					}
				}
				String nonProxiedHosts = String.join("|", service.getNonProxiedHosts());
				if (!nonProxiedHosts.isEmpty()) {
					res.put("http.nonProxyHosts", nonProxiedHosts);
					res.put("https.nonProxyHosts", nonProxiedHosts);
				}
			}
		}
		return res.entrySet().stream().map(entry -> "-D" + entry.getKey() + '=' + entry.getValue())
				.collect(Collectors.toSet());
	}

	/**
	 * Returns a list of XML extension jar paths. If the jar is contributed as a
	 * resource in the xmllsExtension extension point, it's path will be relative to
	 * the root of the contributing plug-in. If the jar is contributed through a
	 * class implementing XMLLSClasspathExtensionProvider, it's path will be
	 * absolute.
	 *
	 * @return List of extension jar paths (relative to the root of their
	 *         contributing plug-in, or absolute if provided by a class implementing
	 *         XMLLSClasspathExtensionProvider)
	 */
	private List<String> getExtensionJarPaths() {
		XMLExtensionRegistry extensionJarRegistry = new XMLExtensionRegistry();
		List<String> extensionJarPaths = extensionJarRegistry.getXMLExtensionJars();
		extensionJarPaths.addAll(extensionJarRegistry.getXMLLSClassPathExtensions());
		return extensionJarPaths;
	}

	private String computeJavaPath() {
		String javaPath = "java";
		boolean existsInPath = Stream.of(System.getenv("PATH")
				.split(Pattern.quote(File.pathSeparator)))
				.map(path -> path.startsWith("\"") && path.endsWith("\"") ? path.substring(1, path.length() - 1) : path)
				.map(Paths::get)
				.anyMatch(path -> Files.exists(path.resolve("java")));
		if (!existsInPath) {
			File f = new File(System.getProperty("java.home"),
					"bin/java" + (Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : ""));
			javaPath = f.getAbsolutePath();
		}
		return javaPath;
	}

	@Override
	public String toString() {
		return "XML Language Server: " + super.toString();
	}

}
