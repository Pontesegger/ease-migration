/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.testhelper.python;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.ScriptResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractPep302ModuleLoadingTest {

	private IReplEngine fEngine;

	abstract protected IReplEngine createEngine();

	@Before
	public void setup() {
		fEngine = createEngine();
		fEngine.setTerminateOnIdle(false);
	}

	@After
	public void teardown() {
		fEngine.terminate();
	}

	@Test
	public void loadSimpleModule() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync("import eclipse.test.basic");

		// make sure the import does not throw
		assertFalse(result.hasException());
	}

	@Test
	public void loadModuleWithDependencies() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync("import eclipse.test.advanced");

		// make sure the import does not throw
		assertFalse(result.hasException());
	}

	@Test
	public void loadNotExistingModule() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync("import eclipse.test.notthere");

		assertTrue(result.hasException());
	}

	@Test
	public void accessModule() throws InterruptedException {
		fEngine.executeSync("import eclipse.test.basic");
		final ScriptResult result = fEngine.executeSync("eclipse.test.basic.add(2, 4)");

		assertFalse(result.hasException());
		assertEquals(6, result.getResult());
	}

	@Test
	public void accessAdvancedModule() throws InterruptedException {
		fEngine.executeSync("import eclipse.test.advanced");
		final ScriptResult result = fEngine.executeSync("eclipse.test.advanced.area(2, 4)");

		assertFalse(result.hasException());
		assertEquals(8, result.getResult());
	}

	@Test
	public void importAs() throws InterruptedException {
		fEngine.executeSync("import eclipse.test.basic as basic");
		final ScriptResult result = fEngine.executeSync("basic.add(2, 4)");

		assertFalse(result.hasException());
		assertEquals(6, result.getResult());
	}

	@Test
	public void fromBasicImportAll() throws InterruptedException {
		fEngine.executeSync("from eclipse.test.basic import *");
		final ScriptResult result = fEngine.executeSync("add(2, 4)");

		assertFalse(result.hasException());
		assertEquals(6, result.getResult());
	}

	@Test
	public void fromBasicImportMethod() throws InterruptedException {
		fEngine.executeSync("from eclipse.test.basic import add");
		final ScriptResult result = fEngine.executeSync("add(2, 4)");

		assertFalse(result.hasException());
		assertEquals(6, result.getResult());
	}
}
