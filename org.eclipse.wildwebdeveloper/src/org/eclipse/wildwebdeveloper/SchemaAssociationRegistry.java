/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;

public class SchemaAssociationRegistry {
	
	private SchemaAssociationRegistry() {}
	
	public static String translate(String url) {
		try {
			return FileLocator.toFileURL(new URL(url)).toString();
		} catch (IOException e) {
			return url;
		}
	}
}
