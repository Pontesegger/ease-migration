/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Method extends Field {

	private final Collection<String> fAliases;
	private final List<Parameter> fParameters;
	private final List<ExceptionValue> fExceptions;
	private List<ScriptExample> fExamples;
	private final ReturnValue fReturnType;

	public Method(String name, String comment, String deprecationMessage, Collection<String> aliases, ReturnValue returnType, List<Parameter> parameters,
			List<ExceptionValue> exceptions, List<ScriptExample> examples) {
		super(name, comment, deprecationMessage);

		fAliases = aliases;
		fReturnType = returnType;
		fParameters = parameters;
		fExceptions = exceptions;
		fExamples = (examples != null) ? examples : Collections.emptyList();
	}

	public Collection<String> getAliases() {
		return fAliases;
	}

	public List<Parameter> getParameters() {
		return fParameters;
	}

	public ReturnValue getReturnType() {
		return fReturnType;
	}

	public List<ExceptionValue> getExceptions() {
		return fExceptions;
	}

	public List<ScriptExample> getExamples() {
		return fExamples;
	}

	public void fetchDetailsFrom(Method method) {
		if (getComment().isEmpty())
			setComment(method.getComment());

		if (getDeprecationMessage() == null)
			setDeprecationMessage(method.getDeprecationMessage());

		if (getExamples().isEmpty())
			fExamples = method.getExamples();

		if (getReturnType().getComment().isEmpty())
			getReturnType().setComment(method.getReturnType().getComment());

		for (final Parameter parameter : getParameters()) {
			if (parameter.getComment().isEmpty()) {
				final Optional<Parameter> candidate = method.getParameters().stream().filter(p -> p.getName().equals(parameter.getName())).findFirst();
				if (candidate.isPresent())
					parameter.setComment(candidate.get().getComment());
			}
		}

		for (final ExceptionValue exception : getExceptions()) {
			if (exception.getComment().isEmpty()) {
				final Optional<ExceptionValue> candidate = method.getExceptions().stream().filter(e -> e.getTypeName().equals(exception.getTypeName()))
						.findFirst();
				if (candidate.isPresent())
					exception.setComment(candidate.get().getComment());
			}
		}
	}

	@Override
	public String toString() {
		final String parameters = getParameters().stream().map(p -> p.getName()).collect(Collectors.joining(", "));
		return getReturnType().getTypeName() + " " + getName() + "(" + parameters + ")";
	}
}
