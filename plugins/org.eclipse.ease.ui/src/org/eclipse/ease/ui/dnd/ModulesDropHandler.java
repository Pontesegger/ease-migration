/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.dnd;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;

public class ModulesDropHandler extends AbstractModuleDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		return ((element instanceof ModuleDefinition) || (element instanceof ModuleEntry));
	}

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		try {
			final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);

			if (element instanceof ModuleDefinition) {
				final Method loadModuleMethod = EnvironmentModule.class.getMethod("loadModule", String.class, boolean.class);
				final String call = codeFactory.createFunctionCall(loadModuleMethod, ((ModuleDefinition) element).getPath().toString(), false);
				scriptEngine.executeAsync(call);

			} else if (element instanceof ModuleEntry) {
				final ModuleDefinition declaringModule = ((ModuleEntry<?>) element).getModuleDefinition();
				loadModule(scriptEngine, declaringModule.getPath().toString(), false);

				if (((ModuleEntry<?>) element).getEntry() instanceof Method) {

					// FIXME we need to find reasonable default values for mandatory parameters
					final String call = codeFactory.createFunctionCall((Method) ((ModuleEntry<?>) element).getEntry());
					scriptEngine.executeAsync(call);

				} else if (((ModuleEntry<?>) element).getEntry() instanceof Field)
					scriptEngine.executeAsync(((Field) ((ModuleEntry<?>) element).getEntry()).getName());

			} else
				// fallback solution
				scriptEngine.executeAsync(element);

		} catch (final NoSuchMethodException | SecurityException e) {
			Logger.error(Activator.PLUGIN_ID, "loadModule() method not found in Environment module", e);
		}
	}
}
