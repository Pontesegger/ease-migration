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
package org.eclipse.ease.modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ICodeFactory.Parameter;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;

public final class ModuleHelper {

	@Deprecated
	private ModuleHelper() {
	}

	/**
	 * Returns a list of exported methods. Any public method marked by a @WrapToScript annotation is exported. If no annotations are found all public methods
	 * are returned.
	 *
	 * @param clazz
	 *            class to be evaluated
	 * @return list of methods
	 */
	public static List<Method> getMethods(final Class<?> clazz) {

		try {
			if ((clazz == null) || (clazz.getMethods().length == 0))
				return Collections.emptyList();

		} catch (final NoClassDefFoundError e) {
			Logger.warning(Activator.PLUGIN_ID, "The class " + clazz.getName() + " is broken", e);
			return Collections.emptyList();
		}

		final List<Method> methods = new ArrayList<>();
		final boolean wrapping = ModuleHelper.hasWrapToScript(clazz);
		for (final Method method : clazz.getMethods()) {
			if ((Modifier.isPublic(method.getModifiers()) && (!wrapping || method.isAnnotationPresent(WrapToScript.class)))) {

				// ignore methods from java.lang.Object
				if (Object.class.equals(method.getDeclaringClass()))
					continue;

				methods.add(method);
			}
		}

		return methods;
	}

	/**
	 * Returns a List of exported fields. Any public final field marked by a @WrapToScript annotation is exported. If no annotations are found all public final
	 * fields are returned.
	 *
	 * @param clazz
	 *            Class to be evaluated
	 * @return List of Fields
	 */
	public static List<Field> getFields(final Class<?> clazz) {

		if ((clazz == null) || (clazz.getDeclaredFields().length == 0))
			return Collections.emptyList();

		final List<Field> fields = new ArrayList<>();
		final boolean wrapping = ModuleHelper.hasWrapToScript(clazz);
		for (final Field field : clazz.getFields()) {
			if ((Modifier.isFinal(field.getModifiers()))
					&& (Modifier.isPublic(field.getModifiers()) && (!wrapping || field.isAnnotationPresent(WrapToScript.class))))
				fields.add(field);
		}

		return fields;
	}

	/**
	 * Returns if the Class to be evaluated contains {@link WrapToScript} annotations.
	 *
	 * @param clazz
	 *            class to be evaluated
	 * @return <code>true</code> when clazz contains {@link WrapToScript} annotations
	 */
	private static boolean hasWrapToScript(final Class<?> clazz) {

		for (final Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(WrapToScript.class))
				return true;
		}

		for (final Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(WrapToScript.class))
				return true;
		}

		return false;
	}

	/**
	 * Resolve a relative module name to its definition. When only the last part of a module name is provided (without path), this method tries to locate the
	 * module and returns its definition. If 2 modules with the same name are detected, a {@link RuntimeException} is thrown.
	 *
	 * @param identifier
	 *            module identifier
	 * @return module definition or <code>null</code> in case no matching definition is found
	 */
	public static ModuleDefinition resolveModuleName(final String identifier) {

		final Path searchPath = new Path(identifier);

		final IScriptService scriptService = ScriptService.getService();
		final Collection<ModuleDefinition> availableModules = scriptService.getAvailableModules();

		// check for valid, absolute path
		ModuleDefinition candidate = null;
		for (final ModuleDefinition definition : availableModules) {
			if (definition.getPath().equals(searchPath))
				return definition;

			if ((searchPath.segmentCount() == 1) && (!searchPath.isAbsolute())) {
				if (definition.getPath().lastSegment().equals(searchPath.lastSegment())) {
					if (candidate != null)
						// we already had one candidate, name is ambiguous
						throw new RuntimeException("Module identifier \"" + identifier + "\" is ambiguous. Use full path name to load.");

					candidate = definition;
				}
			}
		}

		return candidate;
	}

	/**
	 * Get all loaded modules for a given script engine.
	 *
	 * @param engine
	 *            engine to parse
	 * @return module definitions for all loaded modules
	 */
	public static Collection<ModuleDefinition> getLoadedModules(final IScriptEngine engine) {
		// FIXME should be recoded as we should better query the environment module directly

		final Collection<ModuleDefinition> modules = new HashSet<>();

		// statically access service as workbench is not available in headless mode
		final IScriptService scriptService = ScriptService.getService();

		for (final Entry<String, Object> entry : engine.getVariables().entrySet()) {
			if (entry.getKey().startsWith(IEnvironment.MODULE_PREFIX)) {
				final Class<? extends Object> moduleClass = entry.getValue().getClass();

				for (final ModuleDefinition definition : scriptService.getAvailableModules()) {
					if (definition.getModuleClass().equals(moduleClass)) {
						modules.add(definition);
						break;
					}
				}
			}
		}

		return modules;
	}

	public static List<Parameter> getParameters(final Method method) {
		final ArrayList<Parameter> parameters = new ArrayList<>();

		for (int index = 0; index < method.getParameters().length; index++) {
			final Parameter parameter = new Parameter();
			parameter.setClass(method.getParameters()[index].getType());

			parameter.setName(method.getParameters()[index].getName());

			final ScriptParameter annotation = getParameterAnnotation(method.getParameters()[index].getAnnotations());
			if (annotation != null) {
				parameter.setOptional(ScriptParameter.Helper.isOptional(annotation));
				parameter.setDefault(annotation.defaultValue());
			}
			parameters.add(parameter);
		}

		// post process parameters: find unique names for unnamed parameters
		for (final Parameter parameter : parameters) {
			if (parameter.getName().isEmpty())
				parameter.setName(findName(parameters));
		}

		return parameters;
	}

	private static ScriptParameter getParameterAnnotation(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof ScriptParameter)
				return (ScriptParameter) annotation;
		}

		return null;
	}

	/**
	 * Find a unique name that is not used yet.
	 *
	 * @param parameters
	 *            list of available parameters
	 * @return unique unused parameter name
	 */
	private static String findName(final List<Parameter> parameters) {
		String name;
		int index = 1;
		boolean found;
		do {
			found = true;
			name = "param" + index;

			for (final Parameter parameter : parameters) {
				if (name.equals(parameter.getName())) {
					index++;
					found = false;
					break;
				}
			}

		} while (!found);

		return name;
	}

	/**
	 * Check deprecation status of a method/field.
	 *
	 * @param element
	 *            method/field to check
	 * @return <code>true</code> when deprecated
	 */
	public static boolean isDeprecated(final AccessibleObject element) {
		if (element instanceof Method)
			return (element.getAnnotation(Deprecated.class) != null) || (((Method) element).getDeclaringClass().getAnnotation(Deprecated.class) != null);

		if (element instanceof Field)
			return (element.getAnnotation(Deprecated.class) != null) || (((Field) element).getDeclaringClass().getAnnotation(Deprecated.class) != null);

		return element.getAnnotation(Deprecated.class) != null;
	}
}
