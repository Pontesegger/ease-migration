/*******************************************************************************
 * Copyright (c) 2016 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers;

import org.eclipse.ease.modules.WrapToScript;

/**
 * Module only used for unit testing.
 */
public class SampleModule {

	/** PI constant. */
	@WrapToScript
	public static final double PI = 3.1415926;

	/**
	 * Provide sum of 2 variables.
	 *
	 * @param a
	 *            param1
	 * @param b
	 *            param2
	 * @return result
	 */
	@WrapToScript
	public double sum(double a, double b) {
		return a + b;
	}

	/**
	 * Subtract b from a.
	 *
	 * @param a
	 *            param1
	 * @param b
	 *            param2
	 * @return result
	 */
	@WrapToScript
	public double sub(double a, double b) {
		return a - b;
	}

	/**
	 * Multiply 2 values.
	 *
	 * @param a
	 *            param1
	 * @param b
	 *            param2
	 * @return result
	 */
	@WrapToScript
	public double mul(double a, double b) {
		return a * b;
	}

	/**
	 * Divide a by b.
	 *
	 * @param a
	 *            param1
	 * @param b
	 *            param2
	 * @return result
	 */
	@WrapToScript
	public double div(double a, double b) {
		return a / b;
	}
}
