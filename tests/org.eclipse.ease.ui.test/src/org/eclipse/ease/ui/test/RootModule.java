/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.test;

import org.eclipse.ease.modules.WrapToScript;

public class RootModule {

	@WrapToScript
	public static final String TEST_CONSTANT = "";

	@WrapToScript
	public void testMethod() {
		// nothing to do
	}
}