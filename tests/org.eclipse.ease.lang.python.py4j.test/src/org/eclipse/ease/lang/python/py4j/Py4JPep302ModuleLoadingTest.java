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

package org.eclipse.ease.lang.python.py4j;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.lang.python.py4j.internal.Py4jScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.testhelper.python.AbstractPep302ModuleLoadingTest;

public class Py4JPep302ModuleLoadingTest extends AbstractPep302ModuleLoadingTest {

	protected IReplEngine createEngine() {
		final IScriptService scriptService = ScriptService.getService();
		final EngineDescription description = scriptService.getEngineByID(Py4jScriptEngine.ENGINE_ID);
		return (IReplEngine) description.createEngine();
	}
}
