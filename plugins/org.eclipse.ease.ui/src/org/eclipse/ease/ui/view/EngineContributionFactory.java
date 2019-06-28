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
package org.eclipse.ease.ui.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.handler.SwitchEngine;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public final class EngineContributionFactory extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator fServiceLocator;

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		fServiceLocator = serviceLocator;
	}

	@Override
	protected IContributionItem[] getContributionItems() {

		final List<IContributionItem> contributions = new ArrayList<>();

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		if (scriptService != null) {
			final Collection<EngineDescription> engines = scriptService.getEngines();

			final List<CommandContributionItemParameter> items = new ArrayList<>();
			for (final EngineDescription description : engines) {

				if (description.isReplShell()) {

					// set parameter for command
					final HashMap<String, String> parameters = new HashMap<>();
					parameters.put(SwitchEngine.PARAMETER_ID, description.getID());

					final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(fServiceLocator, null,
							SwitchEngine.COMMAND_ID, CommandContributionItem.STYLE_PUSH);
					contributionParameter.parameters = parameters;
					contributionParameter.label = description.getName();
					contributionParameter.visibleEnabled = true;
					contributionParameter.icon = Activator.getImageDescriptor("/icons/eobj16/engine.png");

					items.add(contributionParameter);
				}
			}

			// sort contributions
			Collections.sort(items, (o1, o2) -> o1.label.compareTo(o2.label));

			for (final CommandContributionItemParameter item : items)
				contributions.add(new CommandContributionItem(item));
		}

		return contributions.toArray(new IContributionItem[contributions.size()]);
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
