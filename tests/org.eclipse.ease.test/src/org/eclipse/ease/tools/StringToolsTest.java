/*******************************************************************************
 * Copyright (c) 2017 Syamkumar and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Syamkumar - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.tools;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class StringToolsTest {

	@Test
	public void parseArgumentsNull() {
		assertArrayEquals(StringTools.parseArguments(null), new String[0]);
	}

	@Test
	public void parseArgumentsEmpty() {
		assertArrayEquals(StringTools.parseArguments(""), new String[0]);
	}

	@Test
	public void parseArgumentsNonQuotedSingle() {
		assertArrayEquals(StringTools.parseArguments("param"), new String[] { "param" });
	}

	@Test
	public void parseArgumentsNonQuotedMultiple() {
		assertArrayEquals(StringTools.parseArguments("param1 param2 param3 param4"), new String[] { "param1", "param2", "param3", "param4" });
	}

	@Test
	public void parseArgumentsQuotedSingle() {
		assertArrayEquals(StringTools.parseArguments("\"param\""), new String[] { "param" });
	}

	@Test
	public void parseArgumentsQuotedMultiple() {
		assertArrayEquals(StringTools.parseArguments("\"param1\" \"param2 still param2\" \"param3\" \"param4\""),
				new String[] { "param1", "param2 still param2", "param3", "param4" });
	}

	@Test
	public void parseArgumentsMixedMultiple() {
		assertArrayEquals(StringTools.parseArguments("\"param1\" param2 \"param3 still param3\" param4"),
				new String[] { "param1", "param2", "param3 still param3", "param4" });
	}

	@Test
	public void parseArgumentsMixedMultipleLines() {
		assertArrayEquals(StringTools.parseArguments("param1\r\nparam2\r\n\"param3 still param3\"\r\n\"param4\"\r\n"),
				new String[] { "param1", "param2", "param3 still param3", "param4" });
	}
}
