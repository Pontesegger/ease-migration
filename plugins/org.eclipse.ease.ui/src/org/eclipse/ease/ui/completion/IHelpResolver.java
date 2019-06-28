/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

/**
 * Provides help content for completion proposals and help popups. Whenever HTML renderers are available HTML help is preferred over ASCII help.
 */
public interface IHelpResolver {

	/**
	 * Retrieve test help content.
	 *
	 * @return ASCII content
	 */
	String resolveHelp();

	/**
	 * Retrieve HTML help content.
	 *
	 * @return html content
	 */
	String resolveHTMLHelp();
}
