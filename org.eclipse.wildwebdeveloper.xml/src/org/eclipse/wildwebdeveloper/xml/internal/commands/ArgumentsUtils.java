/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.commands;

import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Arguments utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class ArgumentsUtils {

	private static Gson gson = getDefaultGsonBuilder().create();

	/**
	 * Returns the object from the given index as a given class type and null
	 * otherwise.
	 * 
	 * @param <T>     the class type
	 * @param command the LSP command.
	 * @param index   the index
	 * @param clazz   the class type.
	 * @return the object from the given index as a given class type and null
	 *         otherwise.
	 * @throws UnsupportedOperationException if the object from the given index
	 *                                       cannot be retrieved from the params.
	 */
	public static <T> T getArgAt(Command command, int index, Class<T> clazz) {
		Object obj = getArgAt(command.getArguments(), index);
		return toModel(obj, clazz);
	}

	/**
	 * Returns the object from the given index and null otherwise.
	 * 
	 * @param arguments the argument list.
	 * @param index     the index.
	 * 
	 * @return the object from the given index and null otherwise.
	 */
	public static Object getArgAt(List<Object> arguments, int index) {
		if (arguments == null || index >= arguments.size()) {
			return null;
		}
		return arguments.get(index);
	}

	private static <T> T toModel(Object object, Class<T> clazz) {
		if (object == null) {
			return null;
		}
		if (clazz == null) {
			throw new IllegalArgumentException("Class can not be null");
		}
		if (object instanceof JsonElement) {
			return gson.fromJson((JsonElement) object, clazz);
		}
		if (object instanceof Map) {
			return gson.fromJson(gson.toJsonTree(object), clazz);
		}
		if (clazz.isInstance(object)) {
			return clazz.cast(object);
		}
		return null;
	}

	private static GsonBuilder getDefaultGsonBuilder() {
		return new GsonBuilder() //
				.registerTypeAdapterFactory(new EnumTypeAdapter.Factory()) //
				.registerTypeAdapterFactory(new EitherTypeAdapter.Factory());
	}

}
