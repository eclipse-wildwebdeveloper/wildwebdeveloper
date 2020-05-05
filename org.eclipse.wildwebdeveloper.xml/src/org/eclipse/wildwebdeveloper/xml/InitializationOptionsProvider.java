/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @since 0.8.0
 */
public interface InitializationOptionsProvider extends Supplier<Map<String, Object>> {

}
