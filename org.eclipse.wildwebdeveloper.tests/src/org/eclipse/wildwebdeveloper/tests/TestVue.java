package org.eclipse.wildwebdeveloper.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Rule;
import org.junit.Test;

public class TestVue {


	@Rule 
	public AllCleanRule cleanRule = new AllCleanRule();
	
	@Test
	public void testVueFileWithJSScript() throws CoreException, IOException, InterruptedException {

		IProject project = Utils.provisionTestProject("vue-app");
		
		Process process = new ProcessBuilder(Utils.getNpmLocation(), "install", "--no-bin-links", "--ignore-scripts")
				.directory(project.getLocation().toFile()).start();
		assertEquals("npm ci didn't complete property", 0, process.waitFor());
		
		
		IFile appComponentFile = project.getFolder("src").getFile("App.vue");
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), appComponentFile);
		DisplayHelper.sleep(4000); // Give time for LS to initialize enough before making edit and sending a didChange
		// make an edit
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		document.set(document.get() + "\n");

		assertTrue("Diagnostic not published", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
							IResource.DEPTH_ZERO).length == 10;
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 50000));

		IMarker[] markers = appComponentFile.findMarkers("org.eclipse.lsp4e.diagnostic", true,
				IResource.DEPTH_ZERO);
		boolean foundError = false;
		for (IMarker marker : markers) {
			int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			if (lineNumber == 10 && marker.getAttribute(IMarker.MESSAGE, "").contains( 
					"Declaration or statement expected")) {
				foundError = true;
			}
		}
		assertTrue("No error found in line 10", foundError);
	}
	
}
