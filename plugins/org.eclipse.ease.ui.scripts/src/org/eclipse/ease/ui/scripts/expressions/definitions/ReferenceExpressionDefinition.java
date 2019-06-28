/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.expressions.definitions;

import java.util.Collection;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.ReferenceExpression;

@SuppressWarnings("restriction")
public class ReferenceExpressionDefinition extends AbstractExpressionDefinition {

	@Override
	public Expression toCoreExpression() {
		final Collection<Parameter> parameters = getParameters();
		if (parameters.size() == 1)
			return new ReferenceExpression(getParameters().iterator().next().getValue());

		// an empty reference should resolve to true
		return Expression.TRUE;
	}
}
