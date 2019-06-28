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
package org.eclipse.ease.ui.scripts.expressions.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.ui.scripts.expressions.ExpressionTools;
import org.eclipse.ease.ui.scripts.expressions.ExpressionTools.ExpressionDescription;
import org.eclipse.ease.ui.scripts.expressions.ICompositeExpressionDefinition;
import org.eclipse.ease.ui.scripts.expressions.IExpressionDefinition;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateExpressionHandler extends AbstractHandler implements IHandler {

	public static final String COMMAND_ID_ROOT = "org.eclipse.ease.commands.expressions";
	public static final String COMMAND_ID = COMMAND_ID_ROOT + ".createExpression";
	public static final String PARAMETER_TYPE = COMMAND_ID + ".type";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String type = event.getParameter(PARAMETER_TYPE);

		final Map<String, ExpressionDescription> descriptions = ExpressionTools.loadDescriptions();
		if (descriptions.containsKey(type)) {
			final ExpressionDescription description = descriptions.get(type);
			final IExpressionDefinition expressionDefinition = description.createExpressionDefinition();

			final ISelection selection = HandlerUtil.getActiveMenuSelection(event);
			if (selection instanceof IStructuredSelection) {
				final Object element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof ICompositeExpressionDefinition)
					((ICompositeExpressionDefinition) element).addChild(expressionDefinition);
			}
		}

		return null;
	}
}
