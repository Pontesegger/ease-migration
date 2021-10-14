/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.completion.provider;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.ui.ISharedImages;

public class LoadModuleCompletionProvider extends AbstractPathCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && isMethodParameter(context, EnvironmentModule.LOAD_MODULE_METHOD, 0) && isStringParameter(context);
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {

		final IScriptService scriptService = ScriptService.getInstance();
		final Collection<ModuleDefinition> availableModules = scriptService.getAvailableModules();

		final Collection<IPath> paths = filter(getPathsFromElements(availableModules), context);

		paths.stream().forEach(p -> {
			addProposal(p.toString(), p.toString() + "/", new WorkbenchDescriptorImageResolver(ISharedImages.IMG_OBJ_FOLDER), 100, null);
		});

		final Collection<ModuleDefinition> modules = filter(availableModules, context);
		modules.stream().forEach(m -> {
			final StyledString displayString = new StyledString(m.getPath().lastSegment());
			if (!m.isVisible())
				displayString.append(" (hidden)", StyledString.DECORATIONS_STYLER);

			addProposal(displayString, m.getPath().toString(),
					new DescriptorImageResolver(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/module.png")), 90, null);
		});
	}

	@Override
	protected IPath toPath(Object element) {
		if (element instanceof ModuleDefinition)
			return ((ModuleDefinition) element).getPath();

		throw new IllegalArgumentException("element is not of type ModuleDefinition");
	}
}