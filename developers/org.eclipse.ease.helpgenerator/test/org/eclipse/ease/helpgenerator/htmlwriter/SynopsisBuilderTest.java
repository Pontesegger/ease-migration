/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.htmlwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.ease.helpgenerator.model.ExceptionValue;
import org.eclipse.ease.helpgenerator.model.Method;
import org.eclipse.ease.helpgenerator.model.Parameter;
import org.eclipse.ease.helpgenerator.model.ReturnValue;
import org.eclipse.ease.helpgenerator.model.ScriptExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SynopsisBuilderTest {

	private static final ReturnValue RETURN_VOID = new ReturnValue(ReturnValue.VOID, "");

	private static final Collection<String> NO_ALIASES = Collections.emptySet();
	private static final List<ExceptionValue> NO_EXCEPTIONS = Collections.emptyList();
	private static final List<ScriptExample> NO_EXAMPLES = Collections.emptyList();

	@Test
	@DisplayName("build() void method")
	public void buildVoidMethod() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID, Collections.emptyList(), NO_EXCEPTIONS,
				NO_EXAMPLES);

		assertEquals("void simple()", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with simple return value")
	public void buildSimpleReturnValue() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, new ReturnValue("int", ""), Collections.emptyList(),
				NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("{@link int} simple()", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with class return value")
	public void buildClassReturnValue() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, new ReturnValue("String", ""), Collections.emptyList(),
				NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("{@link String} simple()", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with 1 parameter")
	public void build1Parameter() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID,
				Arrays.asList(new Parameter("param1", "int", "", null)), NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("void simple({@link int} param1)", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with 3 parameter")
	public void build3Parameters() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID,
				Arrays.asList(new Parameter("param1", "int", "", null), new Parameter("param2", "String", "", null),
						new Parameter("param3", "java.io.File", "", null)),
				NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("void simple({@link int} param1, {@link String} param2, {@link java.io.File} param3)", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with optional parameters")
	public void buildOptionalParameters() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID,
				Arrays.asList(new Parameter("param1", "int", "", null), new Parameter("param2", "String", "", "Hello")), NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("void simple({@link int} param1, <i>[{@link String} param2]</i>)", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method throwing 1 exception")
	public void buildMethodThrowing1Exception() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID, Collections.emptyList(),
				Arrays.asList(new ExceptionValue("IOException", "")), NO_EXAMPLES);

		assertEquals("void simple() throws {@link IOException}", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with generic parameter")
	public void buildMethodWithGenericParameter() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID,
				Arrays.asList(new Parameter("param1", "Function<Long, String>", "", null)), NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("void simple({@link Function}&lt;{@link Long}, {@link String}&gt; param1)", SynopsisBuilder.getInstance().build(method));
	}

	@Test
	@DisplayName("build() method with generic ? parameter")
	public void buildMethodWithGenericQuestionParameter() {

		final Method method = new Method("simple", "comment", "deprecationMessage", NO_ALIASES, RETURN_VOID,
				Arrays.asList(new Parameter("param1", "Function<?>", "", null)), NO_EXCEPTIONS, NO_EXAMPLES);

		assertEquals("void simple({@link Function}&lt;?&gt; param1)", SynopsisBuilder.getInstance().build(method));
	}
}
