/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.io.InputStream;

/**
 * Generic interface for a scriptable object. Allows to provide adapters for any kind of object.
 */
public interface IScriptable {

	/**
	 * Get input stream containing source code.
	 * 
	 * @return source code as {@link InputStream}
	 * @throws Exception
	 *             when inputStream cannot be created
	 */
	InputStream getSourceCode() throws Exception;
}
