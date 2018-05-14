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

import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.WrapToScript;

/**
 * Advanced test module. Depends on basic test module.
 */
public class AdvancedModule extends AbstractScriptModule {

	/**
	 * Calculate the area of a rectangle.
	 *
	 * @param a
	 *            width
	 * @param b
	 *            height
	 * @return area
	 */
	@WrapToScript
	public int area(int a, int b) {
		final BasicModule module = getEnvironment().getModule(BasicModule.class);
		return module.mul(a, b);
	}
}
