/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.htmlwriter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ease.helpgenerator.model.ExceptionValue;
import org.eclipse.ease.helpgenerator.model.Method;
import org.eclipse.ease.helpgenerator.model.Parameter;
import org.eclipse.ease.helpgenerator.model.ReturnValue;

public class SynopsisBuilder {

	private static final SynopsisBuilder INSTANCE = new SynopsisBuilder();

	public static SynopsisBuilder getInstance() {
		return INSTANCE;
	}

	private SynopsisBuilder() {
	}

	public String build(final Method method) {
		return String.format("%s %s(%s)%s", createClassLink(method.getReturnType()), method.getName(), createParametersString(method.getParameters()),
				createExceptionString(method.getExceptions()));
	}

	private String createExceptionString(List<ExceptionValue> exceptions) {
		final String exceptionString = exceptions.stream().map(except -> createClassLink(except)).collect(Collectors.joining(", "));
		return exceptionString.isEmpty() ? "" : " throws " + exceptionString;
	}

	private String createParametersString(List<Parameter> parameters) {
		return parameters.stream().map(param -> String.format("%s%s %s%s", param.isOptional() ? "<i>[" : "", createClassLink(param), param.getName(),
				param.isOptional() ? "]</i>" : "")).collect(Collectors.joining(", "));
	}

	private String createClassLink(Parameter parameter) {
		if (ReturnValue.VOID.equals(parameter.getTypeName()))
			return ReturnValue.VOID;

		final String[] tokens = parameter.getTypeName().split("(?<=[,<>\\?])|(?=[,<>\\\\?])");
		return Arrays.asList(tokens).stream().map(token -> {
			switch (token) {
			case ",":
				return ", ";
			case "<":
				return "&lt;";
			case ">":
				return "&gt;";
			case "?":
				return "?";
			default:
				final String type = token.trim();
				if (type.endsWith("[]"))
					return String.format("{@link %s}[]", type.substring(0, type.length() - 2));
				else
					return String.format("{@link %s}", type);
			}

		}).collect(Collectors.joining());
	}
}
