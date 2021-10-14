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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.tokenizer.IClassResolver;
import org.eclipse.ease.ui.completion.tokenizer.IMethodResolver;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;

public class BasicContext implements ICompletionContext {

	private final String fContent;
	private final int fCursorPosition;
	private final IScriptEngine fScriptEngine;
	private final ScriptType fScriptType;
	private final Object fResource;
	private List<Object> fTokenCache = null;

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
		if (fTokenCache == null)
			fTokenCache = getInputTokenizer().getTokens(getRelevantText());

		return fTokenCache;
	}

	@Override
	public boolean isValid() {
		final List<Object> tokens = getTokens();
		return !((tokens.size() == 1) && (InputTokenizer.INVALID.equals(tokens.get(0))));
	}

	protected InputTokenizer getInputTokenizer() {
		return new InputTokenizer(getModuleMethodResolver(), getVariablesResolver());
	}

	protected IClassResolver getVariablesResolver() {
		if (getScriptEngine() != null) {
			return v -> {
				if (getScriptEngine().hasVariable(v)) {
					final Object variable = getScriptEngine().getVariable(v);
					return variable == null ? null : variable.getClass();
				}

				return null;
			};
		}

		return v -> null;
	}

	protected IMethodResolver getModuleMethodResolver() {
		return v -> {
			for (final ModuleDefinition definition : getLoadedModules()) {
				final Optional<Method> matchingMethod = definition.getMethods().stream().filter(m -> m.getName().equals(v)).findFirst();
				if (matchingMethod.isPresent())
					return matchingMethod.get();
			}

			return null;
		};
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
	public String getFilter() {
		final Object lastToken = new TokenList(getTokens()).getLastToken();

		if (lastToken instanceof String) {
			if (!InputTokenizer.isDelimiter(lastToken))
				return (String) lastToken;
		}

		return "";
	}

	@Override
	public boolean isStringLiteral(String input) {
		return input.startsWith("\"") || input.startsWith("'");
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
