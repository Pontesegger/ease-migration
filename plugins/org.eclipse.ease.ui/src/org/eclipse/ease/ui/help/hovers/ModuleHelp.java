/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.help.hovers;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

public class ModuleHelp implements IHoverHelp {

	/**
	 * Retrieve help page URL for a given module definition.
	 *
	 * @param definition
	 *            module definition to get location for
	 * @return help URL
	 */
	public static URL getModuleHelpLocation(final ModuleDefinition definition) {
		final String helpLocation = definition.getHelpLocation(null);
		return PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);
	}

	/**
	 * Retrieve help page for a given module definition.
	 *
	 * @param url
	 *            url to read help from
	 * @return help content (HTML body node)
	 * @throws Exception
	 */
	private static IMemento getHtmlContent(final URL url) throws Exception {
		try {
			final IMemento rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
			return rootNode.getChild("body");
		} catch (final Exception e) {
			Logger.error(Activator.PLUGIN_ID, "Cannot find the module help content ", e);
			throw e;
		}
	}

	private final IMemento fHelpContent;
	private final URL fHelpLocation;

	/**
	 * Help provider for modules, methods and fields.
	 *
	 * @param helpLocation
	 *            location of help html file.
	 * @throws Exception
	 *             when help content cannot be read
	 */
	public ModuleHelp(URL helpLocation) throws Exception {
		fHelpLocation = helpLocation;
		fHelpContent = getHtmlContent(helpLocation);
	}

	@Override
	public String getName() {
		for (final IMemento node : fHelpContent.getChildren()) {
			if ("module".equals(node.getString("class")))
				return node.getString("title");
		}

		return "";
	}

	@Override
	public String getDescription() {
		for (final IMemento node : fHelpContent.getChildren()) {
			if ("module".equals(node.getString("class"))) {
				for (final IMemento contentNode : node.getChildren()) {
					if ("description".equals(contentNode.getString("class"))) {
						IHoverHelp.updateRelativeLinks(contentNode, fHelpLocation);

						return IHoverHelp.getNodeContent(contentNode);
					}
				}
			}
		}

		return "";
	}

	public String getDeprecationMessage() {
		for (final IMemento node : fHelpContent.getChildren()) {
			if ("module".equals(node.getString("class"))) {
				for (final IMemento contentNode : node.getChildren()) {
					if ("deprecated".equals(contentNode.getString("class"))) {
						IHoverHelp.updateRelativeLinks(contentNode, fHelpLocation);

						String content = IHoverHelp.getNodeContent(contentNode);
						if (content.contains("<span class=\"warning\"></span>"))
							content = content.substring(content.indexOf("<span class=\"warning\"></span>") + "<span class=\"warning\"></span>".length());

						return content;
					}
				}
			}
		}

		return "";
	}

	@Override
	public String getHoverContent() {

		final StringBuffer help = new StringBuffer();
		help.append("<h5>"); //$NON-NLS-1$
		help.append(IHoverHelp.getImageAndLabel(HelpHoverImageProvider.getImageLocation("icons/eobj16/module.png"), getName()));
		help.append("</h5>"); //$NON-NLS-1$

		final String deprecationMessage = getDeprecationMessage();
		if (!deprecationMessage.isEmpty()) {
			help.append("<p>"); //$NON-NLS-1$
			help.append(deprecationMessage);
			help.append("</p>"); //$NON-NLS-1$
		}

		final String description = getDescription();
		if (!description.isEmpty()) {
			help.append("<p>"); //$NON-NLS-1$
			help.append(description);
			help.append("</p>"); //$NON-NLS-1$
		}

		return help.toString();
	}

	public IHoverHelp getConstantHelp(final Field field) {
		return new ConstantHelp(fHelpLocation, fHelpContent, field);
	}

	public IHoverHelp getMethodHelp(final Method method) throws Exception {
		return new MethodHelp(fHelpLocation, fHelpContent, method);
	}
}
