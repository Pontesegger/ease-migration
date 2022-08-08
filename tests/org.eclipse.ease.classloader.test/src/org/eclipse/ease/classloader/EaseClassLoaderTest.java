/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

package org.eclipse.ease.classloader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EaseClassLoaderTest {

	private Job fJob;

	@BeforeEach
	public void beforeEach() {
		fJob = new Job("dummy") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return null;
			}
		};
	}

	@Test
	@DisplayName("registerURL() does not throw")
	public void registerURL_does_not_throw() throws MalformedURLException {
		final EaseClassLoader classLoader = new EaseClassLoader();

		assertDoesNotThrow(() -> classLoader.registerURL(fJob, new URL("http://eclipse.org/one")));
	}

	@Test
	@DisplayName("registerURL() does not throw on 2nd URL")
	public void registerURL_does_not_throw_on_2nd_URL() throws MalformedURLException {
		final EaseClassLoader classLoader = new EaseClassLoader();

		assertDoesNotThrow(() -> classLoader.registerURL(fJob, new URL("http://eclipse.org/one")));
		assertDoesNotThrow(() -> classLoader.registerURL(fJob, new URL("http://eclipse.org/two")));
	}
}
