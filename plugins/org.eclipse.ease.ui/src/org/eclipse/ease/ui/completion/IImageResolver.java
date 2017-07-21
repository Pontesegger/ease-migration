/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import org.eclipse.swt.graphics.Image;

public interface IImageResolver {

	/**
	 * Returns an image instance. Only called when the actual image gets displayed.
	 * 
	 * @return image to be displayed in proposal
	 */
	Image getImage();
}
