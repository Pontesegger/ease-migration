/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScriptShellTest {

	@Test
	@DisplayName("ScriptShell implements ITabbedPropertySheetPageContributor")
	public void scriptShell_implements_ITabbedPropertySheetPageContributor() {
		assertTrue(new ScriptShell() instanceof ITabbedPropertySheetPageContributor);
	}

	@Test
	@DisplayName("getContributorId() returns View ID")
	public void getContributorId_returns_view_ID() {
		assertEquals("org.eclipse.ease.ui.views.scriptShell", new ScriptShell().getContributorId());
	}
}
