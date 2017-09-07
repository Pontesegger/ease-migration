/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.reporters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.ease.lang.unittest.definition.IVariable;
import org.eclipse.ease.lang.unittest.runtime.IMetadata;
import org.eclipse.ease.lang.unittest.runtime.ITest;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestResult;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * Creates reports of unit test results. See http://llg.cubic.org/docs/junit/ for the accepted XML format
 */
public final class JUnitReportGenerator implements IReportGenerator {

	private interface ITestCounter {
		int getCount(ITestEntity test);
	}

	private static int countTests(final ITestEntity testEntity, ITestCounter counter) {
		if (testEntity instanceof ITest)
			return counter.getCount(testEntity);

		if (testEntity instanceof ITestContainer) {
			int tests = 0;

			if (((ITestContainer) testEntity).getChildren().isEmpty())
				return counter.getCount(testEntity);

			for (final ITestEntity child : ((ITestContainer) testEntity).getChildren())
				tests += countTests(child, counter);

			return tests;
		}

		return 0;
	}

	private static String escape(final String variable) {
		return variable.replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String getClassName(ITestEntity testEntity) {
		if ((testEntity.getParent() != null) && (testEntity.getParent().getParent() != null))
			// we do want to skip the first level as the root object is a virtual element used in the viewer only.
			return getClassName(testEntity.getParent()) + "/" + testEntity.getName().replaceAll("\\s", "_");

		return testEntity.getName();
	}

	@Override
	public String createReport(final String title, final String description, final ITestEntity testEntity) {
		final XMLMemento root = XMLMemento.createWriteRoot("root");

		final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

		final IMemento suiteNode = root.createChild("testsuite");
		suiteNode.putString("name", title);
		suiteNode.putString("hostname", "localhost");
		suiteNode.putInteger("tests", countTests(testEntity, test -> 1));
		suiteNode.putInteger("failures", countTests(testEntity, test -> test.getResults(TestStatus.FAILURE).size()));
		suiteNode.putInteger("errors", countTests(testEntity, test -> test.getResults(TestStatus.ERROR).size()));
		suiteNode.putInteger("disabled", countTests(testEntity, test -> (TestStatus.DISABLED.equals(test.getStatus())) ? 1 : 0));
		suiteNode.putInteger("time", (int) (testEntity.getDuration() / 1000));
		suiteNode.putString("timestamp", timeFormat.format(testEntity.getStartTimestamp()));
		suiteNode.putInteger("id", 0);

		final IMemento propertiesNode = suiteNode.createChild("properties");
		for (final IMetadata metadata : testEntity.getMetadata()) {
			final IMemento propertyNode = propertiesNode.createChild("property ");
			propertyNode.putString("name", escape(metadata.getKey()));
			propertyNode.putString("value", escape((metadata.getValue() != null) ? metadata.getValue().toString() : "null"));
		}

		if (testEntity instanceof ITestSuite) {
			if (((ITestSuite) testEntity).getDefinition() != null) {
				for (final IVariable variable : ((ITestSuite) testEntity).getDefinition().getVariables()) {
					final IMemento propertyNode = propertiesNode.createChild("property");
					propertyNode.putString("name", escape(variable.getName()));
					propertyNode.putTextData(escape(variable.getContent()));
				}
			}
		}

		addTestResults(suiteNode, testEntity);

		return root.toString();
	}

	private void addTestResults(IMemento suiteNode, ITestEntity testEntity) {
		if (testEntity instanceof ITestContainer) {
			for (final ITestEntity child : ((ITestContainer) testEntity).getChildren())
				addTestResults(suiteNode, child);
		}

		if ((!(testEntity instanceof ITestContainer)) || (!testEntity.getResults().isEmpty())) {
			// write result
			final IMemento testcaseNode = suiteNode.createChild("testcase");
			testcaseNode.putString("name", escape(testEntity.getName()));
			testcaseNode.putString("classname", escape(getClassName(testEntity)));
			testcaseNode.putInteger("time", (int) (testEntity.getDuration() / 1000));
			if (TestStatus.DISABLED.equals(testEntity.getStatus())) {
				final IMemento skippedNode = testcaseNode.createChild("skipped");
				skippedNode.putString("message", escape(testEntity.getResults(TestStatus.DISABLED).get(0).getMessage()));
			} else {
				for (final ITestResult testResult : testEntity.getResults()) {
					if (testResult.getStatus() == TestStatus.FAILURE) {
						final IMemento failureNode = testcaseNode.createChild("failure");
						failureNode.putString("message", escape(testResult.getMessage()));
						failureNode.putString("type", "verification mismatch");

					} else if (testResult.getStatus() == TestStatus.ERROR) {
						final IMemento errorNode = testcaseNode.createChild("error");
						errorNode.putString("message", escape(testResult.getMessage()));
						errorNode.putString("type", "script aborted");
					}
				}
			}
		}
	}

	@Override
	public String getDefaultExtension() {
		return "xml";
	}
}