/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.help.hovers;

import java.net.URL;

import org.eclipse.ui.IMemento;

public interface IHoverHelp {

	interface IMementoVisitor {
		void visit(IMemento node);
	}

	static void visitMemento(IMemento node, IMementoVisitor visitor) {
		visitor.visit(node);

		for (final IMemento child : node.getChildren())
			visitMemento(child, visitor);
	}

	static String getNodeContent(IMemento node) {
		final String candidate = node.toString();
		int startPos = candidate.indexOf("<" + node.getType());
		if (startPos != -1)
			startPos = candidate.indexOf('>', startPos);

		final int endPos = candidate.lastIndexOf("<");

		if ((startPos != -1) && (endPos != -1) && (startPos < endPos))
			return candidate.substring(startPos + 1, endPos);

		return (node.getTextData() != null) ? node.getTextData() : "";
	}

	/**
	 * Replace relative links in HTML content with absolute links.
	 *
	 * @param contentNode
	 *            original content
	 */
	static void updateRelativeLinks(IMemento contentNode, URL baseUrl) {

		visitMemento(contentNode, (node) -> {
			if ((node.getType().equals("a")) && (node.getString("href") != null))
				node.putString("href", resolveUrl(baseUrl, node.getString("href")));

			if ((node.getType().equals("img")) && (node.getString("src") != null))
				node.putString("src", resolveUrl(baseUrl, node.getString("src")));
		});
	}

	static String resolveUrl(URL baseUrl, String relativeLocation) {
		if (relativeLocation.contains("://"))
			return relativeLocation;

		final String baseLocation = baseUrl.toString();

		if (relativeLocation.startsWith("#"))
			return baseLocation + relativeLocation;

		if (relativeLocation.startsWith("/")) {
			final int hostPosition = baseLocation.indexOf(baseUrl.getHost());
			return baseLocation.substring(0, hostPosition + baseUrl.getHost().length()) + relativeLocation;
		}

		final int lastPathDelimiter = baseLocation.lastIndexOf('/');
		if (lastPathDelimiter > 0)
			return baseLocation.substring(0, lastPathDelimiter + 1) + relativeLocation;

		return "";
	}

	static String getImageAndLabel(String imageSrcPath, String label) {
		final StringBuffer buf = new StringBuffer();
		final int imageWidth = 16;
		final int imageHeight = 16;
		final int labelLeft = 20;
		final int labelTop = 2;

		buf.append("<div style='word-wrap: break-word; position: relative; "); //$NON-NLS-1$

		if (imageSrcPath != null) {
			buf.append("margin-left: ").append(labelLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("padding-top: ").append(labelTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		buf.append("'>"); //$NON-NLS-1$
		if (imageSrcPath != null) {
			final StringBuffer imageStyle = new StringBuffer("border:none; position: absolute; "); //$NON-NLS-1$
			imageStyle.append("width: ").append(imageWidth).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("height: ").append(imageHeight).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("left: ").append(-labelLeft - 1).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$

			// hack for broken transparent PNG support in IE 6, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=223900 :
			buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
			final String tooltip = ""; //$NON-NLS-1$
			buf.append("<span ").append(tooltip).append("style=\"").append(imageStyle). //$NON-NLS-1$ //$NON-NLS-2$
					append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageSrcPath).append("')\"></span>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$

			buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
			buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='").append(imageSrcPath).append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
			buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
			buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='").append(imageSrcPath).append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			buf.append("<![endif]-->\n"); //$NON-NLS-1$
		}

		buf.append(label);

		buf.append("</div>"); //$NON-NLS-1$
		return buf.toString();
	}

	String getName();

	String getDescription();

	String getHoverContent();
}
