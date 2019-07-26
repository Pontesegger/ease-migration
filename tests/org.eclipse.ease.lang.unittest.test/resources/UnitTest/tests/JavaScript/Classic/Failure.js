/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

loadModule('/Unittest');

startTest("three");
{
	// invalid assertion
	print("three");
	assertTrue(false);
	endTest();
}

startTest("four");
{
	// 2 invalid assertion
	print("four");
	assertTrue(false);
	assertFalse(true);
	endTest();
}
