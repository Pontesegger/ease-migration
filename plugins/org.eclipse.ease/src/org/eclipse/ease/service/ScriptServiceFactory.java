/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.service;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Service factory for {@link IScriptService}.
 */
public class ScriptServiceFactory extends AbstractServiceFactory {

	public ScriptServiceFactory() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator, final IServiceLocator locator) {
		if (serviceInterface.equals(IScriptService.class))
			return ScriptService.getInstance();

		return null;
	}
}
