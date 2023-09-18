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
 *  Dawid Pakuła - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.vue.autoinsert;

public class AutoInsertOptions {

	private AutoInsertLastChange lastChange;

	public AutoInsertLastChange getLastChange() {
		return lastChange;
	}

	public void setLastChange(AutoInsertLastChange lastChange) {
		this.lastChange = lastChange;
	}
	
}
