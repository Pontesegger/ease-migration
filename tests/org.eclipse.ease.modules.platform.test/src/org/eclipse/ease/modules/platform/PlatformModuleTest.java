/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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

package org.eclipse.ease.modules.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.ease.service.IScriptService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class PlatformModuleTest extends AbstractModuleTest {

	@Override
	protected Object getModuleClass() {
		return PlatformModule.class;
	}

	@Override
	protected Object getModuleID() {
		return PlatformModule.MODULE_ID;
	}

	// test does not run on hudson as no UI is available => no service registry
	@Disabled
	@Test
	public void getExistingService() {
		assertTrue(IScriptService.class.isAssignableFrom(PlatformModule.getService(IScriptService.class).getClass()));
	}

	// test does not run on hudson as no UI is available => no service registry
	@Disabled
	@Test
	public void getNonExistingService() {
		assertNull(PlatformModule.getService(PlatformModuleTest.class));
	}

	@Test
	public void getExistingSystemProperty() {
		assertNotNull(PlatformModule.getSystemProperty("java.home"));
	}

	@Test
	public void getNonExistingSystemProperty() {
		assertNull(PlatformModule.getSystemProperty("java.home.undefined"));
	}

	@Timeout(value = 3, unit = TimeUnit.SECONDS)
	public void runProcess() throws IOException, InterruptedException {
		final Process process = PlatformModule.runProcess("ls", new String[] { "-la" }, "keep", null);
		process.waitFor();

		assertTrue(process.getInputStream().available() > 0);
		assertEquals(0, process.getErrorStream().available());
	}
}
