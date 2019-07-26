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

startTest("five");
{
	// error
	print("five");
	new java.lang.String("").substring(3);
	endTest();
}

print("Never to be reached");
