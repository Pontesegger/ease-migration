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

package org.eclipse.ease.lang.javascript.nashorn;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.testhelper.AbstractEnvironmentTest;

public class NashornEnvironmentTest extends AbstractEnvironmentTest {

	@Override
	protected IReplEngine createScriptEngine() {
		final IScriptService scriptService = ScriptService.getService();
		return (IReplEngine) scriptService.getEngineByID(NashornScriptEngine.ENGINE_ID).createEngine();
	}
}
