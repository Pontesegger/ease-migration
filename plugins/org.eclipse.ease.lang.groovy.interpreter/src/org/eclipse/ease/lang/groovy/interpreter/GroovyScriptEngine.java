package org.eclipse.ease.lang.groovy.interpreter;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.ease.AbstractReplScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.classloader.EaseClassLoader;

import groovy.lang.GroovyShell;

public class GroovyScriptEngine extends AbstractReplScriptEngine {

	/** Classloader used by the groovy shell. */
	private EaseClassLoader fClassloader;

	/** Groovy shell instance. */
	private GroovyShell fEngine;

	public GroovyScriptEngine() {
		super("Groovy");
	}

	@Override
	public void terminateCurrent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupEngine() {
		fClassloader = new EaseClassLoader();
		// we need both classloaders: the one from the current plugin and the global EASE loader
		fEngine = new GroovyShell(new MultiClassLoader(fClassloader, GroovyScriptEngine.class.getClassLoader()));

		setOutputStream(getOutputStream());
		setErrorStream(getErrorStream());
	}

	@Override
	public void setOutputStream(final OutputStream outputStream) {
		super.setOutputStream(outputStream);

		if (fEngine != null)
			fEngine.setProperty("out", getOutputStream());
	}

	@Override
	public void setErrorStream(final OutputStream errorStream) {
		super.setErrorStream(errorStream);

		if (fEngine != null)
			fEngine.setProperty("err", getErrorStream());
	}

	@Override
	protected void teardownEngine() {
	}

	@Override
	protected Object execute(final Script script, final String fileName, final boolean uiThread) throws Exception {
		try (InputStreamReader reader = new InputStreamReader(script.getCodeStream(), StandardCharsets.UTF_8)) {
			if ((fileName == null) || (fileName.isEmpty()))
				return fEngine.evaluate(reader);

			else
				return fEngine.evaluate(reader, fileName);
		}
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return fEngine.getContext().getVariable(name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		return fEngine.getContext().getVariables();
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return fEngine.getContext().hasVariable(name);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		fEngine.getContext().setVariable(name, content);
	}

	@Override
	public void registerJar(final URL url) {
		fClassloader.registerURL(this, url);
	}
}
