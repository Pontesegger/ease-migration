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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ui.PlatformUI;

public class ResourceDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		if ((element instanceof IFile) || (element instanceof File) || (element instanceof URI))
			return scriptEngine.getDescription().getSupportedScriptTypes().contains(getScriptType(element));

		// try to convert special file types
		final IFile adaptedFile = Platform.getAdapterManager().getAdapter(element, IFile.class);
		if (adaptedFile != null)
			return scriptEngine.getDescription().getSupportedScriptTypes().contains(getScriptType(adaptedFile));

		return false;
	}

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		try {
			final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
			final Method includeMethod = EnvironmentModule.class.getMethod("include", String.class);

			if ((element instanceof IFile) || (element instanceof File)) {
				final String call = codeFactory.createFunctionCall(includeMethod, ResourceTools.toAbsoluteLocation(element, null));
				scriptEngine.executeAsync(call);

			} else if (element instanceof URI) {
				final String call = codeFactory.createFunctionCall(includeMethod, element.toString());
				scriptEngine.executeAsync(call);

			} else {
				final IFile adaptedFile = Platform.getAdapterManager().getAdapter(element, IFile.class);
				if (adaptedFile != null)
					performDrop(scriptEngine, adaptedFile);

				else
					// fallback solution
					scriptEngine.executeAsync(element);
			}

		} catch (final Exception e) {
			Logger.error(Activator.PLUGIN_ID, "include() method not found in Environment module", e);

			// fallback solution
			scriptEngine.executeAsync(element);
		}
	}

	private static ScriptType getScriptType(final Object element) {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		return scriptService.getScriptType(ResourceTools.toAbsoluteLocation(element, null));
	}
}
