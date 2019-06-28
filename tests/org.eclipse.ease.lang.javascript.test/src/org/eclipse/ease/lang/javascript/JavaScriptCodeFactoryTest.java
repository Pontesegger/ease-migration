/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class JavaScriptCodeFactoryTest {
	private JavaScriptCodeFactory fFactory;

	@Before
	public void setup() {
		fFactory = new JavaScriptCodeFactory();
	}

	@Test
	public void testCommentCreator() {
		assertEquals("// Comment", fFactory.createCommentedString("Comment", false));
		assertEquals(String.format("// Multi%n// Line%n// Comment"), fFactory.createCommentedString("Multi\nLine\nComment", false));
		assertEquals(String.format("/**%n * Multi%n * Line%n * Comment%n */%n"), fFactory.createCommentedString("Multi\nLine\nComment", true));
	}
}
