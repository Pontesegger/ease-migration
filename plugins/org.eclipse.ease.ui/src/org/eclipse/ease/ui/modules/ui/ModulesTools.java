/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.modules.ui;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.ICodeFactory.Parameter;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.PlatformUI;

public final class ModulesTools {

	public static class ModuleEntry<T> {

		private final ModuleDefinition fModuleDefinition;
		private final T fEntry;

		public ModuleEntry(ModuleDefinition module, T entry) {
			fModuleDefinition = module;
			fEntry = entry;
		}

		public ModuleDefinition getModuleDefinition() {
			return fModuleDefinition;
		}

		public T getEntry() {
			return fEntry;
		}

		@Override
		public int hashCode() {
			return fModuleDefinition.hashCode() ^ fEntry.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ModuleEntry)
				return fModuleDefinition.equals(((ModuleEntry<?>) o).fModuleDefinition) && fEntry.equals(((ModuleEntry<?>) o).fEntry);

			return false;
		}
	}

	private static Styler OPTIONAL_PARAMETER_STYLE = new Styler() {
		private final Font italic = JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);

		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = italic;
			textStyle.foreground = JFaceResources.getColorRegistry().get("QUALIFIER_COLOR");
		}
	};

	@Deprecated
	private ModulesTools() {
	}

	/**
	 * Generates the signature of the method. Optional parameters are written in italic.
	 *
	 * @param method
	 *            inspected method
	 * @return signature of method.
	 */
	public static StyledString getSignature(final Method method, final boolean useStyledReturnValue) {

		final StyledString signature = new StyledString();
		signature.append(method.getName());

		signature.append('(');
		final List<Parameter> parameters = ModuleHelper.getParameters(method);
		for (final Parameter parameter : parameters) {
			if (parameter.isOptional()) {
				signature.append(parameter.getClazz().getSimpleName(), OPTIONAL_PARAMETER_STYLE);
				signature.append(" " + parameter.getName(), OPTIONAL_PARAMETER_STYLE);
			} else {
				signature.append(parameter.getClazz().getSimpleName());
				signature.append(" " + parameter.getName());
			}

			if (!parameter.equals(parameters.get(parameters.size() - 1)))
				signature.append(", ");
		}
		signature.append(')');

		signature.append(" : " + method.getReturnType().getSimpleName(), (useStyledReturnValue) ? StyledString.DECORATIONS_STYLER : null);

		return signature;
	}

	/**
	 * Get the module owning the method provided.
	 *
	 * @param method
	 *            inspected method
	 * @return module containing the method or null.
	 * @deprecated Do not use as this method might yield wrong results!!!
	 */
	@Deprecated
	public static ModuleDefinition getDeclaringModule(final Method method) {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<>(scriptService.getAvailableModules());

		// try for exact match
		for (final ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(method.getDeclaringClass()))
				return module;
		}

		// try for derived match
		for (final ModuleDefinition module : modules) {
			if (method.getDeclaringClass().isAssignableFrom(module.getModuleClass()))
				return module;
		}

		return null;
	}

	/**
	 * Get the module owning the field provided.
	 *
	 * @param field
	 *            inspected field
	 * @return module containing the field or null.
	 */
	public static ModuleDefinition getDeclaringModule(final Field field) {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<>(scriptService.getAvailableModules());

		// try for exact match
		for (final ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(field.getDeclaringClass()))
				return module;
		}

		// try for derived match
		for (final ModuleDefinition module : modules) {
			if (field.getDeclaringClass().isAssignableFrom(module.getModuleClass()))
				return module;
		}

		return null;
	}

	public static int getOptionalParameterCount(final Method method) {
		int optional = 0;

		for (final Annotation[] list : method.getParameterAnnotations()) {
			for (final Annotation annotation : list) {
				if ((annotation.annotationType().equals(ScriptParameter.class)) && (ScriptParameter.Helper.isOptional((ScriptParameter) annotation))) {
					optional++;
					break;
				}
			}
		}

		return optional;
	}
}
