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
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceInitializer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class XMLLanguageServer extends ProcessStreamConnectionProvider {
	private static final String SETTINGS_KEY = "settings";
	private static final String XML_KEY = "xml";
	private static final String CATALOGS_KEY = "catalogs";
	
	private static final XMLExtensionRegistry extensionJarRegistry =new XMLExtensionRegistry();
 	private static final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 	private static final LanguageServerDefinition lemminxDefinition = LanguageServersRegistry.getInstance().getDefinition("org.eclipse.wildwebdeveloper.xml");
 	private static final IPropertyChangeListener psListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (XMLPreferenceInitializer.XML_PREFERENCES_CATAGLOGS.equals(event.getProperty())) {
				Map<String, Object> config = mergeCustomInitializationOptions(extensionJarRegistry.getInitiatizationOptions());

				@SuppressWarnings("rawtypes")
				DidChangeConfigurationParams params = new DidChangeConfigurationParams(
						Collections.singletonMap(XML_KEY, ((Map)config.get(SETTINGS_KEY)).get(XML_KEY)));
				LanguageServiceAccessor.getActiveLanguageServers(null).stream().filter(server -> lemminxDefinition.equals(LanguageServiceAccessor.resolveServerDefinition(server).get()))
					.forEach(ls -> ls.getWorkspaceService().didChangeConfiguration(params));
			}
		}
 	};
 	
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
					.toFileURL(getClass().getResource("/language-servers/server/org.eclipse.lemminx-uber.jar"));
			List<String> extensionJarPaths = getExtensionJarPaths();
			String uberJarPath = new java.io.File(url.getPath()).getAbsolutePath();
			jarPaths.add(uberJarPath);
			jarPaths.addAll(extensionJarPaths);
			commands.add(String.join(System.getProperty("path.separator"), jarPaths));
			commands.add("org.eclipse.lemminx.XMLServerLauncher");
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

	@Override
	public Object getInitializationOptions(URI rootUri) {
		return mergeCustomInitializationOptions(extensionJarRegistry.getInitiatizationOptions());
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, Object> mergeCustomInitializationOptions(Map<String, Object> defaults) {
		Map<String, Object> options = defaults == null ? new HashMap<String, Object>() : defaults;
		
		Set<String> catalogs = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

		// Default catalogs
		if (defaults.get(SETTINGS_KEY) instanceof Map) {
			Map settingsObj = (Map)defaults.get(SETTINGS_KEY);
			if (settingsObj.get(XML_KEY) instanceof Map) {
				Map xmlObj = (Map)settingsObj.get(XML_KEY);
				if (xmlObj.get(CATALOGS_KEY) instanceof String[]) {
					catalogs.addAll(Arrays.asList((String[])xmlObj.get(CATALOGS_KEY)));
				}
			}
		}
		// User defined XML Catalog
		XMLPreferenceInitializer.getCatalogs(store).forEach(f -> catalogs.add(f.getAbsolutePath()));
		
		// Build a merged catalog
		Map<String, Object> xmlObj = new HashMap<>();
		xmlObj.put(CATALOGS_KEY, catalogs);

		Map<String, Object> settingsObj = new HashMap<>();
		settingsObj.put(XML_KEY, xmlObj);

		options.put(SETTINGS_KEY, settingsObj);
		return options;
	}
	
	@Override
	public void start() throws IOException {
		super.start();
		store.addPropertyChangeListener(psListener); 
	}
	
	@Override
	public void stop() {
		store.removePropertyChangeListener(psListener);
		super.stop();
	}
}
