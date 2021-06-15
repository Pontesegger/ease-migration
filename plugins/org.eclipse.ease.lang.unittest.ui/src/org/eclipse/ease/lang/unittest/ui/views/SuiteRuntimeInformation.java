/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.unittest.ui.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class SuiteRuntimeInformation {
	private static final String XML_PARAMETER_TIMING = "timing";
	private static final String XML_NODE_RUN = "run";
	private static final String XML_NODE_FILE = "entity";

	/** Typical script runtime. In case we have no other information. */
	private static final long DEFAULT_RUNTIME = -1;
	private static final int RUNS_TO_SAVE = 10;

	private class RuntimeInformation {

		private final ArrayList<Long> fTimings = new ArrayList<>();

		public synchronized long getEstimatedRuntime() {
			if (fTimings.isEmpty())
				return DEFAULT_RUNTIME;

			// use a weighted average
			long currentWeight = 100;
			long totalWeight = 0;
			long timing = 0;
			for (final long duration : fTimings) {
				timing += duration * currentWeight;
				totalWeight += currentWeight;
				currentWeight *= 0.7;
			}

			return timing / totalWeight;
		}

		public synchronized void addRuntime(final long time) {
			fTimings.add(0, time);
		}

		public ArrayList<Long> getTimings() {
			return fTimings;
		}
	}

	private final HashMap<IPath, RuntimeInformation> fRuntimes = new HashMap<>();
	private final ITestSuite fTestSuite;

	public SuiteRuntimeInformation(final ITestSuite testSuite) {
		fTestSuite = testSuite;
		load();
	}

	private void load() {
		try {
			final XMLMemento root = XMLMemento.createReadRoot(new FileReader(getSettingsFile()));

			for (final IMemento fileNode : root.getChildren(XML_NODE_FILE)) {
				for (final IMemento runNode : fileNode.getChildren(XML_NODE_RUN)) {
					try {
						addTiming(new Path(fileNode.getTextData()), Long.parseLong(runNode.getString(XML_PARAMETER_TIMING)));
					} catch (final NumberFormatException e) {
						// invalid value in XML, ignore value
					}
				}
			}

			// apply loaded data to testsuite
			final Collection<ITestEntity> entries = new HashSet<>();
			entries.add(fTestSuite);
			while (!entries.isEmpty()) {
				final ITestEntity candidate = entries.iterator().next();
				entries.remove(candidate);

				if (candidate.getEstimatedDuration() < 0) {
					final long estimatedDuration = getEstimatedDuration(candidate.getFullPath());
					if (estimatedDuration >= 0) {
						candidate.setEstimatedDuration(estimatedDuration);
					}
				}

				if (candidate instanceof ITestContainer)
					entries.addAll(((ITestContainer) candidate).getCopyOfChildren());
			}

		} catch (final Exception e) {
			// cannot load stats, ignore
		}
	}

	public synchronized void save() {

		// update runtime information from execution data
		final Collection<ITestEntity> entries = new HashSet<>();
		entries.add(fTestSuite);
		while (!entries.isEmpty()) {
			final ITestEntity candidate = entries.iterator().next();
			entries.remove(candidate);

			if ((candidate instanceof ITestFile) && (!TestStatus.NOT_RUN.equals(candidate.getEntityStatus()))) {
				if (!fRuntimes.containsKey(candidate.getFullPath()))
					fRuntimes.put(candidate.getFullPath(), new RuntimeInformation());

				fRuntimes.get(candidate.getFullPath()).addRuntime(candidate.getDuration());
			}

			if (candidate instanceof ITestContainer)
				entries.addAll(((ITestContainer) candidate).getCopyOfChildren());
		}

		// store to file
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(getSettingsFile());

			// create xml output
			final XMLMemento rootNode = XMLMemento.createWriteRoot("root");
			for (final Entry<IPath, RuntimeInformation> entry : fRuntimes.entrySet()) {
				final IMemento fileNode = rootNode.createChild(XML_NODE_FILE);
				fileNode.putTextData(entry.getKey().toString());

				final List<Long> timings = entry.getValue().getTimings();

				for (int index = 0; index < Math.min(timings.size(), RUNS_TO_SAVE); index++) {
					final IMemento runNode = fileNode.createChild(XML_NODE_RUN);
					runNode.putString(XML_PARAMETER_TIMING, Long.toString(timings.get(index)));
				}
			}

			rootNode.save(new OutputStreamWriter(outputStream));

		} catch (final Exception e) {
			// ignore any error on saving stats
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (final IOException e) {
				// fail gracefully
			}
		}
	}

	private synchronized void addTiming(final IPath fileIdentifier, final long timing) {
		if (!fRuntimes.containsKey(fileIdentifier))
			fRuntimes.put(fileIdentifier, new RuntimeInformation());

		fRuntimes.get(fileIdentifier).addRuntime(timing);
	}

	private File getSettingsFile() {
		final IPath path = Activator.getDefault().getStateLocation().append("timing_" + fTestSuite.getName().hashCode() + ".xml");
		return path.toFile();
	}

	public long getEstimatedDuration(IPath fullPath) {
		final RuntimeInformation runtimeInformation = fRuntimes.get(fullPath);
		if (runtimeInformation != null)
			return runtimeInformation.getEstimatedRuntime();

		return -1;
	}
}
