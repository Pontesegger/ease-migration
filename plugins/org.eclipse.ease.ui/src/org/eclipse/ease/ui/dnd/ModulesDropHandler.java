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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.dialogs.ParametersDialog;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Display;

public class ModulesDropHandler extends AbstractModuleDropHandler implements IShellDropHandler {

	public static final String NULL_INDICATOR = "<null>";

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		return ((element instanceof ModuleDefinition) || (element instanceof ModuleEntry));
	}

	@Override
	public void performDrop(IScriptEngine scriptEngine, Object element) {
		performDrop(scriptEngine, element, DND.DROP_MOVE);
	}

	@Override
	public void performDrop(IScriptEngine scriptEngine, Object element, int detail) {
		try {
			if (element instanceof ModuleDefinition) {
				final Method loadModuleMethod = EnvironmentModule.class.getMethod("loadModule", String.class, boolean.class);

				final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
				final String call = codeFactory.createFunctionCall(loadModuleMethod, ((ModuleDefinition) element).getPath().toString(), false);
				scriptEngine.executeAsync(call);

			} else if (element instanceof ModuleEntry) {
				final ModuleDefinition declaringModule = ((ModuleEntry<?>) element).getModuleDefinition();
				loadModule(scriptEngine, declaringModule.getPath().toString(), false);

				if (((ModuleEntry<?>) element).getEntry() instanceof Method) {
					final Method method = (Method) ((ModuleEntry<?>) element).getEntry();
					if ((detail != DND.DROP_MOVE) && (hasParameters(method)))
						executeCallWithParameters(scriptEngine, method);
					else
						executeDefaultCall(scriptEngine, method);

				} else if (((ModuleEntry<?>) element).getEntry() instanceof Field)
					scriptEngine.executeAsync(((Field) ((ModuleEntry<?>) element).getEntry()).getName());

			} else
				// fallback solution
				scriptEngine.executeAsync(element);

		} catch (final NoSuchMethodException | SecurityException e) {
			Logger.error(Activator.PLUGIN_ID, "loadModule() method not found in Environment module", e);
		}
	}

	private boolean hasParameters(Method method) {
		return method.getParameterCount() > 0;
	}

	private void executeCallWithParameters(IScriptEngine scriptEngine, Method method) {
		final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);

		final ParametersDialog dialog = new ParametersDialog(Display.getDefault().getActiveShell(), method, method.getParameters());
		if (dialog.open() == Window.OK) {
			final String call = codeFactory.createFunctionCall(method, createParameterInstances(method, dialog.getParameters()));
			scriptEngine.executeAsync(call);
		}
	}

	private Object[] createParameterInstances(Method method, List<String> parameters) {
		final ArrayList<Object> instances = new ArrayList<>();
		for (int index = 0; index < Math.min(method.getParameterCount(), parameters.size()); index++) {
			final Parameter parameter = method.getParameters()[index];
			instances.add(convert(parameters.get(index), parameter.getType()));
		}

		return instances.toArray();
	}

	private Object convert(String input, Class<?> targetClass) {
		if (NULL_INDICATOR.equals(input))
			return null;

		if ((String.class.equals(targetClass)) || (Object.class.equals(targetClass)))
			return input;

		if ((Integer.class.equals(targetClass)) || (int.class.equals(targetClass)))
			return Integer.valueOf(input);

		if ((Long.class.equals(targetClass)) || (long.class.equals(targetClass)))
			return Long.valueOf(input);

		if ((Double.class.equals(targetClass)) || (double.class.equals(targetClass)))
			return Double.valueOf(input);

		if ((Float.class.equals(targetClass)) || (float.class.equals(targetClass)))
			return Float.valueOf(input);

		if ((Boolean.class.equals(targetClass)) || (boolean.class.equals(targetClass)))
			return Boolean.valueOf(input);

		throw new IllegalArgumentException(String.format("Cannot convert '%s' to %s", input, targetClass.getCanonicalName()));
	}

	private void executeDefaultCall(IScriptEngine scriptEngine, Method entry) {
		final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);

		final String call = codeFactory.createFunctionCall(entry);
		scriptEngine.executeAsync(call);
	}
}
