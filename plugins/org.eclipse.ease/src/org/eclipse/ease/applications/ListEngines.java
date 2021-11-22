/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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
package org.eclipse.ease.applications;

import java.io.PrintStream;

import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class ListEngines implements IApplication {

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		getOutputStream().println("Name: engineID");
		getOutputStream().println("==============");
		final IScriptService service = ScriptService.getService();
		for (final EngineDescription description : service.getEngines())
			getOutputStream().println(String.format("\t%s: %s", description.getName(), description.getID()));

		return 0;
	}

	@Override
	public void stop() {
	}

	private PrintStream getOutputStream() {
		return System.out;
	}
}
