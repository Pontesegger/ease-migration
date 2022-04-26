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

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

public class ModulesStackLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(final Object element) {
		final ModuleDefinition moduleDefinition = ModuleDefinition.forInstance(element);
		if (moduleDefinition != null)
			return moduleDefinition.getName();

		return element.getClass().getCanonicalName();
	}

	@Override
	public Image getImage(final Object element) {
		final ModuleDefinition moduleDefinition = ModuleDefinition.forInstance(element);
		if (moduleDefinition != null) {
			final ImageDescriptor icon = moduleDefinition.getImageDescriptor();
			if (icon != null)
				return icon.createImage();

			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/module.png", true);
		}

		return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_java_class.png", true);
	}
}
