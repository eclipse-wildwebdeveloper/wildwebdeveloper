/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class IsNodeProjectPropertyTester extends PropertyTester {

	public static final String JAVASCRIPT_CONTENT_TYPE = "org.eclipse.tm4e.language_pack.javascript";
	public static final String TYPESCRIPT_CONTENT_TYPE = "org.eclipse.tm4e.language_pack.typescript";

	private static final String IS_NODE_RESOURCE_PROPERTY = "isNodeResource"; //$NON-NLS-1$

	@Override public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (IS_NODE_RESOURCE_PROPERTY.equals(property)) {
			IResource resource = Adapters.adapt(receiver, IResource.class);
			if (resource == null) {
				return false;
			}
			if (resource instanceof IFile file) {
				IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
				IContentType jsContentType = contentTypeManager.getContentType(JAVASCRIPT_CONTENT_TYPE);
				IContentType tsContentType = contentTypeManager.getContentType(TYPESCRIPT_CONTENT_TYPE);
				try (
					InputStream content = file.getContents();
				) {
					List<IContentType> contentTypes = Arrays.asList(contentTypeManager.findContentTypesFor(content, resource.getName()));
					return contentTypes.contains(jsContentType) || contentTypes.contains(tsContentType);
				} catch (Exception e) {
					return false;
				}
			}
		}
		return false;
	}

}
