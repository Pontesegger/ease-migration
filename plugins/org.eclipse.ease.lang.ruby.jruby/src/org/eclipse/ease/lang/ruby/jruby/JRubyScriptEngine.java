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
package org.eclipse.ease.lang.ruby.jruby;

import java.net.URL;
import java.util.Map;

import org.eclipse.ease.AbstractReplScriptEngine;
import org.eclipse.ease.Script;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class JRubyScriptEngine extends AbstractReplScriptEngine {

	private ScriptingContainer fEngine;

	public JRubyScriptEngine() {
		super("JRuby");
	}

	@Override
	public Object getExecutedFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void terminateCurrent() {
	}

	@Override
	protected void setupEngine() {
		fEngine = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);

		fEngine.setOutput(getOutputStream());
		fEngine.setError(getErrorStream());
		fEngine.setInput(getInputStream());
	}

	@Override
	protected void teardownEngine() {
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		return fEngine.runScriptlet(script.getCodeStream(), fileName);
	}

	@Override
	public void registerJar(final URL url) {
		throw new RuntimeException("Oeration not supported");
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return fEngine.get("$" + name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		return fEngine.getVarMap();
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return internalGetVariables().containsKey("$" + name);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		fEngine.put("$" + name, content);
	}
}
