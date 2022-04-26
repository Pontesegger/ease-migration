/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

/**
 * A classloader using 'Eclipse-BuddyPolicy: global' as class loading strategy. It further allows to register additional jar files to be looked up.
 */
public class EaseClassLoader extends ClassLoader {

	private final Map<Job, URLClassLoader> fRegisteredJars = new HashMap<>();

	/** Marker that we are currently looking within a specific URLClassLoader. */
	private final Collection<URLClassLoader> fTraversingURLClassLoader = new HashSet<>();

	/**
	 * Constructor for the class loader.
	 */
	public EaseClassLoader() {
		super(FrameworkUtil.getBundle(EaseClassLoader.class).adapt(BundleWiring.class).getClassLoader());
	}

	/**
	 * Constructor using a given parent classloader. When using this classloader the Eclipse-BuddyPolicy from the parent classloader bundle will be used.
	 *
	 * @param parent
	 *            parent classloader
	 */
	public EaseClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> findClass(final String name) throws ClassNotFoundException {
		// try to load from jars
		final Job currentJob = Job.getJobManager().currentJob();
		final URLClassLoader classLoader = fRegisteredJars.get(currentJob);
		if (classLoader != null) {
			// the EaseClassLoader will query its parent (this) if it cannot find the requested class, keep a marker to break the cycle
			if (!fTraversingURLClassLoader.contains(classLoader)) {
				fTraversingURLClassLoader.add(classLoader);
				try {
					final Class<?> clazz = classLoader.loadClass(name);
					if (clazz != null)
						return clazz;

				} catch (final ClassNotFoundException e) {
					// ignore, class not found in registered JARs
				} finally {
					// clear marker
					fTraversingURLClassLoader.remove(classLoader);
				}
			}
		}

		// not found in jars, delegate to eclipse loader
		return super.findClass(name);
	}

	/**
	 * Add a URL to the search path of the classloader. Currently detects classes only, not resources.
	 *
	 * @param engine
	 *            script engine used
	 * @param url
	 *            url to add to classpath
	 */
	public void registerURL(final Job engine, final URL url) {
		// engine needs to be registered as we use a single classloader for multiple script engines.
		if (!fRegisteredJars.containsKey(engine))
			fRegisteredJars.put(engine, URLClassLoader.newInstance(new URL[] { url }, this));

		else {
			final List<URL> urlList = Arrays.asList(fRegisteredJars.get(engine).getURLs());
			if (!urlList.contains(url)) {
				urlList.add(url);
				fRegisteredJars.put(engine, URLClassLoader.newInstance(urlList.toArray(new URL[0]), this));
			}
		}
	}

	public void unregisterEngine(final Job engine) {
		final URLClassLoader classLoader = fRegisteredJars.remove(engine);
		if (classLoader != null) {
			try {
				classLoader.close();
			} catch (final IOException e) {
				// gracefully ignore
			}
		}
	}
}
