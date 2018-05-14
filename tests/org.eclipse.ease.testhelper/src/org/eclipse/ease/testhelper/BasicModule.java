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

package org.eclipse.ease.testhelper;

import org.eclipse.ease.modules.WrapToScript;

/**
 * Simple math methods for unittests.
 */
public class BasicModule {

	/**
	 * Add two integers.
	 *
	 * @param a
	 *            first parameter
	 * @param b
	 *            second parameter
	 * @return sum of a and b
	 */
	@WrapToScript
	public int add(int a, int b) {
		return a + b;
	}

	/**
	 * Multiply two integers.
	 *
	 * @param a
	 *            first parameter
	 * @param b
	 *            second parameter
	 * @return product of a and b
	 */
	@WrapToScript
	public int mul(int a, int b) {
		return a * b;
	}
}
