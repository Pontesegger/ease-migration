/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.junit.Test;
import org.mockito.Mockito;

public class AbstractCodeFactoryTest {

	@Test
	public void createNewKeywordHeader() {
		final AbstractCodeFactory factory = mock(AbstractCodeFactory.class, Mockito.CALLS_REAL_METHODS);

		final HashMap<String, String> keywords = new HashMap<>();
		keywords.put("first", "value");
		keywords.put("menu", "this is a menu entry");

		final String header = factory.createKeywordHeader(keywords, null);

		assertTrue(header.contains("first           : value"));
		assertTrue(header.contains("menu            : this is a menu entry"));
	}
}
