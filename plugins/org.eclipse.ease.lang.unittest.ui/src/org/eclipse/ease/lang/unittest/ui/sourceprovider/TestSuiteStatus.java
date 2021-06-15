/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.unittest.ui.sourceprovider;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;

public class TestSuiteStatus extends PropertyTester {

	private static String PROPERTY_STATUS = "status";
	private static String PROPERTY_EXISTS = "exists";

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof ITestSuite) {
			if (PROPERTY_STATUS.equals(property)) {
				if (expectedValue != null)
					return ((ITestSuite) receiver).getStatus().toString().equalsIgnoreCase(expectedValue.toString());

			} else if (PROPERTY_EXISTS.equals(property))
				return true;
		}

		return false;
	}
}
