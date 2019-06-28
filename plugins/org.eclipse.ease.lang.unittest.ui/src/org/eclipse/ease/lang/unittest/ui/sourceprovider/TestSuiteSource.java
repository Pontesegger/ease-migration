/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.sourceprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;

public class TestSuiteSource extends AbstractSourceProvider {

	public static final String VARIABLE_TESTSUITE = Activator.PLUGIN_ID + ".testsuite";
	private static final String[] SOURCES = new String[] { VARIABLE_TESTSUITE };

	private Object fCurrentSuite = IEvaluationContext.UNDEFINED_VARIABLE;

	public static TestSuiteSource getActiveInstance() {

		try {
			final ISourceProviderService sourceService = PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			final Object testSuiteSource = sourceService.getSourceProvider(TestSuiteSource.VARIABLE_TESTSUITE);
			if (testSuiteSource instanceof TestSuiteSource)
				return (TestSuiteSource) testSuiteSource;
		} catch (final IllegalStateException e) {
			// no workbench available, we might be running headless
		}

		return null;
	}

	public TestSuiteSource() {
	}

	@Override
	public void dispose() {
		fCurrentSuite = IEvaluationContext.UNDEFINED_VARIABLE;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		final Map<String, Object> result = new HashMap<>();

		result.put(VARIABLE_TESTSUITE, fCurrentSuite);

		return result;
	}

	public void setActiveSuite(final ITestSuite suite) {
		fCurrentSuite = (suite != null) ? suite : IEvaluationContext.UNDEFINED_VARIABLE;

		fireSourceChanged(ISources.ACTIVE_PART, VARIABLE_TESTSUITE, fCurrentSuite);

		final IEvaluationService evaluationService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
		evaluationService.requestEvaluation(VARIABLE_TESTSUITE);
	}

	@Override
	public String[] getProvidedSourceNames() {
		return SOURCES;
	}
}
