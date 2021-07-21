/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;

public class BasicContext implements ICompletionContext {

	private final String fContent;
	private final int fCursorPosition;
	private final IScriptEngine fScriptEngine;
	private final ScriptType fScriptType;
	private final Object fResource;

	public BasicContext(IScriptEngine scriptEngine, String content, int cursorPosition) {
		fScriptEngine = scriptEngine;
		fScriptType = scriptEngine.getDescription().getSupportedScriptTypes().get(0);
		fResource = null;
		fContent = content;
		fCursorPosition = cursorPosition;
	}

	public BasicContext(ScriptType scriptType, Object resource, String content, int cursorPosition) {
		fScriptEngine = null;
		fScriptType = scriptType;
		fResource = resource;
		fContent = content;
		fCursorPosition = cursorPosition;
	}

	@Override
	public List<Object> getTokens() {
		return getInputTokenizer().getTokens(getRelevantText());
	}

	protected InputTokenizer getInputTokenizer() {
		if (getScriptEngine() != null)
			return new InputTokenizer(v -> {
				if (getScriptEngine().hasVariable(v)) {
					final Object variable = getScriptEngine().getVariable(v);
					return variable == null ? null : variable.getClass();
				}

				return null;
			});

		return new InputTokenizer();
	}

	@Override
	public String getText() {
		return fContent;
	}

	private String getRelevantText() {
		return getText().substring(0, getReplaceOffset());
	}

	@Override
	public int getReplaceOffset() {
		return fCursorPosition;
	}

	@Override
	public int getReplaceLength() {
		return 0;
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fScriptEngine;
	}

	@Override
	public List<ModuleDefinition> getLoadedModules() {
		final List<ModuleDefinition> modules = new ArrayList<>();

		if (getScriptEngine() != null) {
			modules.addAll(ModuleHelper.getLoadedModules(getScriptEngine()));

		} else {
			modules.addAll(getLoadedModules(getRelevantText()));

			if (modules.stream().filter(m -> EnvironmentModule.MODULE_NAME.equals(m.getName())).count() == 0)
				modules.add(ModuleHelper.resolveModuleName(EnvironmentModule.MODULE_NAME));
		}

		return modules;
	}

	private static final Pattern LOAD_MODULE_PATTERN = Pattern.compile(EnvironmentModule.LOAD_MODULE_METHOD + "\\([\\\"\\'](.*?)[\\\"\\']");

	private Collection<? extends ModuleDefinition> getLoadedModules(String content) {
		final List<ModuleDefinition> modules = new ArrayList<>();

		final Matcher matcher = LOAD_MODULE_PATTERN.matcher(content);
		while (matcher.find()) {
			final String moduleId = matcher.group(1);
			final ModuleDefinition moduleDefinition = ModuleHelper.resolveModuleName(moduleId);
			if (moduleDefinition != null) {
				if (modules.stream().filter(m -> moduleId.equals(m.getName())).count() == 0)
					modules.add(moduleDefinition);
			}
		}

		return modules;
	}

	private Map<Object, String> getIncludes() {
		return Collections.emptyMap();
	}

	@Override
	public String getFilterToken() {
		final Object lastToken = new TokenList(getTokens()).getLastToken();
		return (isFilter(lastToken)) ? lastToken.toString() : "";
	}

	@Override
	public String getFilter() {
		final String filter = getFilterToken();

		return isStringLiteral(filter) ? filter.substring(1) : filter;
	}

	private boolean isFilter(final Object lastToken) {
		return (lastToken instanceof String) && (!InputTokenizer.isDelimiter(lastToken));
	}

	@Override
	public boolean isStringLiteral(String input) {
		return input.startsWith("\"");
	}

	@Override
	public ScriptType getScriptType() {
		return fScriptType;
	}

	@Override
	public Object getResource() {
		return fResource;
	}
}
