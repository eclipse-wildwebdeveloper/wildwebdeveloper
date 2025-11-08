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
 * Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ExtendWith(AllCleanRule.class)
public class TestXML {

	private IProject project;
	private ICompletionProposal[] proposals;

	@BeforeEach
	public void setUpProject() throws CoreException {
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
	}

	@Test
	public void testXMLFile() throws Exception {
		final IFile file = project.getFile("blah.xml");
		file.create("FAIL".getBytes(), true, false, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("<plugin></");
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
			try {
				return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
			} catch (CoreException e) {
				return false;
			}
		}), "Diagnostic not published");
	}

	@Test
	public void testXSLFile() throws Exception {
		final IFile file = project.getFile("blah.xsl");
		file.create("FAIL".getBytes(), true, false, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("FAIL");
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
			try {
				return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
			} catch (CoreException e) {
				return false;
			}
		}), "Diagnostic not published");
	}

	@Test
	public void testXSDFile() throws Exception {
		final IFile file = project.getFile("blah.xsd");
		file.create("FAIL".getBytes(), true, false, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("a<");
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
			try {
				return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
			} catch (CoreException e) {
				return false;
			}
		}), "Diagnostic not published");
	}

	@Test
	public void testDTDFile() throws Exception {
		final IFile file = project.getFile("blah.dtd");
		file.create("FAIL".getBytes(), true, false, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("<!--<!-- -->");
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
			try {
				return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
			} catch (CoreException e) {
				return false;
			}
		}), "Diagnostic not published");
	}

	@Test
	public void testComplexXML() throws Exception {
		final IFile file = project.getFile("blah.xml");
		String content = "<layout:BlockLayoutCell\n" + "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	\n"
				+ "    xsi:schemaLocation=\"sap.ui.layout https://openui5.hana.ondemand.com/downloads/schemas/sap.ui.layout.xsd\"\n"
				+ "	xmlns:layout=\"sap.ui.layout\">\n" + "    |\n" + "</layout:BlockLayoutCell>";
		int offset = content.indexOf('|');
		content = content.replace("|", "");
		file.create(content.getBytes(), true, false, null);
		AbstractTextEditor editor = (AbstractTextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				file, "org.eclipse.ui.genericeditor.GenericEditor");
		editor.getSelectionProvider().setSelection(new TextSelection(offset, 0));
		LSContentAssistProcessor processor = new LSContentAssistProcessor();
		proposals = processor.computeCompletionProposals(Utils.getViewer(editor), offset);
		DisplayHelper.sleep(editor.getSite().getShell().getDisplay(), 2000);
		assertTrue(proposals.length > 1);
	}

	@Test
	public void autoCloseTags() throws Exception {
		final IFile file = project.getFile("autoCloseTags.xml");
		file.create("<foo".getBytes(), true, false, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		document.replace(4, 0, ">");
		assertTrue(DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> "<foo></foo>".equals(document.get())),
				"Autoclose not done");
	}

	/**
	 * Tests the creation of the system catalog with schemas that have been
	 * contributed. This tests as well the use of jar-URLs to allow relative
	 * includes within schemas (Issue #1078)
	 */
	@Test
	public void testXMLCatalog() throws Exception {
		// Create bundle with a catalog contribution
		Path bundlePath = Files.createTempFile("xmlCatalogTest", "bundle.jar");
		bundlePath.toFile().deleteOnExit();

		try (FileOutputStream bundleOutput = new FileOutputStream(bundlePath.toFile());
				JarOutputStream bundleJarOut = new JarOutputStream(bundleOutput)) {

			// Manifest
			bundleJarOut.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
			PrintWriter fileOut = new PrintWriter(bundleJarOut, true, StandardCharsets.UTF_8);
			fileOut.println("Manifest-Version: 1.0");
			fileOut.println("Bundle-ManifestVersion: 2");
			fileOut.println("Bundle-Name: XML-Catalog-Test");
			fileOut.println("Bundle-SymbolicName: org.eclipse.wildwebdeveloper.test.xmlcatalog;singleton:=true");
			fileOut.println("Bundle-Version: 0.0.1");
			fileOut.println("Require-Bundle: org.eclipse.wst.xml.core");

			// plugin.xml
			bundleJarOut.putNextEntry(new JarEntry("plugin.xml"));
			fileOut = new PrintWriter(bundleJarOut, true, StandardCharsets.UTF_8);
			fileOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			fileOut.println("<?eclipse version=\"3.0\"?>");
			fileOut.println("<plugin>");
			fileOut.println("<extension point=\"org.eclipse.wst.xml.core.catalogContributions\">");
			fileOut.println("<catalogContribution id=\"Test\">");
			fileOut.println("<uri name=\"http://eclipse.org/wildwebdeveloper/test\" ");
			fileOut.println("uri=\"/org/eclipse/wildwebdeveloper/test/schema.xsd\"/>");
			fileOut.println("</catalogContribution>");
			fileOut.println("</extension>");
			fileOut.println("</plugin>");

			// Schema in Jar
			bundleJarOut.putNextEntry(new JarEntry("org/eclipse/wildwebdeveloper/test/schema.xsd"));
			fileOut = new PrintWriter(bundleJarOut, true, StandardCharsets.UTF_8);
			fileOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			fileOut.println(
					"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://eclipse.org/wildwebdeveloper/test\" xmlns:com=\"http://acme.com/order\">");
			fileOut.println("<xs:element name=\"order\" type=\"com:ordertype\" />");
			fileOut.println("<xs:complexType name=\"ordertype\">");
			fileOut.println("<xs:sequence>");
			fileOut.println("<xs:element name=\"person\" type=\"xs:string\" />");
			fileOut.println("<xs:element name=\"item\" maxOccurs=\"unbounded\" type=\"xs:string\" />");
			fileOut.println("</xs:sequence>");
			fileOut.println("<xs:attribute name=\"orderid\" type=\"xs:string\" use=\"required\" />");
			fileOut.println("</xs:complexType>");
			fileOut.println("</xs:schema>");

			bundleJarOut.closeEntry();
		}

		// Install and start bundle
		Plugin plugin = org.eclipse.wildwebdeveloper.xml.internal.Activator.getDefault();
		BundleContext bundleContext = plugin.getBundle().getBundleContext();
		URL bundleUrl = bundlePath.toUri().toURL();
		Bundle catalogBundle = bundleContext.installBundle(bundleUrl.toExternalForm(), bundleUrl.openStream());
		catalogBundle.start();

		// Open preferences dialog to trigger the refresh of the system catalog
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLCatalogPreferencePage", null, null);
		dialog.getShell().open();
		dialog.getShell().close();

		// Find system.catalog in well known location from plugin
		File systemCatalog = plugin.getStateLocation().append("system-catalog.xml").toFile();
		// Parse system-catalog to check it
		Document systemCatalogDom = runWithLocalCatalogOnlyForTestXMLCatalog(() -> DocumentBuilderFactory.newDefaultInstance()
				.newDocumentBuilder().parse(systemCatalog));

		// root
		Node catalogNode = systemCatalogDom.getLastChild();
		assertEquals(Node.ELEMENT_NODE, catalogNode.getNodeType());
		assertEquals("catalog", catalogNode.getNodeName());

		// find URI-entries
		NodeList catalogEntries = catalogNode.getChildNodes();
		List<Node> uriNodes = IntStream.range(0, catalogEntries.getLength()).mapToObj(catalogEntries::item).filter(n -> n
				.getNodeType() == Node.ELEMENT_NODE).filter(n -> "uri".equals(n.getNodeName())).collect(Collectors.toList());
		assertFalse(uriNodes.isEmpty(), "uri-nodes expected");

		// find expected entry
		List<Node> expectedNodes = uriNodes.stream().filter(n -> "http://eclipse.org/wildwebdeveloper/test".equals(n.getAttributes()
				.getNamedItem("name").getNodeValue())).collect(Collectors.toList());
		assertEquals(1, expectedNodes.size(), "one uri-node with the used name expected");
		Node uriNode = expectedNodes.get(0);

		// value of uri
		assertNotNull(uriNode.getAttributes().getNamedItem("uri"), "uri-attribute expected");
		String uri = uriNode.getAttributes().getNamedItem("uri").getNodeValue();
		assertNotNull(uri, "value fro uri expected");
		// use of jar-uri - file is not cached in local filesystem. This enables
		// relative includes in schemas
		assertTrue(uri.startsWith("jar:file:/"), "jar-uri expected: " + uri);
		assertTrue(uri.endsWith("/org/eclipse/wildwebdeveloper/test/schema.xsd"), "relative path of schema in uri expected: " + uri);

		// Uninstall bundle once again
		catalogBundle.uninstall();
	}

	/**
	 * Workaround for erratic runtime exceptions because of catalog.dtd temporarily not being available online:
	 *
	 * <pre>{@code
	 *   java.io.FileNotFoundException: http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd
	 *       at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2010)
	 *       at ...
	 *       at java.xml/javax.xml.parsers.DocumentBuilder.parse(DocumentBuilder.java:206)
	 *       at org.eclipse.wildwebdeveloper.tests.TestXML.testXMLCatalog(TestXML.java:246)
	 *       at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	 *       at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	 *       at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	 * }</pre>
	 */
	private <T> T runWithLocalCatalogOnlyForTestXMLCatalog(Callable<T> callable) throws Exception {
		var origSysProps = new HashMap<String, String>();
		List.of( //
				"javax.xml.useCatalog", //
				"javax.xml.catalog.files", //
				"javax.xml.catalog.prefer", //
				"javax.xml.catalog.resolve" //
		).forEach(k -> origSysProps.put(k, System.getProperty(k)));

		var catalogRedirect = Path.of("src/org/eclipse/wildwebdeveloper/tests/catalog/oasis-catalog-redirect.xml");
		assertTrue(Files.exists(catalogRedirect));
		// enforce usage of local catalog.dtd file
		System.setProperty("javax.xml.useCatalog", "true");
		System.setProperty("javax.xml.catalog.files", catalogRedirect.toUri().toString());
		System.setProperty("javax.xml.catalog.prefer", "system");
		System.setProperty("javax.xml.catalog.resolve", "strict");
		try {
			return callable.call();
		} finally {
			// restore previous behaviour
			for (var e : origSysProps.entrySet()) {
				if (e.getValue() == null) {
					System.clearProperty(e.getKey());
				} else {
					System.setProperty(e.getKey(), e.getValue());
				}
			}
		}
	}

	/**
	 * Verifies that HTTPS catalog URIs contributed via the WTP catalog
	 * are included in Wild Web Developer's generated system catalog.
	 *
	 * Expected behavior: the HTTPS URI is preserved in system-catalog.xml.
	 * Current behavior (issue #1892): WWD filters non file/jar schemes and
	 * drops the entry, so this test should fail until the code accepts http/https.
	 */
	@Test
	public void testXMLCatalogHttpsUriIncluded() throws Exception {
		final String HTTPS_NAME = "http://eclipse.org/wildwebdeveloper/https-test";
		final String HTTPS_URI = "https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd";

		// Create a bundle that contributes an HTTPS-based catalog entry
		Path bundlePath = Files.createTempFile("xmlCatalogHttpsTest", "bundle.jar");
		bundlePath.toFile().deleteOnExit();

		try (var bundleOutput = new FileOutputStream(bundlePath.toFile());
				var bundleJarOut = new JarOutputStream(bundleOutput)) {

			// Manifest
			bundleJarOut.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
			bundleJarOut.write("""
				Manifest-Version: 1.0
				Bundle-ManifestVersion: 2
				Bundle-Name: XML-Catalog-HTTPS-Test
				Bundle-SymbolicName: org.eclipse.wildwebdeveloper.test.xmlcatalog.https;singleton:=true
				Bundle-Version: 0.0.1
				Require-Bundle: org.eclipse.wst.xml.core
				""".getBytes(StandardCharsets.UTF_8));
			bundleJarOut.closeEntry();

			// plugin.xml with HTTPS catalog contribution
			bundleJarOut.putNextEntry(new JarEntry("plugin.xml"));
			bundleJarOut.write("""
				<?xml version="1.0" encoding="UTF-8"?>
				<?eclipse version="3.0"?>
				<plugin>
				  <extension point="org.eclipse.wst.xml.core.catalogContributions">
				    <catalogContribution id="TestHttps">
				      <uri name="%s" uri="%s"/>
				    </catalogContribution>
				  </extension>
				</plugin>
				""".formatted(HTTPS_NAME, HTTPS_URI).getBytes(StandardCharsets.UTF_8));
			bundleJarOut.closeEntry();
		}

		// Install and start the bundle
		Plugin plugin = org.eclipse.wildwebdeveloper.xml.internal.Activator.getDefault();
		BundleContext bundleContext = plugin.getBundle().getBundleContext();
		URL bundleUrl = bundlePath.toUri().toURL();
		Bundle catalogBundle;
		try (var is = bundleUrl.openStream()) {
			catalogBundle = bundleContext.installBundle(bundleUrl.toExternalForm(), is);
			catalogBundle.start();
		}

		// Open preferences dialog to trigger the refresh of the system catalog
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"org.eclipse.wildwebdeveloper.xml.internal.ui.preferences.XMLCatalogPreferencePage", null, null);
		dialog.getShell().open();
		dialog.getShell().close();

		// Find system-catalog and parse it
		File systemCatalog = plugin.getStateLocation().append("system-catalog.xml").toFile();
		Document systemCatalogDom = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(systemCatalog);

		// root element
		Node catalogNode = systemCatalogDom.getLastChild();
		assertEquals(Node.ELEMENT_NODE, catalogNode.getNodeType());
		assertEquals("catalog", catalogNode.getNodeName());

		// collect <uri> entries
		NodeList catalogEntries = catalogNode.getChildNodes();
		List<Node> uriNodes = IntStream.range(0, catalogEntries.getLength()).mapToObj(catalogEntries::item).filter(n -> n
				.getNodeType() == Node.ELEMENT_NODE).filter(n -> "uri".equals(n.getNodeName())).toList();
		assertFalse(uriNodes.isEmpty(), "uri-nodes expected");

		// find contributed HTTPS entry
		List<Node> expectedNodes = uriNodes.stream().filter(n -> HTTPS_NAME.equals(n.getAttributes().getNamedItem("name").getNodeValue()))
				.toList();
		assertEquals(1, expectedNodes.size(), "one https uri-node with the used name expected");
		Node uriNode = expectedNodes.get(0);

		// ensure the https URI is preserved
		assertNotNull(uriNode.getAttributes().getNamedItem("uri"), "uri-attribute expected");
		String uri = uriNode.getAttributes().getNamedItem("uri").getNodeValue();
		assertEquals(HTTPS_URI, uri, "https uri value should be preserved in catalog");

		// cleanup
		catalogBundle.uninstall();
	}
}
