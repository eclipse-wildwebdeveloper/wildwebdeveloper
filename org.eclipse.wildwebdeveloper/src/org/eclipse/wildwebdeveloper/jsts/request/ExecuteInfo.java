/*******************************************************************************
 * Copyright (c) 2025 Dawid Pakuła and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Dawid Pakuła <zulus@w3des.net> - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.jsts.request;

public class ExecuteInfo {

	private int executionTarget = 0;

	private boolean expectsResult = true;

	private boolean isAsync = false;

	private boolean lowPriority = true;

	public int getExecutionTarget() {
		return executionTarget;
	}

	public void setExecutionTarget(int executionTarget) {
		this.executionTarget = executionTarget;
	}

	public boolean isExpectsResult() {
		return expectsResult;
	}

	public void setExpectsResult(boolean expectsResult) {
		this.expectsResult = expectsResult;
	}

	public boolean isAsync() {
		return isAsync;
	}

	public void setAsync(boolean isAsync) {
		this.isAsync = isAsync;
	}

	public boolean isLowPriority() {
		return lowPriority;
	}

	public void setLowPriority(boolean lowPriority) {
		this.lowPriority = lowPriority;
	}
}
