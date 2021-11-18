package org.eclipse.ease.lang.javascript.nashorn;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.ease.AbstractReplScriptEngine;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineException;
import org.eclipse.ease.ScriptObjectType;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseDebugVariable.Type;
import org.eclipse.ease.lang.javascript.JavaScriptCodeFactory;

public class NashornScriptEngine extends AbstractReplScriptEngine {

	public static final String ENGINE_ID = "org.eclipse.ease.javascript.nashorn";

	private ScriptEngine fEngine;

	public NashornScriptEngine() {
		super("Nashorn");
	}

	@Override
	public void terminateCurrent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return fEngine.get(name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		final Map<String, Object> variables = new HashMap<>();
		final Bindings bindings = fEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		for (final Entry<String, Object> entry : bindings.entrySet())
			variables.put(entry.getKey(), entry.getValue());

		return variables;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return fEngine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(name);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		if (!JavaScriptCodeFactory.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		fEngine.put(name, content);
	}

	@Override
	public void registerJar(final URL url) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupEngine() throws ScriptEngineException {
		final ScriptEngineManager engineManager = new ScriptEngineManager();
		fEngine = engineManager.getEngineByName("nashorn");

		if (fEngine == null) {
			throw new ScriptEngineException("Unable to load Nashorn Script Engine");
		}
	}

	@Override
	protected void teardownEngine() {
		fEngine = null;
	}

	@Override
	protected Object execute(final Script script, final String fileName, final boolean uiThread) throws Exception {
		return fEngine.eval(script.getCode());
	}

	@Override
	protected boolean acceptVariable(Object value) {
		if (isFunction(value))
			return false;

		return super.acceptVariable(value);
	}

	@Override
	public ScriptObjectType getType(Object object) {
		if (object != null) {
			if (isArray(object))
				return ScriptObjectType.NATIVE_ARRAY;

			if (isFunction(object))
				return ScriptObjectType.NATIVE_OBJECT;
		}

		return super.getType(object);
	}

	@Override
	protected String getTypeName(Object value) {
		final ScriptObjectType type = getType(value);
		switch (type) {
		case NATIVE_ARRAY:
			return "JavaScript Array";

		case NATIVE_OBJECT:
			return "JavaScript Object";

		default:
			return super.getTypeName(value);
		}
	}

	@Override
	protected Collection<EaseDebugVariable> getDefinedVariables(Object scope) {
		final Collection<EaseDebugVariable> result = new HashSet<>();

		if (isScriptObject(scope)) {
			final Map<String, Object> childElements = getChildElements(scope);

			if (isArray(scope)) {
				for (final Entry<String, Object> child : childElements.entrySet()) {
					final EaseDebugVariable variable = createVariable("[" + child.getKey() + "]", child.getValue());
					result.add(variable);
				}

			} else {
				for (final Entry<String, Object> child : childElements.entrySet()) {
					final EaseDebugVariable variable = createVariable(child.getKey(), child.getValue());
					result.add(variable);
				}

			}

			return result;

		} else
			return super.getDefinedVariables(scope);
	}

	@Override
	protected EaseDebugVariable createVariable(String name, Object value) {
		final EaseDebugVariable variable = super.createVariable(name, value);

		if (isArray(value)) {
			variable.getValue().setValueString("array[" + getChildElements(value).size() + "]");
			variable.setType(Type.NATIVE_ARRAY);

		} else if (isScriptObject(value)) {
			variable.getValue().setValueString("object{" + getChildElements(value).size() + "}");
			variable.setType(Type.NATIVE_OBJECT);
		}

		return variable;
	}

	@Override
	public String toString(Object object) {
		if (isArray(object)) {
			final Map<String, Object> childElements = getChildElements(object);

			// sort array keys
			final List<String> keys = new ArrayList<>(childElements.keySet());
			Collections.sort(keys, (o1, o2) -> {
				try {
					final int index1 = Integer.parseInt(o1);
					final int index2 = Integer.parseInt(o2);
					return index2 - index1;
				} catch (final NumberFormatException e) {
					return o1.compareTo(o2);
				}
			});

			final List<Object> elements = new ArrayList<>();
			for (final String index : keys)
				elements.add(childElements.get(index));

			return buildArrayString(elements);
		}

		if (isFunction(object)) {
			final Map<String, Object> childElements = getChildElements(object);
			return buildObjectString(childElements);
		}

		return super.toString(object);
	}

	private static boolean isScriptObject(Object value) {
		return (value != null) && ("jdk.nashorn.api.scripting.ScriptObjectMirror".equals(value.getClass().getName()));
	}

	/**
	 * Get child elements from a given script object. Child elements will be filtered using acceptVariable(). We are using reflection as we have class loading
	 * restrictions on the nashorn packages in eclipse.
	 *
	 * @param parent
	 *            script object to investigate
	 * @return map of <identifier, childElement>
	 */
	private Map<String, Object> getChildElements(Object parent) {
		final Map<String, Object> children = new HashMap<>();

		if (isScriptObject(parent)) {
			try {
				final Method keySetMethod = parent.getClass().getMethod("keySet");
				final Method getMethod = parent.getClass().getMethod("get", Object.class);
				if ((keySetMethod != null) && (getMethod != null)) {
					final Object keys = keySetMethod.invoke(parent, new Object[0]);
					if (keys instanceof Collection<?>) {
						for (final Object key : (Collection<?>) keys) {
							final Object childValue = getMethod.invoke(parent, key);
							if (acceptVariable(childValue))
								children.put(key.toString(), childValue);
						}
					}
				}

			} catch (final ReflectiveOperationException e) {
				// ignore
			} catch (final SecurityException e) {
				// ignore
			} catch (final IllegalArgumentException e) {
				// ignore
			}
		}

		return children;
	}

	private static boolean isFunction(Object value) {
		return invokeBooleanMethod(value, "isFunction");
	}

	private static boolean isArray(Object value) {
		return invokeBooleanMethod(value, "isArray");
	}

	/**
	 * Invoke a method without parameters and cast the return value to boolean. We are using reflection as we have class loading restrictions on the nashorn
	 * packages in eclipse.
	 *
	 * @param value
	 *            instance to invoke method on
	 * @param methodName
	 *            method name to invoke
	 * @return method invokation result
	 */
	private static boolean invokeBooleanMethod(Object value, String methodName) {
		if (isScriptObject(value)) {
			try {
				final Method method = value.getClass().getMethod(methodName);
				if (method != null) {
					final Object isFunction = method.invoke(value, new Object[0]);
					return Boolean.parseBoolean(isFunction.toString());
				}

			} catch (final ReflectiveOperationException e) {
				// ignore
			} catch (final SecurityException e) {
				// ignore
			} catch (final IllegalArgumentException e) {
				// ignore
			}
		}

		return false;
	}

	@Override
	protected void notifyExecutionListeners(Script script, int status) {
		if (!getTerminateOnIdle()) {
			// high probability to run in interactive shell mode

			if (IExecutionListener.SCRIPT_END == status) {
				try {
					setVariable("_", script.getResult().get());
				} catch (final ExecutionException e) {
					setVariable("_", e.getCause());
				}
			}
		}

		super.notifyExecutionListeners(script, status);
	}
}
