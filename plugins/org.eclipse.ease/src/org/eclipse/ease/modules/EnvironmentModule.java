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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.ModuleDefinition.ModuleDependency;
import org.eclipse.ease.modules.ModuleTracker.ModuleState;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * The Environment provides base functions for all script interpreters. It is automatically loaded by any interpreter upon startup.
 */
public class EnvironmentModule extends AbstractScriptModule implements IEnvironment {

	public static final String MODULE_NAME = "/System/Environment";

	public static final String EASE_CODE_PREFIX = "__EASE_";
	public static final String MODULE_PREFIX = EASE_CODE_PREFIX + "MOD_";

	private static final Pattern VALID_TOPICS_PATTERN = Pattern.compile("[\\w ]+(?:\\(\\))?");

	public static void bootstrap() {
		final ModuleDefinition definition = ModuleHelper.resolveModuleName(MODULE_NAME);
		final EnvironmentModule instance = (EnvironmentModule) definition.createModuleInstance();

		instance.initialize(AbstractScriptEngine.getCurrentScriptEngine(), instance);

		instance.wrap(instance, false);
	}

	private final ModuleTracker fModuleTracker = new ModuleTracker();

	private final ListenerList<IModuleListener> fModuleListeners = new ListenerList<>();

	/** Stores short method IDs to method relations used for script callbacks. */
	private final Map<String, Method> fRegisteredMethods = new HashMap<>();

	/** Callbacks for wrapped method invocations. */
	private final ListenerList<IModuleCallbackProvider> fModuleCallbacks = new ListenerList<>();

	public EnvironmentModule() {
		final ModuleDefinition moduleDefinition = ModuleHelper.resolveModuleName(MODULE_NAME);
		final ModuleState state = fModuleTracker.addModule(moduleDefinition);
		state.setInstance(this);
	}

	public Object getModuleInstance(ModuleDefinition definition) {
		final ModuleState moduleState = fModuleTracker.getOrCreateModuleState(definition);

		if (!moduleState.isLoaded()) {
			if (definition.isDeprecated())
				printError("Module \"" + definition.getName() + "\" is deprecated. Consider updating your code.");

			createModuleDependencies(definition);
			final Object instance = definition.createModuleInstance();
			moduleState.setInstance(instance);

			// check that module class got initialized correctly
			if (instance == null)
				throw new RuntimeException("Could not create module instance, see workspace log for more details");

			if (instance instanceof IScriptModule)
				((IScriptModule) instance).initialize(getScriptEngine(), this);
		}

		return moduleState.getInstance();
	}

	private void createModuleDependencies(ModuleDefinition parentModuleDefinition) {

		for (final ModuleDependency dependency : parentModuleDefinition.getDependencies()) {
			final ModuleDefinition dependencyDefinition = dependency.getDefinition();

			if (dependencyDefinition == null)
				throw new RuntimeException("Could not resolve module dependency \"" + dependency.getId() + "\"");

			createModuleDependencies(dependencyDefinition);

			getModuleInstance(dependencyDefinition);
		}
	}

	@Override
	@WrapToScript
	public final Object loadModule(final String moduleIdentifier, @ScriptParameter(defaultValue = "false") boolean useCustomNamespace) {
		// resolve identifier
		final ModuleDefinition definition = ModuleHelper.resolveModuleName(moduleIdentifier);
		if (definition == null)
			throw new RuntimeException("Could not find module \"" + moduleIdentifier + "\"");

		final ModuleState moduleState = fModuleTracker.getOrCreateModuleState(definition);

		// make sure module instance gets loaded
		getModuleInstance(definition);

		if (!useCustomNamespace)
			wrapModuleDependencies(definition);

		// create function wrappers
		return wrap(moduleState.getInstance(), useCustomNamespace);
	}

	/**
	 * Find and wrap instances of module dependencies. All found dependencies (and dependencies of dependencies) are wrapped into the root scope if they were
	 * not wrapped before.
	 *
	 * @param parentModuleDefinition
	 *            parent definition to find dependencies for
	 */
	private void wrapModuleDependencies(ModuleDefinition parentModuleDefinition) {
		for (final ModuleDependency dependency : parentModuleDefinition.getDependencies()) {
			final ModuleDefinition dependencyDefinition = dependency.getDefinition();

			wrapModuleDependencies(dependencyDefinition);

			final ModuleState dependencyState = fModuleTracker.getOrCreateModuleState(dependencyDefinition);
			if (!dependencyState.isWrapped())
				wrap(dependencyState.getInstance(), false);
		}
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
		final ModuleDefinition definition = ModuleHelper.resolveModuleName(name);
		if (definition == null)
			return null;

		final ModuleState moduleState = fModuleTracker.getModuleState(definition);
		return (moduleState != null) ? moduleState.getInstance() : null;
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
		for (final ModuleState state : fModuleTracker.getAvailableModules()) {
			if (state.getInstance() != null) {
				if (clazz.isAssignableFrom(state.getInstance().getClass()))
					return (T) state.getInstance();
			}
		}

		return null;
	}

	@Override
	public List<Object> getModules() {
		return fModuleTracker.getAvailableModules().stream().filter(state -> state.isLoaded()).map(state -> state.getInstance()).collect(Collectors.toList());
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
	@WrapToScript(supportedLanguages = "!Python")
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
		final Object result = createWrappers(toBeWrapped, identifier, useCustomNamespace);

		boolean isTrackedModule = false;
		for (final ModuleState state : fModuleTracker.getAvailableModules()) {
			if (toBeWrapped.equals(state.getInstance())) {
				state.setWrapped(true);
				isTrackedModule = true;
				break;
			}
		}

		if (!isTrackedModule) {
			final ModuleState state = fModuleTracker.addInstance(toBeWrapped);
			state.setWrapped(true);
		}

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
	private Object createWrappers(final Object instance, final String identifier, boolean useCustomNamespace) {
		final ICodeFactory codeFactory = getCodeFactory();
		if (null == codeFactory)
			return null;

		String wrapperCode;
		if (instance instanceof IEnvironment)
			wrapperCode = codeFactory.createWrapper((IEnvironment) instance, instance, identifier, useCustomNamespace, getScriptEngine());
		else
			wrapperCode = codeFactory.createWrapper(this, instance, identifier, useCustomNamespace, getScriptEngine());

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
	@WrapToScript(supportedLanguages = "!Python")
	public void help(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String topic) {

		// sanity check to avoid blue screens for invalid calls, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=479762
		if ((topic != null) && !VALID_TOPICS_PATTERN.matcher(topic).matches()) {
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

	@Override
	public void addModuleCallback(IModuleCallbackProvider callbackProvider) {
		fModuleCallbacks.add(callbackProvider);
	}

	// needed by dynamic script code
	public boolean hasMethodCallback(String methodToken) {
		final Method method = fRegisteredMethods.get(methodToken);

		for (final IModuleCallbackProvider callbackProvider : fModuleCallbacks) {
			if ((callbackProvider.hasPreExecutionCallback(method)) || (callbackProvider.hasPostExecutionCallback(method)))
				return true;
		}

		return false;
	}

	// needed by dynamic script code, do not change synopsis
	public void preMethodCallback(String methodToken, Object... parameters) {
		final Method method = fRegisteredMethods.get(methodToken);
		if (method != null) {
			for (final IModuleCallbackProvider callbackProvider : fModuleCallbacks) {
				if (callbackProvider.hasPreExecutionCallback(method)) {
					callbackProvider.preExecutionCallback(method, parameters);
				}
			}
		}
	}

	// needed by dynamic script code, do not change synopsis
	public void postMethodCallback(String methodToken, Object result) {
		final Method method = fRegisteredMethods.get(methodToken);
		if (method != null) {
			for (final IModuleCallbackProvider callbackProvider : fModuleCallbacks) {
				if (callbackProvider.hasPostExecutionCallback(method)) {
					callbackProvider.postExecutionCallback(method, result);
				}
			}
		}
	}

	public String registerMethod(Method method) {
		final String key = Integer.toString(method.toString().hashCode());
		fRegisteredMethods.put(key, method);

		return key;
	}
}
