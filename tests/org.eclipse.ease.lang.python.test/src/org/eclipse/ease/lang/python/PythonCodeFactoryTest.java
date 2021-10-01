/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.service.EngineDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PythonCodeFactoryTest {
	// @formatter:off
	private static final String PY4J_VOID_METHOD = "def voidMethod():\n"
			+ "    if not __EASE_MOD_org_eclipse_ease_modules_EnvironmentModule.hasMethodCallback(\"1234\"):\n"
			+ "        MODULE.voidMethod()\n"
			+ "        return org.eclipse.ease.ScriptResult.VOID\n"
			+ "    \n"
			+ "    else:\n"
			+ "        parameters_array = gateway.new_array(gateway.jvm.Object, 0)\n"
			+ "        __EASE_MOD_org_eclipse_ease_modules_EnvironmentModule.preMethodCallback(\"1234\", parameters_array)\n"
			+ "        __result = MODULE.voidMethod()\n"
			+ "        __EASE_MOD_org_eclipse_ease_modules_EnvironmentModule.postMethodCallback(\"1234\", __result)\n"
			+ "        return org.eclipse.ease.ScriptResult.VOID\n"
			+ "\n"
			+ "voidMethod.__ease__ = True\n"
			+ "\n";

	private static final String PY4J_INT_METHOD = "def intMethodWithParameters(a, b):\n"
			+ "    if not __EASE_MOD_org_eclipse_ease_modules_EnvironmentModule.hasMethodCallback(\"1234\"):\n"
			+ "        return MODULE.intMethodWithParameters(a, b)\n"
			+ "    \n"
			+ "    else:\n"
			+ "        parameters_array = gateway.new_array(gateway.jvm.Object, 2)\n"
			+ "        if a != None:\n"
			+ "            parameters_array[0] = a\n"
			+ "        if b != None:\n"
			+ "            parameters_array[1] = b\n"
			+ "        __EASE_MOD_org_eclipse_ease_modules_EnvironmentModule.preMethodCallback(\"1234\", parameters_array)\n"
			+ "        __result = MODULE.intMethodWithParameters(a, b)\n"
			+ "        __EASE_MOD_org_eclipse_ease_modules_EnvironmentModule.postMethodCallback(\"1234\", __result)\n"
			+ "        return __result\n"
			+ "\n"
			+ "intMethodWithParameters.__ease__ = True\n"
			+ "\n";
	// @formatter:on

	private static String replaceMocks(String code) {
		return code.replaceAll("IEnvironment\\$MockitoMock\\$\\d+", "EnvironmentModule");
	}

	private PythonCodeFactory fFactory;

	@BeforeEach
	public void beforeEach() {
		fFactory = new PythonCodeFactory();
	}

	@Test
	public void testCommentCreator() {
		assertEquals("# Comment", fFactory.createCommentedString("Comment", false));
		assertEquals(String.format("# Multi%n# Line%n# Comment"), fFactory.createCommentedString("Multi\nLine\nComment", false));
		assertEquals(String.format("\"\"\"Multi%nLine%nComment\"\"\""), fFactory.createCommentedString("Multi\nLine\nComment", true));
	}

	@Test
	@DisplayName("createFunctionWrapper() creates void return types for Py4J")
	public void createFunctionWrapper_creates_void_return_types_for_Py4J() throws NoSuchMethodException {
		final IEnvironment environment = mock(IEnvironment.class);
		when(environment.registerMethod(any())).thenReturn("1234");
		mockPy4JEngine(environment);

		final Method voidMethod = getClass().getMethod("voidMethod");
		final String code = fFactory.createFunctionWrapper(environment, "MODULE", voidMethod);
		assertEquals(PY4J_VOID_METHOD, replaceMocks(code));
	}

	@Test
	@DisplayName("createFunctionWrapper() creates return and parameters for Py4J")
	public void createFunctionWrapper_creates_return_andParameters_for_Py4J() throws NoSuchMethodException {
		final IEnvironment environment = mock(IEnvironment.class);
		when(environment.registerMethod(any())).thenReturn("1234");
		mockPy4JEngine(environment);

		final Method intMethod = getClass().getMethod("intMethodWithParameters", String.class, int.class);
		final String code = fFactory.createFunctionWrapper(environment, "MODULE", intMethod);
		assertEquals(PY4J_INT_METHOD, replaceMocks(code));
	}

	private void mockPy4JEngine(IEnvironment environment) {
		final EngineDescription description = mock(EngineDescription.class);
		when(description.getID()).thenReturn("org.eclipse.ease.lang.python.py4j");

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getDescription()).thenReturn(description);

		when(environment.getScriptEngine()).thenReturn(engine);
	}

	public void voidMethod() {
	}

	public int intMethodWithParameters(String a, int b) {
		return 0;
	}
}
