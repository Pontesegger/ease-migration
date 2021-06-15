/*******************************************************************************
 * Copyright (c) 2018 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.debugging;

import org.eclipse.core.resources.IResource;
import org.eclipse.ease.Script;

/**
 * Simple interface for mapping between data types in different debugger realms.
 *
 * The eclipse framework uses {@link IResource} objects to identify files while EASE relies on {@link Script} objects.
 */
public interface IScriptRegistry {
	/**
	 * Add a new {@link Script} to the registry and store its mapping.
	 *
	 * @param script
	 *            Script to be stored in registry.
	 */
	public void put(Script script);

	/**
	 * Return the {@link Script} identified by this {@link IResource}.
	 *
	 * @param resource
	 *            {@link IResource} to get {@link Script} for.
	 * @return {@link Script} identified by {@link IResource} or {@code null} if no mapping found.
	 */
	public Script getScript(IResource resource);

	/**
	 * Get the {@link IResource} identified by this {@link Script}.
	 *
	 * @param script
	 *            {@link Script} to get {@link IResource} for.
	 * @return {@link IResource} identified by {@link Script} or {@code null} if no mapping found.
	 */
	public IResource getResource(Script script);
}
