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

package org.eclipse.ease.ui.completion.tokenizer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputTokenizer {

	private static final Pattern PACKAGE_PATTERN = Pattern.compile("(java|com|org)\\.([\\p{Lower}\\d]+\\.?)*");
	private static final Pattern CLASS_PATTERN = Pattern.compile("(java|com|org)\\.([\\p{Lower}\\d]+\\.?)\\.\\p{Upper}(\\w)*");
	private static final Pattern VARIABLES_PATTERN = Pattern.compile("\\p{Alpha}\\w*");

	private static final char[] DELIMITERS = { '.', '(', ',' };

	public static boolean isDelimiter(Object element) {
		for (final char c : DELIMITERS) {
			if (new String(new char[] { c }).equals(element))
				return true;
		}

		return "()".equals(element) || ")".equals(element);
	}

	public static boolean isTextFilter(Object element) {
		return (element instanceof String) && (!isDelimiter(element));
	}

	private final IVariablesResolver fVariablesResolver;

	public InputTokenizer() {
		this(v -> null);
	}

	public InputTokenizer(IVariablesResolver variablesResolver) {
		fVariablesResolver = variablesResolver;
	}

	public List<Object> getTokens(String input) {
		return getTokensFromSimplifiedInput(getSimplifiedInput(input));
	}

	private List<Object> getTokensFromSimplifiedInput(String simpleInput) {
		final List<Object> simpleToken = getSimpleToken(simpleInput);
		if (simpleToken != null)
			return simpleToken;

		final int delimiterPosition = findLastDelimiter(simpleInput);
		if (delimiterPosition > 0)
			return divideAndConquerTokens(simpleInput, delimiterPosition);
		else
			return Arrays.asList(simpleInput);
	}

	private List<Object> divideAndConquerTokens(String simpleInput, final int delimiterPosition) {
		final List<Object> tokens = new ArrayList<>();
		final String beforeDelimiter = simpleInput.substring(0, delimiterPosition);
		final String delimiterAndRest = simpleInput.substring(delimiterPosition).trim();

		tokens.addAll(getTokensFromSimplifiedInput(beforeDelimiter));

		final Class<?> lastClass = getTrailingClassToken(tokens);
		final Method method = (lastClass != null) ? detectMethod(lastClass, delimiterAndRest.substring(1)) : null;

		if (method != null)
			tokens.add(method);

		else if ("()".equals(delimiterAndRest))
			tokens.add(delimiterAndRest);

		else if (delimiterAndRest.startsWith("(") && (delimiterAndRest.length() > 1)) {
			tokens.add("(");
			tokens.add(delimiterAndRest.substring(1).trim());

		} else if (delimiterAndRest.startsWith(".") && (delimiterAndRest.length() > 1)) {
			tokens.add(".");
			tokens.add(delimiterAndRest.substring(1).trim());

		} else if (delimiterAndRest.startsWith(",") && (delimiterAndRest.length() > 1)) {
			tokens.add(",");
			tokens.add(delimiterAndRest.substring(1).trim());

		} else
			tokens.add(delimiterAndRest);

		return tokens;
	}

	private Method detectMethod(Class<?> clazz, String methodName) {
		return Arrays.asList(clazz.getMethods()).stream().filter(m -> methodName.equals(m.getName())).findFirst().orElse(null);
	}

	private Class<?> getTrailingClassToken(List<Object> tokens) {
		Object checkToken = null;
		if ((tokens.size() >= 2) && ("()".equals(tokens.get(tokens.size() - 1)))) {
			checkToken = tokens.get(tokens.size() - 2);

			if (checkToken instanceof Method)
				return ((Method) checkToken).getReturnType();

		} else if (!tokens.isEmpty()) {
			checkToken = tokens.get(tokens.size() - 1);
		}

		if (checkToken instanceof Class<?>)
			return (Class<?>) checkToken;

		return null;
	}

	private int findLastDelimiter(String input) {
		int position = -1;
		for (final char delimiter : DELIMITERS)
			position = Math.max(position, input.lastIndexOf(delimiter));

		return position;
	}

	private List<Object> getSimpleToken(String input) {
		if (input.isEmpty())
			return Collections.emptyList();

		final Matcher variablesMatcher = VARIABLES_PATTERN.matcher(input);
		if (variablesMatcher.matches()) {
			final Class<?> candidate = fVariablesResolver.resolveClass(input);
			if ((candidate != null) && (!isBlacklisted(candidate)))
				return Arrays.asList(candidate, "()");
		}

		final Package packageInstance = getPackage(input);
		if (packageInstance != null)
			return Arrays.asList(packageInstance);

		final Class<?> clazz = getClass(input);
		if (clazz != null)
			return Arrays.asList(clazz);

		return null;
	}

	private boolean isBlacklisted(Class<?> candidate) {
		return candidate.getName().startsWith("org.mozilla.javascript");
	}

	protected Package getPackage(String input) {
		final Matcher packageMatcher = PACKAGE_PATTERN.matcher(input);
		if (packageMatcher.matches()) {
			return Package.getPackage(input);
		}

		return null;
	}

	protected Class<?> getClass(String input) {
		final Matcher classMatcher = CLASS_PATTERN.matcher(input);
		if (classMatcher.matches()) {
			try {
				return getClass().getClassLoader().loadClass(input);
			} catch (final ClassNotFoundException e) {
				// class does not exist
			}
		}

		return null;
	}

	private String getSimplifiedInput(String input) {
		String simplifiedInput = input.trim();
		simplifiedInput = simplifyLiterals(simplifiedInput);
		final String trailingLiteral = getTrailingLiteral(simplifiedInput);
		simplifiedInput = simplifiedInput.substring(0, simplifiedInput.length() - trailingLiteral.length());

		simplifiedInput = simplifyBrackets(simplifiedInput);
		simplifiedInput = simplifyParameters(simplifiedInput);
		simplifiedInput = clipIrrelevantStuff(simplifiedInput);

		return simplifiedInput + trailingLiteral;
	}

	private String getTrailingLiteral(String input) {
		final int literalStart = input.indexOf('"');

		return (literalStart >= 0) ? input.substring(literalStart) : "";
	}

	/**
	 * Remove unused tokens. Removes stuff left of an assignment or left of whitespace.
	 *
	 * @param input
	 *            text to simplify
	 * @return simplified string, 'foo = new File' ... 'File'
	 */
	private String clipIrrelevantStuff(String input) {
		String simplifiedInput = input;

		final int locationOfEquals = simplifiedInput.lastIndexOf('=');
		if (locationOfEquals >= 0)
			simplifiedInput = simplifiedInput.substring(locationOfEquals + 1).trim();

		final int locationOfSpace = simplifiedInput.lastIndexOf(' ');
		if (locationOfSpace >= 0)
			simplifiedInput = simplifiedInput.substring(locationOfSpace + 1).trim();

		final int locationOfTab = simplifiedInput.lastIndexOf('\t');
		if (locationOfTab >= 0)
			simplifiedInput = simplifiedInput.substring(locationOfTab + 1).trim();

		return simplifiedInput;
	}

	/**
	 * Remove parameters in an open bracket when they are not relevant.
	 *
	 * @param input
	 *            text to simplify
	 * @return simplified string, 'foo(42, bar(), another' ... 'foo(,,another'
	 */
	private String simplifyParameters(String input) {
		final BracketMatcher bracketMatcher = new BracketMatcher(input);
		if (bracketMatcher.hasOpenBrackets()) {
			final Bracket openBracket = bracketMatcher.getOpenBrackets().get(0);
			final int lastCommaPosition = input.lastIndexOf(',');

			if (lastCommaPosition > openBracket.getStart()) {
				final StringBuilder simplifiedText = new StringBuilder(input.substring(0, openBracket.getStart() + 1));
				simplifiedText.append(getCommas(input.substring(openBracket.getStart())));
				simplifiedText.append(input.substring(lastCommaPosition + 1).trim());
				return simplifiedText.toString();
			}
		}

		return input;
	}

	private String getCommas(String input) {
		final int amountOfNeededCommas = (int) input.chars().filter(c -> c == ',').count();
		return ",".repeat(amountOfNeededCommas);
	}

	/**
	 * Remove contents within brackets.
	 *
	 * @param input
	 *            text to simplify
	 * @return simplified string, 'foo(42, 12, bar())' ... 'foo()'
	 */
	private String simplifyBrackets(String input) {
		final StringBuilder simplifiedText = new StringBuilder(input);

		while (true) {
			final BracketMatcher bracketMatcher = new BracketMatcher(simplifiedText.toString());
			final Optional<Bracket> bracket = bracketMatcher.getBrackets().stream().filter(b -> b.getStart() < (b.getEnd() - 1)).findFirst();
			if (bracket.isPresent()) {
				simplifiedText.delete(bracket.get().getStart() + 1, bracket.get().getEnd());
			} else
				break;
		}

		return simplifiedText.toString();
	}

	/**
	 * Remove content within String literals.
	 *
	 * @param input
	 *            text to simplify
	 * @return simplified string
	 */
	private String simplifyLiterals(String input) {
		final StringBuilder simplified = new StringBuilder(input);

		int startIndex;
		int endIndex = 0;
		do {
			startIndex = simplified.indexOf("\"");
			if (startIndex >= 0) {
				endIndex = simplified.indexOf("\"", startIndex + 1);

				while ((endIndex > startIndex) && (simplified.charAt(endIndex - 1) == '\\')) {
					endIndex = simplified.indexOf("\"", endIndex + 1);
				}

				if (endIndex > startIndex)
					simplified.delete(startIndex, endIndex + 1);
			}
		} while ((startIndex >= 0) && (endIndex > startIndex));

		return simplified.toString();
	}

	protected boolean isLiteral(final char candidate) {
		return ('"' == candidate);
	}
}
