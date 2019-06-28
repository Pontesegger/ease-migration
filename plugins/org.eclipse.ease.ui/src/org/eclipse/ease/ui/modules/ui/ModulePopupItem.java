/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.modules.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.handler.LoadModule;
import org.eclipse.ease.ui.tools.AbstractPopupItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class ModulePopupItem extends AbstractPopupItem {

	private final ModuleDefinition fDefinition;

	public ModulePopupItem(final ModuleDefinition definition) {
		fDefinition = definition;
	}

	@Override
	protected CommandContributionItemParameter getContributionParameter() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(LoadModule.PARAMETER_NAME, fDefinition.getPath().toString());

		return new CommandContributionItemParameter(null, null, LoadModule.COMMAND_ID, parameters, null, null, null, null, null, null,
				CommandContributionItem.STYLE_PUSH, null, true);
	}

	@Override
	public String getDisplayName() {
		return fDefinition.getName();
	}

	@Override
	protected ImageDescriptor getImageDescriptor() {
		ImageDescriptor descriptor = fDefinition.getImageDescriptor();
		if (descriptor != null)
			return descriptor;

		return Activator.getImageDescriptor("/icons/eobj16/module.png");
	}
}
