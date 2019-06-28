/*******************************************************************************
 * Copyright (c) 2017 Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.IScriptable;
import org.eclipse.ease.ScriptResult;
import org.junit.Before;
import org.junit.Test;

public abstract class ModeTestBase extends Py4JEngineTestBase {
	abstract protected ScriptResult executeCode(String code) throws Exception;

	abstract protected void executeCode(String code, Object result) throws Exception;

	@Before
	public void loadModules() throws Exception {
		executeCode("loadModule('/Py4jTests')");
	}

	@Test
	public void pythonInteger() throws Exception {
		executeCode("40 + 2", 42);
	}

	@Test
	public void pythonString() throws Exception {
		executeCode("'42'", "42");
	}

	@Test
	public void javaInteger() throws Exception {
		executeCode("java.lang.Integer(42)", 42);
	}

	@Test
	public void javaString() throws Exception {
		executeCode("java.lang.String('42')", "42");
	}

	@Test
	public void pythonList() throws Exception {
		executeCode("[1,2,3]", Arrays.asList(1, 2, 3));
	}

	@Test
	public void pythonSet() throws Exception {
		final Set<Object> javaSet = new HashSet<>();
		javaSet.add(new Integer(1));
		javaSet.add(new Integer(2));
		javaSet.add(new Integer(3));
		executeCode("{1,2,2,3}", javaSet);
	}

	@Test
	public void pythonDict() throws Exception {
		final Map<String, Object> javaMap = new HashMap<>();
		javaMap.put("a", "b");
		executeCode("{'a': 'b'}", javaMap);
	}

	@Test
	public void createJavaType() throws Exception {
		executeCode("java.io.File('/')", new java.io.File("/"));
	}

	@Test
	public void createEclipseClass() throws Exception {
		executeCode("org.eclipse.core.runtime.Path('/')", new org.eclipse.core.runtime.Path("/"));
	}

	@Test
	public void createPythonObject() throws Exception {
		final ScriptResult result = executeCode("object()", false);
		assertNotNull(result);
		assertNull(result.getException());
		assertNotNull(result.getResult());
		assertTrue(result.getResult() instanceof String);
		assertThat((String) result.getResult(), startsWith("<object object at "));
	}

	@Test
	public void implementJavaInterface() throws Exception {
		final StringBuilder sb = new StringBuilder();
		sb.append("class Foo:\n");
		sb.append("\tdef getSourceCode(self):\n");
		sb.append("\t\treturn None\n");
		sb.append("\tdef toString(self):\n");
		sb.append("\t\treturn java.lang.String(repr(self))\n");
		sb.append("\tclass Java:\n");
		sb.append("\t\timplements = ['org.eclipse.ease.IScriptable']\n");
		executeCode(sb.toString(), false);
		final ScriptResult result = executeCode("Foo()");
		assertNotNull(result);
		assertNull(result.getException());
		assertNotNull(result.getResult());
		assertTrue(result.getResult() instanceof IScriptable);
	}

	@Test
	public void testMultiple() throws Exception {
		javaInteger();
		javaString();
		createJavaType();
		createEclipseClass();
		javaInteger();
		javaString();
	}

	@Test
	public void javaStringArray() throws Exception {
		executeCode("stringArray(['Hello', 'World'])", "Hello, World");
	}

	@Test
	public void javaIntegerArray() throws Exception {
		executeCode("integerArray([1, 2])", 3);
	}

	@Test
	public void javaIntArray() throws Exception {
		executeCode("intArray([1, 2])", 3);
	}

	@Test
	public void javaDoubleArray() throws Exception {
		executeCode("doubleArray([1.0, 2.0])", 3.0);
	}

	@Test
	public void javaBooleanArray() throws Exception {
		executeCode("booleanArray([True, False])", false);
	}

	@Test
	public void javaByteArray() throws Exception {
		executeCode("byteArray(bytearray(b'\\x01\\x02'))", "0102");
	}

	@Test
	public void javaShortArray() throws Exception {
		executeCode("shortArray([1, 2])", 3);
	}

	@Test
	public void javaLongArray() throws Exception {
		final ScriptResult result = executeCode("longArray([1, 2])", false);
		assertNotNull(result);
		assertNull(result.getException());
		assertNotNull(result.getResult());

		// FIXME: Different behavior between Python 2 and 3
		assertEquals(new Long(3), Long.valueOf(result.getResult().toString()));
	}

	@Test
	public void javaFloatArray() throws Exception {
		executeCode("floatArray([1.0, 2.0])", 3.0);
	}

	@Test
	public void javaObjectArray() throws Exception {
		executeCode("javaObjectArray([org.eclipse.core.runtime.Path('..'), org.eclipse.core.runtime.Path('..')])", new Path("./../.."));
	}

	@Test
	public void varargs() throws Exception {
		executeCode("varargs([org.eclipse.core.runtime.Path('..'), org.eclipse.core.runtime.Path('..')])", new Path("./../.."));
	}
}
