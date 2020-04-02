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
package org.eclipse.wildwebdeveloper;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;
import org.eclipse.ui.internal.genericeditor.GenericEditorWithContentTypeIcon;
import org.eclipse.ui.internal.genericeditor.GenericEditorWithIconAssociationOverride;

public class JSEditorAssociationOverride extends GenericEditorWithIconAssociationOverride {
	private final IEditorDescriptor genericEditorWithJSIcon;
	private final IContentType wildWebDeveloperJSContentType;

	public JSEditorAssociationOverride() {
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor genericEditorDescriptor = editorReg.findEditor(ExtensionBasedTextEditor.GENERIC_EDITOR_ID);
		genericEditorWithJSIcon = genericEditorDescriptor != null ? new GenericEditorWithContentTypeIcon("*.js", genericEditorDescriptor) : null;
		wildWebDeveloperJSContentType = Platform.getContentTypeManager().getContentType("org.eclipse.wildwebdeveloper.js");
	}
	
	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		return editorInput != null ? overrideDefaultEditor(editorInput.getName(), contentType, editorDescriptor)
				: editorDescriptor;
	}
	
	// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=559907
	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		if (wildWebDeveloperJSContentType != null && genericEditorWithJSIcon != null) {
			if(wildWebDeveloperJSContentType.equals(contentType) || 
					wildWebDeveloperJSContentType.isAssociatedWith(fileName)) { 
				return genericEditorWithJSIcon;
			}
		}
		return super.overrideDefaultEditor(fileName, contentType, editorDescriptor);
	}
}
