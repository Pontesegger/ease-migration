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
package org.eclipse.ease.lang.javascript.rhino;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ease.engine.javascript.rhino.debugger";

	private static BundleContext fContext;

	public static BundleContext getContext() {
		return fContext;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		fContext = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		fContext = null;
	}
}
