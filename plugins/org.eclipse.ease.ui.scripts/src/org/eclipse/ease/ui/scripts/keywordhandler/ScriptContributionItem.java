/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.keywordhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.AdaptExpression;
import org.eclipse.core.internal.expressions.IterateExpression;
import org.eclipse.core.internal.expressions.WithExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.scripts.handler.RunScript;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ease.ui.tools.LocationImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class ScriptContributionItem extends CommandContributionItem {

	private static final Pattern ENABLE_PATTERN = Pattern.compile("enableFor\\((.*)\\)");

	private static ImageDescriptor getImageDescriptor(final IScript script) {
		String location = script.getKeywords().get("image");
		if (location != null) {
			String imageLocation = ResourceTools.toAbsoluteLocation(location, script.getLocation());
			if (imageLocation == null)
				imageLocation = location;

			return LocationImageDescriptor.createFromLocation(imageLocation);
		}

		return null;
	}

	private static Map<String, String> getParameters(final IScript script) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put(RunScript.PARAMETER_NAME, script.getPath().toString());

		return parameters;
	}

	private final IScript fScript;
	private Expression fVisibleExpression = null;

	public ScriptContributionItem(final IScript script) {
		super(new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), script.getLocation(), RunScript.COMMAND_ID,
				getParameters(script), getImageDescriptor(script), null, null, script.getName(), null, null, STYLE_PUSH, null, true));

		fScript = script;
	}

	public ScriptContributionItem(final IScript script, final String enablement) {
		this(script);

		Matcher matcher = ENABLE_PATTERN.matcher(enablement);
		if (matcher.matches()) {
			try {
				WithExpression withExpression = new WithExpression("selection");
				IterateExpression iteratorExpression = new IterateExpression(null, Boolean.FALSE.toString());
				AdaptExpression adaptExpression = new AdaptExpression(matcher.group(1));

				withExpression.add(iteratorExpression);
				iteratorExpression.add(adaptExpression);

				fVisibleExpression = withExpression;
			} catch (CoreException e) {
				// TODO provide log message to user

				fVisibleExpression = Expression.FALSE;
			}
		}
	}

	@Override
	public void update() {
		setLabel(fScript.getKeywords().get("name"));
		ParameterizedCommand command = getCommand();
		command.getParameterMap().putAll(getParameters(fScript));

		super.update();
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isVisible() {

		if (fVisibleExpression != null) {
			try {
				final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				EvaluationResult evaluate = fVisibleExpression.evaluate(handlerService.getCurrentState());

				return Boolean.parseBoolean(evaluate.toString());

			} catch (CoreException e) {
				// TODO provide log message to user
				return false;
			}
		}

		return true;
	}
}
