/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors: Christian Pontesegger - initial API and implementation
 ******************************************************************************/

loadModule("/Unittest");

var modernTest = {

	// special marker to indicate this instance as unit test
	__unittest : '',

	// unit test description
	__description : 'Test class demonstrating modern testing',

	/**
	 * Simple test case. The
	 * 
	 * @test marker indicates it as a test.
	 */
	validNoAssertion : function() {
		'@test';
		'@description(Working testcase)';

		print("validNoAssertion");
	},

	validAssertion : function() {
		'@test';
		'@description(Working testcase)';

		assertTrue(true);
		print("validAssertion");
	}
}
