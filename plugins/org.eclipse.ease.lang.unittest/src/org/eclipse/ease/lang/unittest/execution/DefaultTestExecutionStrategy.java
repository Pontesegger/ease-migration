/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest.execution;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.lang.unittest.TestSuiteScriptEngine;
import org.eclipse.ease.lang.unittest.definition.Flag;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;

public class DefaultTestExecutionStrategy implements ITestExecutionStrategy {

	private static Collection<ITestEntity> getEntries(ITestEntity root) {
		final Collection<ITestEntity> entries = new HashSet<>();

		entries.add(root);
		if (root instanceof ITestContainer) {
			for (final ITestEntity child : ((ITestContainer) root).getChildren())
				entries.addAll(getEntries(child));
		}

		return entries;
	}

	private TestSuiteScriptEngine fEngine;
	private Collection<ITestEntity> fActiveEntities;
	private boolean fStopSuiteOnError = false;

	@Override
	public void prepareExecution(TestSuiteScriptEngine engine, ITestEntity root) {
		prepareExecution(engine, root, getEntries(root));
	}

	@Override
	public void prepareExecution(TestSuiteScriptEngine engine, ITestEntity root, Collection<ITestEntity> activeEntities) {
		fEngine = engine;
		fActiveEntities = activeEntities;

		for (final ITestEntity entry : activeEntities)
			entry.reset();

		final ITestSuite testSuite = root.getTestSuite();
		if (testSuite != null) {
			testSuite.setMasterEngine(engine);

			final ITestSuiteDefinition definition = testSuite.getDefinition();
			if (definition != null)
				fStopSuiteOnError = definition.getFlag(Flag.STOP_SUITE_ON_ERROR, false);
		}
	}

	@Override
	public void execute(ITestEntity testEntity) {
		if ((!testEntity.getRoot().hasError()) || (!fStopSuiteOnError)) {
			if (fActiveEntities.contains(testEntity))
				testEntity.run(this);
		}
	}

	@Override
	public IScriptEngine createScriptEngine(ITestSuite testSuite, Object resource) {
		return fEngine.createScriptEngine(testSuite, resource);
	}
}
