/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.views.shell.dropins.modules;

import java.util.stream.Collectors;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.jface.viewers.IStructuredContentProvider;

public class ModulesStackContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IScriptEngine) {
			final IEnvironment environment = IEnvironment.getEnvironment((IScriptEngine) inputElement);
			if (environment != null)
				return environment.getModules().stream().map(m -> ModuleDefinition.forInstance(m)).filter(m -> m != null).collect(Collectors.toList())
						.toArray();
		}

		return new Object[0];
	}
}
