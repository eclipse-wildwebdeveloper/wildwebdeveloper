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

import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public class VueClientImpl extends LanguageClientImpl implements VueLanguageServerExtention {

	@Override
	public void projectLoadingFinish(Object object) {
		// TODO should this set some state because only now stuff will work like hover..
		// or maybe even after projectLanguageService "enabled" call
		logMessage(new MessageParams(MessageType.Info, "Vue project loading finished"));
	}
	
	@Override
	public void projectLoadingStart(Object object) {
		logMessage(new MessageParams(MessageType.Info, "Vue project loading started"));
	}
	
	@Override
	public void projectLanguageService(Map<String,Object> data) {
		logMessage(new MessageParams(MessageType.Info, "Language Service is " + (((Boolean)data.get("languageServiceEnabled")).booleanValue()?"":"not yet ") + "enabled for project " + data.get("projectName")));
	}
	
}
