/*******************************************************************************
 * Copyright (c) 2019, 2023 Red Hat Inc. and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLPreferenceServerConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

@SuppressWarnings("restriction")
public class XMLLanguageServer extends ProcessStreamConnectionProvider {
	
	private static final String XML_LANGUAGE_SERVER_ID = "org.eclipse.wildwebdeveloper.xml";
	
	private static final String SETTINGS_KEY = "settings";
	private static final String XML_KEY = "xml";

	// Extended capabilities for set CodeLens capabilities
	private static final String EXTENDED_CLIENT_CAPABILITIES_KEY = "extendedClientCapabilities";
	private static final String CODE_LENS_KEY = "codeLens";
	private static final String CODE_LENS_KIND_KEY = "codeLensKind";
	private static final String VALUE_SET_KEY = "valueSet";
	private static final String BINDING_WIZARD_SUPPORT_KEY = "bindingWizardSupport";
	
	private static enum CodeLensKind {
		association;
	}
	
	private static final XMLExtensionRegistry extensionJarRegistry = new XMLExtensionRegistry();
	private static final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	private static final LanguageServerDefinition lemminxDefinition = LanguageServersRegistry.getInstance()
			.getDefinition(XML_LANGUAGE_SERVER_ID);
	
	private final IPropertyChangeListener psListener = event -> {
		XMLPreferenceServerConstants.getLemminxPreference(event).ifPresent(pref -> {
			Map<String, Object> config = mergeCustomInitializationOptions(
					extensionJarRegistry.getInitiatizationOptions());

			@SuppressWarnings("rawtypes")
			DidChangeConfigurationParams params = new DidChangeConfigurationParams(
					Collections.singletonMap(XML_KEY, ((Map) config.get(SETTINGS_KEY)).get(XML_KEY)));

			LanguageServers.forProject(null).withPreferredServer(lemminxDefinition).excludeInactive()
					.collectAll((w, ls) -> CompletableFuture.completedFuture(ls)).thenAccept(
							lss -> lss.stream().forEach(ls -> ls.getWorkspaceService().didChangeConfiguration(params)));
		});
	};

	private final String logLevelString;

	public XMLLanguageServer() {
		List<String> commands = new ArrayList<>();
		List<String> jarPaths = new ArrayList<>();
		commands.add(computeJavaPath());
		commands.addAll(getProxySettings());
		String debugPortString = System.getProperty(getClass().getName() + ".debugPort");
		if (debugPortString != null) {
			commands.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + debugPortString);
		}
		logLevelString = System.getProperty(getClass().getName() + ".log.level");
		if (logLevelString != null) {
			commands.add("-Dlog.level=" + logLevelString); // defined in org.eclipse.lemminx.logs.LogHelper
		}
		commands.add("-Duser.name=" + System.getProperty("user.name"));
		commands.add("-Duser.home=" + System.getProperty("user.home"));
		commands.add("-classpath");
		try {
			Bundle lemminxBundle = getLemminxBundle();
			File file = FileLocator.getBundleFileLocation(lemminxBundle)
					.orElseThrow(() -> new IllegalStateException("Can't determine lemminx file location"));
			List<String> extensionJarPaths = getExtensionJarPaths();
			String uberJarPath = file.getAbsolutePath();
			jarPaths.add(uberJarPath);
			jarPaths.addAll(extensionJarPaths);
			commands.add(String.join(System.getProperty("path.separator"), jarPaths));
			String mainClass = lemminxBundle.getHeaders().get("Main-Class");
			commands.add(mainClass);
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));
		} catch (RuntimeException e) {
			ILog.get().error(e.getMessage(), e);
		}
	}

	private Bundle getLemminxBundle() {
		Bundle self = FrameworkUtil.getBundle(getClass());
		BundleWiring wiring = self.adapt(BundleWiring.class);
		List<BundleWire> wires = wiring.getRequiredWires("osgi.wiring.bundle");
		for (BundleWire bundleWire : wires) {
			Bundle bundle = bundleWire.getProvider().getBundle();
			if (bundle.getSymbolicName().equals("org.eclipse.lemminx.uber-jar")) {
				return bundle;
			}
		}
		throw new IllegalStateException("can't find the lemminx bundle!");
	}

	private Collection<? extends String> getProxySettings() {
		Map<String, String> res = new HashMap<>();
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			if (entry.getKey() instanceof String property && entry.getValue() instanceof String value) {
				if (property.toLowerCase().contains("proxy") || property.toLowerCase().contains("proxies")) {
					res.put(property, value);
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
					if (data.getUserId() != null) {
						res.put(data.getType().toLowerCase() + ".proxyUser", data.getUserId());
					}
					if (data.getPassword() != null) {
						res.put(data.getType().toLowerCase() + ".proxyPassword", data.getPassword());
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
		return new File(System.getProperty("java.home"),
				"bin/java" + (Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : "")).getAbsolutePath();
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		Map<String, Object> initializationOptions = new HashMap<>();
		Map<String, Object> settings = mergeCustomInitializationOptions(
				extensionJarRegistry.getInitiatizationOptions());
		initializationOptions.put(SETTINGS_KEY, settings.get(SETTINGS_KEY));
		Object extendedClientCapabilities = createExtendedClientCapabilities();
		initializationOptions.put(EXTENDED_CLIENT_CAPABILITIES_KEY, extendedClientCapabilities);
		return initializationOptions;
	}

	private static Object createExtendedClientCapabilities() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		Map<String, Object> codeLens = new HashMap<>();
		extendedClientCapabilities.put(CODE_LENS_KEY, codeLens);
		Map<String, Object> codeLensKind = new HashMap<>();
		codeLens.put(CODE_LENS_KIND_KEY, codeLensKind);
		List<String> valueSet = Arrays.asList(CodeLensKind.association.name());
		codeLensKind.put(VALUE_SET_KEY, valueSet);
		extendedClientCapabilities.put(BINDING_WIZARD_SUPPORT_KEY, Boolean.TRUE);
		return extendedClientCapabilities;
	}
	
	private Map<String, Object> mergeCustomInitializationOptions(Map<String, Object> defaults) {
		Map<String, Object> xmlOpts = new HashMap<>(defaults);
		XMLPreferenceServerConstants.storePreferencesToLemminxOptions(store, xmlOpts);
		if (logLevelString != null) {
			xmlOpts.put("logs", Map.of("file", new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), ".metadata/lemminx.log").getAbsolutePath()));
		}
		return Map.of(SETTINGS_KEY, Map.of(XML_KEY, xmlOpts));
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
	
	@Override
	public String toString() {
		return "XML Language Server: " + super.toString();
	}
}
