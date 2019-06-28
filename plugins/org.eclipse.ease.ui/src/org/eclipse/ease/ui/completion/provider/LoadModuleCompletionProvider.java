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
package org.eclipse.ease.ui.completion.provider;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class LoadModuleCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return (context.getType() == Type.STRING_LITERAL) && (context.getCaller().endsWith("loadModule"));
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {

		// create a path to search for
		final IPath filterPath = new Path(context.getFilter());
		final IPath searchPath;

		if (filterPath.segmentCount() > 1)
			searchPath = filterPath.makeAbsolute().removeLastSegments(1);

		else if (filterPath.hasTrailingSeparator())
			searchPath = filterPath.makeAbsolute();

		else
			searchPath = new Path("/");

		final Collection<String> pathProposals = new HashSet<>();

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final Collection<ModuleDefinition> availableModules = scriptService.getAvailableModules();

		for (final ModuleDefinition definition : availableModules) {
			final IPath modulePath = definition.getPath();
			if (searchPath.isPrefixOf(modulePath)) {
				// this is a valid candidate

				if ((searchPath.segmentCount() + 1) == modulePath.segmentCount()) {

					if (matchesFilterIgnoreCase(definition.getName())) {

						// add module proposal
						final StyledString displayString = new StyledString(modulePath.lastSegment());
						if (!definition.isVisible())
							displayString.append(" (hidden)", StyledString.DECORATIONS_STYLER);

						addProposal(displayString, definition.getName(),
								new DescriptorImageResolver(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/module.png")), 0, null);
					}

				} else {
					// add path proposal; collect them first to avoid duplicates
					pathProposals.add(modulePath.removeLastSegments(1).toString());
				}
			}
		}

		// add path proposals
		for (final String pathProposal : pathProposals) {
			if (matchesFilterIgnoreCase(pathProposal))
				addProposal(pathProposal, pathProposal + "/",
						new DescriptorImageResolver(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER)), 10, null);
		}
	}
}