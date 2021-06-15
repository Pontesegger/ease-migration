/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.dnd;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModulesDropHandlerTest {

	@Test
	@DisplayName("accepts() == true when element instanceOf ModuleDefinition")
	public void accepts_true_for_ModuleDefinition() {
		final ModuleDefinition mock = mock(ModuleDefinition.class);
		assertTrue(new ModulesDropHandler().accepts(null, mock));
	}

	@Test
	@DisplayName("accepts() == true when element instanceOf ModuleEntry")
	public void accepts_true_for_ModuleEntry() {
		final ModuleEntry mock = mock(ModuleEntry.class);
		assertTrue(new ModulesDropHandler().accepts(null, mock));
	}
}
