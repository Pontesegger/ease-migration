/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
