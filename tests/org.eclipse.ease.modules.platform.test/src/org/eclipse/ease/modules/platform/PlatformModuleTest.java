/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.ease.service.IScriptService;
import org.junit.Ignore;
import org.junit.Test;

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
	@Ignore
	@Test
	public void getExistingService() {
		assertTrue(IScriptService.class.isAssignableFrom(PlatformModule.getService(IScriptService.class).getClass()));
	}

	// test does not run on hudson as no UI is available => no service registry
	@Ignore
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

	@Test(timeout = 3000)
	public void runProcess() throws IOException, InterruptedException {
		final Process process = PlatformModule.runProcess("ls", new String[] { "-la" });
		process.waitFor();

		assertTrue(process.getInputStream().available() > 0);
	}
}
