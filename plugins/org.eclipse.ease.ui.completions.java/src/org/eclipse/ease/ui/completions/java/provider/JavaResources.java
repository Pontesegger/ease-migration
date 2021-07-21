/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completions.java.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public final class JavaResources {

	private static final JavaResources INSTANCE = new JavaResources();

	private static final String PLUGIN_ID = "org.eclipse.ease.ui.completions.java";

	private static final Pattern FQN_CLASSNAME_PATTERN = Pattern.compile("(\\p{Lower}+(?:\\.\\p{Lower}+)*)\\.(\\p{Upper}.*)");

	private static final Collection<String> FILTERED_PACKAGES = Arrays.asList("java.awt", "java.applet", "groovy", "netscape", "kotlin");

	public static JavaResources getInstance() {
		return INSTANCE;
	}

	/** Maps packageName -> {classNames}, eg 'java.io' -> {File, FileBuffer, ...}. */
	private Map<String, Collection<String>> fPackagesAndClasses = Collections.emptyMap();
	private boolean fisLoadingTriggered = false;

	private JavaResources() {
	}

	public Map<String, Collection<String>> getClasses() {
		if (fPackagesAndClasses.isEmpty())
			loadClasses();

		return fPackagesAndClasses;
	}

	public Collection<String> getPackages() {
		return getClasses().keySet();
	}

	public Collection<String> getClasses(String packageName) {
		if (getClasses().containsKey(packageName))
			return getClasses().get(packageName);

		return Collections.emptySet();
	}

	private void loadClasses() {
		if (!fisLoadingTriggered) {
			fisLoadingTriggered = true;

			final Job job = new Job("Load Java code completion") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final Map<String, Collection<String>> loadingContent = new TreeMap<>();

					readJavaPackagesAndClasses(loadingContent);
					readEclipsePackages(loadingContent);

					fPackagesAndClasses = loadingContent;

					return Status.OK_STATUS;
				}
			};

			job.setSystem(true);
			job.schedule();
		}
	}

	private void readEclipsePackages(Map<String, Collection<String>> target) {
		final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

		for (final Bundle bundle : context.getBundles()) {
			for (final String packageName : getExportedPackages(bundle)) {
				if (!isFiltered(packageName))
					registerPackage(target, packageName);
			}
		}
	}

	private void readJavaPackagesAndClasses(Map<String, Collection<String>> target) {
		try (BufferedReader reader = getJavaClassDefinitions()) {
			String entry = reader.readLine();
			while (entry != null) {
				final Matcher matcher = FQN_CLASSNAME_PATTERN.matcher(entry);
				if (matcher.matches())
					registerClass(target, matcher.group(1), matcher.group(2));

				entry = reader.readLine();
			}

		} catch (final IOException e) {
			Logger.error(PLUGIN_ID, "Cannot read package list for code completion", e);
		}
	}

	private void registerPackage(Map<String, Collection<String>> target, String packageName) {
		if (!target.containsKey(packageName)) {
			target.put(packageName, new TreeSet<String>());
		}

		final int delimiterPosition = packageName.lastIndexOf('.');
		if (delimiterPosition > 0)
			registerPackage(target, packageName.substring(0, delimiterPosition));
	}

	private void registerClass(Map<String, Collection<String>> target, final String packageName, String className) {
		if (!isFiltered(packageName)) {
			registerPackage(target, packageName);
			target.get(packageName).add(className);
		}
	}

	private boolean isFiltered(String packageName) {
		for (final String filter : FILTERED_PACKAGES) {
			if (packageName.startsWith(filter))
				return true;
		}

		return false;
	}

	private BufferedReader getJavaClassDefinitions() throws IOException {
		final URL url = new URL("platform:/plugin/" + PLUGIN_ID + "/resources/java" + getJavaMajorVersion() + " classes.txt");
		return new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
	}

	/**
	 * Get the major version number of the java runtime.
	 *
	 * @return java major version, eg 6, 8, 12
	 */
	private int getJavaMajorVersion() {
		int result = 1000; // set to a high value so that the min function below will pick the highest supported version

		final Pattern versionPattern = Pattern.compile("(?:1\\.)?(\\d+).*");
		final String version = System.getProperty("java.runtime.version");
		final Matcher matcher = versionPattern.matcher(version);
		if (matcher.matches())
			result = Integer.parseInt(matcher.group(1));

		return Math.min(result, Activator.JAVA_CLASSES_MAX_VERSION);
	}

	/**
	 * Get a list of all exported packages of a bundle.
	 *
	 * @param bundle
	 *            bundle instance
	 * @return collection of exported packages
	 */
	private Collection<String> getExportedPackages(final Bundle bundle) {
		final Collection<String> exportedPackages = new HashSet<>();

		final String exportPackage = bundle.getHeaders().get("Export-Package");
		if (exportPackage != null) {
			final String[] packages = exportPackage.split(",");
			for (final String packageEntry : packages) {
				String candidate = packageEntry.trim().split(";")[0];
				if (candidate.endsWith("\""))
					candidate = candidate.substring(0, candidate.length() - 1);

				if ((candidate.contains(".internal")) || (packageEntry.contains(";x-internal:=true")))
					// ignore internal packages
					continue;

				if ((candidate.startsWith("Lib")) || (candidate.startsWith("about_files")) || (candidate.startsWith("META")))
					// ignore some dedicated packages
					continue;

				exportedPackages.add(candidate);
			}
		}

		return exportedPackages;
	}
}
