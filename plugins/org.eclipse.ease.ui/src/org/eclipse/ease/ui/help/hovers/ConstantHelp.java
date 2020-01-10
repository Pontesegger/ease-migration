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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.IMemento;

public class ConstantHelp implements IHoverHelp {

	private final URL fHelpLocation;
	private final IMemento fHelpContent;
	private final Field fField;

	/**
	 * @param helpLocation
	 * @param helpContent
	 * @param field
	 */
	public ConstantHelp(URL helpLocation, IMemento helpContent, Field field) {
		fHelpLocation = helpLocation;
		fHelpContent = helpContent;
		fField = field;
	}

	@Override
	public String getName() {
		return fField.getName();
	}

	@Override
	public String getDescription() {
		final IMemento descriptionNode = getDescriptionNode();
		if (descriptionNode != null) {
			String content = IHoverHelp.getNodeContent(descriptionNode);
			final IMemento warningNode = getWarningNode(descriptionNode);
			if (warningNode != null) {
				content = content.replace(IHoverHelp.getNodeContent(warningNode), "");
				content = content.replace("<div class=\"warning\"></div>", "");
			}

			return content;
		}

		return "";
	}

	private String getDeprecationMessage() {
		final IMemento descriptionNode = getDescriptionNode();
		if (descriptionNode != null) {
			final IMemento warningNode = getWarningNode(descriptionNode);
			return IHoverHelp.getNodeContent(warningNode);
		}

		return null;
	}

	private IMemento getWarningNode(IMemento descriptionNode) {
		final IMemento warningNode = descriptionNode.getChild("div");
		if (warningNode != null) {
			if ("warning".equals(warningNode.getString("class")))
				return warningNode;
		}

		return null;
	}

	private IMemento getDescriptionNode() {
		for (final IMemento node : fHelpContent.getChildren()) {
			if ("constants".equals(node.getString("class"))) {
				final List<IMemento> candidates = new ArrayList<>();
				candidates.addAll(Arrays.asList(node.getChildren()));

				while (!candidates.isEmpty()) {
					final IMemento candidate = candidates.remove(0);
					if (fField.getName().equals(candidate.getString("data-field"))) {

						IHoverHelp.updateRelativeLinks(candidate, fHelpLocation);
						return candidate;

					} else
						candidates.addAll(Arrays.asList(candidate.getChildren()));
				}

				break;
			}
		}

		return null;
	}

	@Override
	public String getHoverContent() {
		final StringBuffer help = new StringBuffer();
		help.append("<h5>"); //$NON-NLS-1$
		help.append(IHoverHelp.getImageAndLabel(HelpHoverImageProvider.getImageLocation("icons/eobj16/field.png"), getName()));
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
}
