/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
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

package org.eclipse.ease;

import java.util.List;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.ScriptType;

public interface ICompletionContext extends IScriptEngineProvider {

	List<Object> getTokens();

	String getText();

	int getReplaceOffset();

	int getReplaceLength();

	/**
	 * Get all loaded modules.
	 *
	 * @return loaded modules
	 */
	List<ModuleDefinition> getLoadedModules();

	/**
	 * Get a text filter to be applied for the current input. This is the prefix of the expected completion proposals.
	 *
	 * @return filter text or empty string
	 */
	String getFilter();

	boolean isStringLiteral(String input);

	boolean isValid();

	ScriptType getScriptType();

	Object getResource();
}
