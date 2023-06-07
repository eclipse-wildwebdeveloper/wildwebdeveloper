/*******************************************************************************
 * Copyright (c) 2023 Dawid Pakuła and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Dawid Pakuła <zulus@w3des.net> - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue;

import java.util.Map;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public interface VueLanguageServerExtention {

	@JsonNotification(value = "vue/projectLoadingStart")
	public void projectLoadingStart(Object object);
	
	@JsonNotification(value = "vue/projectLoadingFinish")
	public void projectLoadingFinish(Object object);
	
	@JsonNotification(value = "vue/projectLanguageService")
	public void projectLanguageService(Map<String,Object> data);
}
