/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.modules.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.ease.ui.tools.AbstractVirtualTreeProvider;
import org.eclipse.ui.PlatformUI;

public class ModulesContentProvider extends AbstractVirtualTreeProvider {

	private final boolean fModulesOnly;

	public ModulesContentProvider(final boolean modulesOnly) {
		fModulesOnly = modulesOnly;
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {

		if ((parentElement instanceof ModuleDefinition) && !fModulesOnly) {
			final List<Object> children = new ArrayList<>();

			for (final Field field : ModuleHelper.getFields(((ModuleDefinition) parentElement).getModuleClass()))
				children.add(new ModuleEntry<>((ModuleDefinition) parentElement, field));

			for (final Method method : ModuleHelper.getMethods(((ModuleDefinition) parentElement).getModuleClass()))
				children.add(new ModuleEntry<>((ModuleDefinition) parentElement, method));

			return children.toArray();
		}

		return super.getChildren(parentElement);
	};

	@Override
	protected void populateElements(final Object inputElement) {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<>(scriptService.getAvailableModules());

		for (final ModuleDefinition module : modules)
			registerElement(module.getPath().removeLastSegments(1), module);
	}

	@Override
	public boolean hasChildren(final Object element) {

		if ((element instanceof ModuleDefinition) && !fModulesOnly) {
			final Class<?> clazz = ((ModuleDefinition) element).getModuleClass();
			if (clazz == null)
				return false;

			return !ModuleHelper.getMethods(clazz).isEmpty() || !ModuleHelper.getFields(clazz).isEmpty();
		}

		return super.hasChildren(element);
	}
}
