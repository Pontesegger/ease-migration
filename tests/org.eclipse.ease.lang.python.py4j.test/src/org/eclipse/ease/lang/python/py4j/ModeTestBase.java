/*******************************************************************************
 * Copyright (c) 2017 Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ease.IScriptable;
import org.eclipse.ease.ScriptResult;
import org.junit.Test;

/**
 * @author Kloesch
 *
 */
public abstract class ModeTestBase extends Py4JEngineTestBase {
	abstract protected ScriptResult executeCode(String code) throws Exception;

	abstract protected void executeCode(String code, Object result) throws Exception;

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
}
