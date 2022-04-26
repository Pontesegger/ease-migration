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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.ease.Script;

/**
 * Default implementation of {@link IScriptRegistry} simply using maps to perform 1:1 mapping.
 */
public class ScriptRegistry implements IScriptRegistry {
	/** Lookup from {@link IResource} in eclipse world to {@link Script} in EASE world. */
	private final Map<IResource, Script> fResourceMap = new HashMap<>();

	/** Reverse lookup from {@link Script} in EASE world to {@link IResource} in eclipse world. */
	private final Map<Script, IResource> fScriptMap = new HashMap<>();

	@Override
	public Script getScript(IResource resource) {
		return fResourceMap.get(resource);
	}

	@Override
	public IResource getResource(Script script) {
		return fScriptMap.get(script);
	}

	@Override
	public void put(Script script) {
		if (script.getCommand() instanceof IResource) {
			fResourceMap.put((IResource) script.getCommand(), script);
			fScriptMap.put(script, (IResource) script.getCommand());
		}
	}
}
