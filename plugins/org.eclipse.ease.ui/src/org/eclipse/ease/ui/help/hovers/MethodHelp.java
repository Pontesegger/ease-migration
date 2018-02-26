/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.help.hovers;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IMemento;

public class MethodHelp implements IHoverHelp {

	private class GenericHoverHelp implements IHoverHelp {

		private final String fName;
		private final String fDescription;

		public GenericHoverHelp(String name, String description) {
			fName = name;
			fDescription = description.replaceAll("<a ", "<a class='header' ");
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public String getDescription() {
			return fDescription;
		}

		@Override
		public String getHoverContent() {
			final StringBuilder help = new StringBuilder();
			help.append("<dd>");
			help.append("<b>");
			help.append(getName());
			help.append("</b> ");
			help.append(getDescription());
			help.append("</dd>");

			return help.toString();
		}

	}

	private final URL fHelpLocation;
	private IMemento fHelpContent;
	private final Method fMethod;

	/**
	 * @param helpLocation
	 * @param helpContent
	 * @param method
	 * @throws Exception
	 */
	public MethodHelp(URL helpLocation, IMemento helpContent, Method method) throws Exception {
		fHelpLocation = helpLocation;
		fMethod = method;
		fHelpContent = null;

		// extract method description node
		for (final IMemento node : helpContent.getChildren()) {
			if ((fMethod.getName().equals(node.getString("data-method"))) && ("command".equals(node.getString("class")))) {
				IHoverHelp.updateRelativeLinks(node, fHelpLocation);
				fHelpContent = node;
				break;
			}
		}

		if (fHelpContent == null)
			throw new Exception("Cannot find method help for " + getName());
	}

	@Override
	public String getName() {
		return fMethod.getName();
	}

	@Override
	public String getDescription() {
		for (final IMemento contentNode : fHelpContent.getChildren()) {
			if ("description".equals(contentNode.getString("class")))
				return IHoverHelp.getNodeContent(contentNode);
		}

		return null;
	}

	private String getReturnValueDescription() {
		for (final IMemento contentNode : fHelpContent.getChildren()) {
			if ("return".equals(contentNode.getString("class")))
				return IHoverHelp.getNodeContent(contentNode);
		}

		return null;
	}

	public List<IHoverHelp> getParameterDescriptions() {

		final List<IHoverHelp> parameters = new ArrayList<>();

		for (final IMemento contentNode : fHelpContent.getChildren()) {
			if ("parameters".equals(contentNode.getString("class"))) {
				for (final IMemento child : contentNode.getChildren()) {
					if ("description".equals(child.getString("class"))) {
						final String key = child.getString("data-parameter");
						final String value = IHoverHelp.getNodeContent(child);

						parameters.add(new GenericHoverHelp(key, value) {

							@Override
							public String getName() {
								if (isOptional())
									return "<i>" + super.getName() + "</i>";

								return super.getName();
							}

							private boolean isOptional() {
								return getDescription().contains("<span class=\"optional\">");
							}
						});
					}
				}
			}
		}

		return parameters;

	}

	public List<IHoverHelp> getExceptionDescriptions() {

		final List<IHoverHelp> exceptions = new ArrayList<>();

		for (final IMemento contentNode : fHelpContent.getChildren()) {
			if ("exceptions".equals(contentNode.getString("class"))) {
				String key = null;
				for (final IMemento child : contentNode.getChildren()) {

					if ("description".equals(child.getString("class"))) {
						final String value = IHoverHelp.getNodeContent(child);
						if (key == null)
							key = child.getString("data-exception");

						exceptions.add(new GenericHoverHelp(key, value));
						key = null;

					} else
						key = IHoverHelp.getNodeContent(child);
				}
			}
		}

		return exceptions;
	}

	public List<IHoverHelp> getExamples() {

		final List<IHoverHelp> examples = new ArrayList<>();

		for (final IMemento contentNode : fHelpContent.getChildren()) {
			if ("examples".equals(contentNode.getString("class"))) {
				String key = null;
				for (final IMemento child : contentNode.getChildren()) {

					if ("description".equals(child.getString("class"))) {
						final String value = IHoverHelp.getNodeContent(child);
						if (key != null)
							examples.add(new GenericHoverHelp(key, value) {

								@Override
								public String getHoverContent() {
									final StringBuilder help = new StringBuilder();

									help.append("<dd><div class=\"code\">");
									help.append(getName());
									help.append("</div><div class=\"description\">");
									help.append(getDescription());
									help.append("</div></dd>");

									return help.toString();
								}
							});

						key = null;

					} else
						key = IHoverHelp.getNodeContent(child);
				}
			}
		}

		return examples;
	}

	@Override
	public String getHoverContent() {
		final StringBuffer help = new StringBuffer();

		help.append("<h5>"); //$NON-NLS-1$
		help.append(IHoverHelp.getImageAndLabel(HelpHoverImageProvider.getImageLocation("icons/eobj16/function.png"), extractSynopsis()));
		help.append("</h5>"); //$NON-NLS-1$
		help.append("<br />"); //$NON-NLS-1$

		// method description
		final String description = getDescription();
		if (description != null) {
			help.append("<p>");
			help.append(description);
			help.append("</p>");
		}

		// method parameters
		final List<IHoverHelp> parameterDescriptions = getParameterDescriptions();
		if (!parameterDescriptions.isEmpty()) {
			help.append("<dl>");
			help.append("<dt>Parameters:</dt>");

			for (final IHoverHelp parameter : parameterDescriptions)
				help.append(parameter.getHoverContent());

			help.append("</dl>");
		}

		// return value
		final String returnValueDescription = getReturnValueDescription();
		if (returnValueDescription != null) {
			help.append("<dl>");
			help.append("<dt>Returns:</dt>");
			help.append("<dd>");
			help.append(returnValueDescription);
			help.append("</dd>");
			help.append("</dl>");
		}

		// exceptions
		final List<IHoverHelp> exceptionDescription = getExceptionDescriptions();
		if (!exceptionDescription.isEmpty()) {
			help.append("<dl>");
			help.append("<dt>Throws:</dt>");

			for (final IHoverHelp exception : exceptionDescription)
				help.append(exception.getHoverContent());

			help.append("</dl>");
		}

		// examples
		final List<IHoverHelp> examples = getExamples();
		if (!examples.isEmpty()) {
			help.append("<dl>");
			help.append("<dt>Examples:</dt>");

			for (final IHoverHelp example : examples) {
				help.append(example.getHoverContent());
			}

			help.append("</dl>");
		}

		return help.toString();
	}

	/**
	 * Extract synopsis from HTML description.
	 *
	 * @return synopsis string or <code>null</code>
	 */
	private String extractSynopsis() {
		for (final IMemento child : fHelpContent.getChildren("p")) {
			if ("synopsis".equals(child.getString("class"))) {

				for (final IMemento anchor : child.getChildren("a"))
					anchor.putString("class", "header");

				return IHoverHelp.getNodeContent(child);
			}
		}

		return "";
	}
}
