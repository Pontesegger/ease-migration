/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ease.IScriptEngineProvider;

public class ScriptEnginePropertyTester extends PropertyTester {

	private static final String PROPERTY_ENGINE_ID = "engineID";

	public ScriptEnginePropertyTester() {
	}

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof IScriptEngineProvider) {
			if (PROPERTY_ENGINE_ID.equals(property))
				return ((IScriptEngineProvider) receiver).getScriptEngine().getDescription().getID().equals(expectedValue);
		}

		return false;
	}
}
