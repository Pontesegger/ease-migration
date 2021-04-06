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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;

/**
 * Drop handler accepting jar files. Calls loadJar() on dropped files.
 */
public class JarDropHandler extends AbstractFileDropHandler {

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		try {
			final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
			final Method loadJarMethod = EnvironmentModule.class.getMethod("loadJar", Object.class);

			final String call = codeFactory.createFunctionCall(loadJarMethod, getFileURI(element));
			scriptEngine.execute(call);
		} catch (final NoSuchMethodException e) {
			Logger.error(Activator.PLUGIN_ID, "Method loadJar() not found", e);
		} catch (final SecurityException e) {
			Logger.error(Activator.PLUGIN_ID, "Method loadJar() not accessible", e);
		}
	}

	@Override
	protected Collection<String> getAcceptedFileExtensions() {
		return Arrays.asList("jar");
	}
}