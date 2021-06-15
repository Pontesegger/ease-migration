/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
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

package org.eclipse.ease.testhelper.python;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractPep302ModuleLoadingTest {

	private IReplEngine fEngine;

	protected abstract IReplEngine createEngine();

	@BeforeEach
	public void setup() {
		fEngine = createEngine();
		fEngine.setTerminateOnIdle(false);
	}

	@AfterEach
	public void teardown() {
		fEngine.terminate();
	}

	@Test
	public void loadSimpleModule() {
		assertDoesNotThrow(() -> fEngine.execute("import eclipse.test.basic").get());
	}

	@Test
	public void loadModuleWithDependencies() {
		assertDoesNotThrow(() -> fEngine.execute("import eclipse.test.advanced").get());
	}

	@Test
	public void loadNotExistingModule() {
		assertThrows(ScriptExecutionException.class, () -> fEngine.execute("import eclipse.test.notthere").get());
	}

	@Test
	public void accessModule() throws ExecutionException {
		fEngine.execute("import eclipse.test.basic");
		final ScriptResult result = fEngine.execute("eclipse.test.basic.add(2, 4)");

		assertEquals(6, result.get());
	}

	@Test
	public void accessAdvancedModule() throws ExecutionException {
		fEngine.execute("import eclipse.test.advanced");
		final ScriptResult result = fEngine.execute("eclipse.test.advanced.area(2, 4)");

		assertEquals(8, result.get());
	}

	@Test
	public void importAs() throws ExecutionException {
		fEngine.execute("import eclipse.test.basic as basic");
		final ScriptResult result = fEngine.execute("basic.add(2, 4)");

		assertEquals(6, result.get());
	}

	@Test
	public void fromBasicImportAll() throws ExecutionException {
		fEngine.execute("from eclipse.test.basic import *");
		final ScriptResult result = fEngine.execute("add(2, 4)");

		assertEquals(6, result.get());
	}

	@Test
	public void fromBasicImportMethod() throws ExecutionException {
		fEngine.execute("from eclipse.test.basic import add");
		final ScriptResult result = fEngine.execute("add(2, 4)");

		assertEquals(6, result.get());
	}
}
