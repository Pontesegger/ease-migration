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

package org.eclipse.ease.lang.python.jython;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.ScriptResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JythonScriptEngineTest {

	@Test
	@DisplayName("toString(VOID) is null")
	public void toString_of_VOID_is_null() throws ExecutionException, CoreException, URISyntaxException, IOException {
		assertNull(new JythonScriptEngine().toString(ScriptResult.VOID));
	}

	@Test
	@DisplayName("toString(null) is null")
	public void toString_of_null_is_null() throws ExecutionException, CoreException, URISyntaxException, IOException {
		assertNull(new JythonScriptEngine().toString(null));
	}

	@Test
	@DisplayName("toString(Object) is not empty")
	public void toString_of_Object_is_not_empty() throws ExecutionException, CoreException, URISyntaxException, IOException {
		assertEquals("foo", new JythonScriptEngine().toString("foo"));
	}

}
