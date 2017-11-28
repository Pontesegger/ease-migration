/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * The Environment provides base functions for all script interpreters. It is automatically loaded by any interpreter upon startup.
 */
public class EnvironmentModule extends AbstractScriptModule implements IEnvironment {

	public static final String MODULE_NAME = "/System/Environment";

	public static final String MODULE_PREFIX = "__MOD_";

	private static final Pattern VALID_TOPICS_PATTERN = Pattern.compile("[\\w ]+(?:\\(\\))?");

	/** Stores ordering of wrapped elements. */
	private final List<Object> fModules = new ArrayList<>();

	/** Stores beautified names of loaded modules. */
	private final Map<String, Object> fModuleNames = new HashMap<>();

	private final ListenerList fModuleListeners = new ListenerList();

	@Override
	@WrapToScript
	public final Object loadModule(final String identifier, @ScriptParameter(defaultValue = "false") boolean useCustomNamespace) {
		// resolve identifier
		final String moduleName = ModuleHelper.resolveName(identifier);

		Object module = getModule(moduleName);
		if (module == null) {
			// not loaded yet
			final IScriptService scriptService = ScriptService.getService();
			final Map<String, ModuleDefinition> availableModules = scriptService.getAvailableModules();

			final ModuleDefinition definition = availableModules.get(moduleName);
			if (definition != null) {
				// module exists

				// load dependencies; always load to bring dependencies on top of modules stack
				for (final Entry<String, Boolean> entry : definition.getDependencies().entrySet()) {
					final ModuleDefinition requiredModule = scriptService.getModuleDefinition(entry.getKey());

					if (requiredModule == null)
						throw new RuntimeException("Could not resolve module dependency \"" + entry + "\"");

					if ((!fModuleNames.containsKey(requiredModule.getPath().toString())) || (entry.getValue())) {
						// only load if module was never loaded or reload is set to true
						try {
							loadModule(requiredModule.getPath().toString(), useCustomNamespace);

						} catch (final RuntimeException e) {
							throw new RuntimeException("Could not load module dependency \"" + requiredModule.getPath().toString() + "\"", e);
						}
					}
				}

				// print deprecation warning
				if (definition.isDeprecated())
					printError("Module \"" + moduleName + "\" is deprecated. Consider updating your code.");

				module = definition.createModuleInstance();

				// check that module class got initialized correctly
				if (module == null)
					throw new RuntimeException("Could not create module instance, see workspace log for more details");

				if (module instanceof IScriptModule)
					((IScriptModule) module).initialize(getScriptEngine(), this);

				fModuleNames.put(moduleName, module);

				// we need to track this module already as we need it in case we want to manipulate functions of already loaded modules
				fModules.add(module);

				// scripts changing functions force reloading of the whole module stack
				if (module instanceof IScriptFunctionModifier) {
					final List<Object> reverseList = new ArrayList<>(fModules);
					Collections.reverse(reverseList);

					for (final Object loadedModule : reverseList)
						wrap(loadedModule, useCustomNamespace);
				}
			} else
				throw new RuntimeException("Could not find module \"" + identifier + "\"");
		}

		// first take care that module is tracked as it might modify itself implementing IScriptFunctionModifier
		// move module up to first position
		fModules.remove(module);
		fModules.add(0, module);

		// create function wrappers
		return wrap(module, useCustomNamespace);
	}

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);

		fModules.add(this);
		fModuleNames.put(EnvironmentModule.MODULE_NAME, this);
	}

	/**
	 * List all available (visible) modules. Returns a list of visible modules. Loaded modules are indicated.
	 *
	 * @return string containing module information
	 */
	@WrapToScript
	public final String listModules() {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<>(scriptService.getAvailableModules().values());

		modules.sort((m1, m2) -> {
			return m1.getPath().toString().compareTo(m2.getPath().toString());
		});

		final StringBuilder output = new StringBuilder();

		// add header
		output.append("available modules\n=================\n\n");

		// add modules
		for (final ModuleDefinition module : modules) {

			if (module.isVisible()) {
				output.append('\t');

				output.append(module.getPath().toString());
				if (getModule(module.getPath().toString()) != null)
					output.append(" [LOADED]");

				output.append('\n');
			}
		}

		// write to default output
		print(output, true);

		return output.toString();
	}

	/**
	 * Resolves a loaded module and returns the Java instance. Will only query previously loaded modules.
	 *
	 * @param name
	 *            name of the module to resolve
	 * @return resolved module instance or <code>null</code>
	 */
	@Override
	@WrapToScript
	public final Object getModule(final String name) {
		return fModuleNames.get(name);
	}

	/**
	 * Resolves a loaded module by its class.
	 *
	 * @param clazz
	 *            module class to look resolve
	 * @return resolved module instance or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Object, U extends Class<T>> T getModule(final U clazz) {
		for (final Object module : getModules()) {
			if (clazz.isAssignableFrom(module.getClass()))
				return (T) module;
		}

		return null;
	}

	@Override
	public List<Object> getModules() {
		return Collections.unmodifiableList(fModules);
	}

	/**
	 * Write a message to the output stream of the script engine.
	 *
	 * @param text
	 *            message to write
	 * @param lineFeed
	 *            <code>true</code> to add a line feed after the text
	 */
	@Override
	@WrapToScript
	public final void print(final @ScriptParameter(defaultValue = "") Object text, final @ScriptParameter(defaultValue = "true") boolean lineFeed) {
		if (lineFeed)
			getScriptEngine().getOutputStream().println(text);
		else
			getScriptEngine().getOutputStream().print(text);
	}

	/**
	 * Write a message to the error stream of the script engine.
	 *
	 * @param text
	 *            message to write
	 */
	@WrapToScript
	public final void printError(final @ScriptParameter(defaultValue = "") Object text) {
		getScriptEngine().getErrorStream().println(text);
	}

	@Override
	public void addModuleListener(final IModuleListener listener) {
		fModuleListeners.add(listener);
	}

	@Override
	public void removeModuleListener(final IModuleListener listener) {
		fModuleListeners.remove(listener);
	}

	/**
	 * Read a single line of data from the default input stream of the script engine. Depending on the <i>blocking</i> parameter this method will wait for user
	 * input or return immediately with available data.
	 *
	 * @param blocking
	 *            <code>true</code> results in a blocking call until data is available, <code>false</code> returns in any case
	 * @return string data from input stream or <code>null</code>
	 * @throws IOException
	 *             when reading on the input stream fails
	 */
	@WrapToScript
	public String readInput(@ScriptParameter(defaultValue = "true") final boolean blocking) throws IOException {
		final InputStream inputStream = getScriptEngine().getInputStream();
		boolean doRead = blocking;
		if (!doRead) {
			try {
				doRead = (inputStream.available() > 0);
			} catch (final IOException e) {
				// no data to read available
			}
		}

		if (doRead)
			// read a single line
			return new BufferedReader(new InputStreamReader(inputStream)).readLine();

		return null;
	}

	protected void fireModuleEvent(final Object module, final int type) {
		for (final Object listener : fModuleListeners.getListeners())
			((IModuleListener) listener).notifyModule(module, type);
	}

	@Override
	@WrapToScript
	public Object wrap(final Object toBeWrapped, @ScriptParameter(defaultValue = "false") boolean useCustomNamespace) {
		// register new variable in script engine

		final String identifier = getCodeFactory().getSaveVariableName(getWrappedVariableName(toBeWrapped));

		final boolean reloaded = getScriptEngine().hasVariable(identifier);
		getScriptEngine().setVariable(identifier, toBeWrapped);

		Logger.trace(Activator.PLUGIN_ID, ICodeFactory.TRACE_MODULE_WRAPPER, "wrapping object: " + toBeWrapped.toString());

		// create function wrappers
		final Object result = createWrappers(toBeWrapped, identifier, reloaded, useCustomNamespace);

		// notify listeners
		fireModuleEvent(toBeWrapped, reloaded ? IModuleListener.RELOADED : IModuleListener.LOADED);

		return result;
	}

	public static final String getWrappedVariableName(final Object toBeWrapped) {
		return (MODULE_PREFIX + toBeWrapped.getClass().getName()).replace('.', '_');
	}

	/**
	 * Create script wrapper functions for the given instance.
	 *
	 * @param instance
	 *            module instance to create wrappers for
	 * @param identifier
	 *            identifier to be used to store object in script engine scope
	 * @param reload
	 *            flag indicating that the module was already loaded
	 * @param useCustomNamespace
	 *            set to <code>true</code> if functions and constants should not be stored to the global namespace but to the return value only
	 * @return java instance or wrapper object
	 */
	private Object createWrappers(final Object instance, final String identifier, final boolean reload, boolean useCustomNamespace) {
		final ICodeFactory codeFactory = getCodeFactory();
		if (null == codeFactory)
			return null;

		final String wrapperCode = codeFactory.createWrapper(this, instance, identifier, useCustomNamespace, getScriptEngine());

		if (useCustomNamespace)
			return getScriptEngine().inject(new Script("Wrapper(" + instance.getClass().getSimpleName() + ")", wrapperCode));
		else
			getScriptEngine().inject(new Script("Wrapper(" + instance.getClass().getSimpleName() + ")", wrapperCode));

		return instance;
	}

	/**
	 * Execute script code. This method executes script code directly in the running interpreter. Execution is done in the same thread as the caller thread.
	 *
	 * @param data
	 *            code to be interpreted
	 * @return result of code execution
	 */
	@WrapToScript
	public final Object execute(final Object data) {
		return getScriptEngine().inject(data);
	}

	/**
	 * Terminates script execution immediately. Code following this command will not be executed anymore.
	 *
	 * @param value
	 *            return code
	 */
	@WrapToScript
	public final void exit(final @ScriptParameter(defaultValue = ScriptParameter.NULL) Object value) {
		throw new ExitException(value);
	}

	/**
	 * Include and execute a script file. Quite similar to eval(Object) a source file is opened and its content is executed. Multiple sources are available:
	 * "workspace://" opens a file relative to the workspace root, "project://" opens a file relative to the current project, "file://" opens a file from the
	 * file system. All other types of URIs are supported too (like http:// ...). You may also use absolute and relative paths as defined by your local file
	 * system.
	 *
	 * @param filename
	 *            name of file to be included
	 * @return result of include operation
	 * @throws Throwable
	 */
	@WrapToScript
	public final Object include(final String filename) {
		final Object file = ResourceTools.resolve(filename, getScriptEngine().getExecutedFile());
		if (file != null)
			return getScriptEngine().inject(file);

		throw new RuntimeException("Cannot locate '" + filename + "'");
	}

	/**
	 * Get the current script engine instance.
	 *
	 * @return {@link IScriptEngine} instance
	 */
	@WrapToScript
	@Override
	public IScriptEngine getScriptEngine() {

		final IScriptEngine engine = super.getScriptEngine();
		if (engine == null) {
			final Job currentJob = Job.getJobManager().currentJob();
			if (currentJob instanceof IScriptEngine)
				return (IScriptEngine) currentJob;
		}

		return engine;
	}

	/**
	 * Get the generic script code factory registered for this script engine.
	 *
	 * @return code factory
	 */
	private ICodeFactory getCodeFactory() {
		return ScriptService.getCodeFactory(getScriptEngine());
	}

	/**
	 * Add a jar file to the classpath. Contents of the jar can be accessed right after loading. <i>location</i> can be an URL, a path, a File or an IFile
	 * instance. Adding a jar location does not necessary mean that its classes can be accessed. If the source is not accessible, then its classes are not
	 * available for scripting, too.
	 *
	 * @param location
	 *            {@link URL}, {@link Path}, {@link File} or {@link IFile}
	 * @return <code>true</code> when input could be converted to a URL
	 * @throws MalformedURLException
	 *             invalid URL detected
	 */
	@WrapToScript
	public boolean loadJar(Object location) throws MalformedURLException {
		if (!(location instanceof URL)) {
			// try to resolve workspace URIs
			Object file = ResourceTools.resolve(location, getScriptEngine().getExecutedFile());

			if (file instanceof IFile)
				file = ((IFile) file).getLocation().toFile();

			if (file instanceof File)
				location = ((File) file).toURI().toURL();

			else
				location = new URL(location.toString());
		}

		if (location instanceof URL) {
			getScriptEngine().registerJar((URL) location);
			return true;
		}

		return false;
	}

	/**
	 * Open help page on addressed topic. If the given topic matches a method or field from a loaded module, the definition will be opened. If the topic is
	 * unknown, a search in the whole eclipse help will be launched.
	 *
	 * @param topic
	 *            help topic to open (typically a function name)
	 */
	@WrapToScript
	public void help(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String topic) {

		// sanity check to avoid blue screens for invalid calls, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=479762
		if (!VALID_TOPICS_PATTERN.matcher(topic).matches()) {
			printError("Invalid help topic to look for: \"" + topic + "\"");
			return;
		}

		if (PlatformUI.isWorkbenchRunning()) {
			if (topic != null) {
				for (final Object module : getModules()) {
					final ModuleDefinition definition = ModuleDefinition.getDefinition(module);
					if (definition != null) {
						// look for matching method
						for (final Method method : definition.getMethods()) {
							if (matchesMethod(method, topic)) {
								// method found, display help

								final String link = definition.getHelpLocation(method.getName());
								Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(link));

								// done
								return;
							}
						}

						for (final Field field : definition.getFields()) {
							if (matchesField(field, topic)) {
								// field found, display help

								final String link = definition.getHelpLocation(field.getName());
								Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(link));

								// done
								return;
							}
						}
					}
				}

				// nothing found, start a search in help
				Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().getHelpSystem().search(topic));
			} else {
				// no topic provided, show main help page
				Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().getHelpSystem().displayHelp());
			}
		}
	}

	/**
	 * Verify that a given name matches the field name or one of its aliases.
	 *
	 * @param field
	 *            field to query
	 * @param name
	 *            name to match
	 * @return <code>true</code> on match
	 */
	private boolean matchesField(final Field field, final String name) {
		if (name.equalsIgnoreCase(field.getName()))
			return true;

		final WrapToScript wrapAnnotation = field.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String alias : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (name.equalsIgnoreCase(alias.trim()))
					return true;
		}

		return false;
	}

	/**
	 * Verify that a given name matches the method name or one of its aliases.
	 *
	 * @param method
	 *            method to query
	 * @param name
	 *            name to match
	 * @return <code>true</code> on match
	 */
	private boolean matchesMethod(final Method method, final String name) {
		if (name.equalsIgnoreCase(method.getName()))
			return true;

		final WrapToScript wrapAnnotation = method.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String alias : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (name.equalsIgnoreCase(alias.trim()))
					return true;
		}

		return false;
	}
}
