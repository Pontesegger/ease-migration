/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ease.IScriptable;

/**
 * Adapts files to {@link IScriptable}s. Works for eclipse workspace files and java File objects.
 */
public class ScriptableAdapter implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(IScriptable.class)) {
			if (adaptableObject instanceof IFile)
				return (T) (IScriptable) () -> ((IFile) adaptableObject).getContents();

			if (adaptableObject instanceof File)
				return (T) (IScriptable) () -> new FileInputStream((File) adaptableObject);

			if (adaptableObject instanceof URL)
				return (T) (IScriptable) () -> ((URL) adaptableObject).openStream();

			if (adaptableObject instanceof URI)
				return (T) (IScriptable) () -> ((URI) adaptableObject).toURL().openStream();
		}

		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IScriptable.class };
	}
}
