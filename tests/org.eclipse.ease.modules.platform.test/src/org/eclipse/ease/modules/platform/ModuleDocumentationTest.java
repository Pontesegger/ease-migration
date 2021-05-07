/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.core.runtime.Platform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModuleDocumentationTest {

	@Test
	@DisplayName("module documentation exists")
	public void module_documentation_exists() {
		assertNotNull(Platform.getBundle("org.eclipse.ease.modules.platform").getEntry("/help/module_org.eclipse.ease.modules.platform.platform.html"));
	}
}
