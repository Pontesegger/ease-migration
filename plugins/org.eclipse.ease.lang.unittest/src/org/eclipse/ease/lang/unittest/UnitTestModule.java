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

package org.eclipse.ease.lang.unittest;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.eclipse.ease.ExitException;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.lang.unittest.definition.Flag;
import org.eclipse.ease.lang.unittest.definition.ICode;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.runtime.IMetadata;
import org.eclipse.ease.lang.unittest.runtime.IRuntimeFactory;
import org.eclipse.ease.lang.unittest.runtime.ITest;
import org.eclipse.ease.lang.unittest.runtime.ITestClass;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestResult;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.IScriptFunctionModifier;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;

/**
 * Support methods for scripted unit tests. Provides several assertion methods and utility functions to manipulate the current test instances and states.
 */
public class UnitTestModule extends AbstractScriptModule implements IScriptFunctionModifier {

	private static final String ASSERTION_FUNCION_NAME = "assertion";

	private ITestClass fCurrentTestClass = null;
	private ITest fCurrentTest = null;
	private boolean fThrowOnFailure = false;

	/**
	 * Start a specific unit test. Started tests should be terminated by an {@link #endTest()}.
	 *
	 * @param title
	 *            name of test
	 * @param description
	 *            short test description
	 */
	@WrapToScript
	public final void startTest(final String title, @ScriptParameter(defaultValue = "") final String description) {
		ITestContainer container = fCurrentTestClass;

		if (container == null) {
			final ITestFile testFile = getTestFile();
			if (testFile != null)
				container = testFile;
			else
				throw new RuntimeException("Could not detect current test file. The test framework is broken.");
		}

		fCurrentTest = IRuntimeFactory.eINSTANCE.createTest();
		fCurrentTest.setName(title);
		fCurrentTest.setDescription(description);
		fCurrentTest.setEntityStatus(TestStatus.RUNNING);

		if (getScriptEngine() instanceof IDebugEngine)
			fCurrentTest.setStackTrace(((IDebugEngine) getScriptEngine()).getStackTrace());

		container.getChildren().add(fCurrentTest);
	}

	/**
	 * End the current test. Does nothing if no test was started.
	 */
	@WrapToScript
	public final void endTest() {
		if (fCurrentTest != null) {
			fCurrentTest.setEntityStatus(TestStatus.PASS);
			fCurrentTest = null;
		}
	}

	/**
	 * Get the currently executed test file instance. The test file is not a file instance but the runtime representation of a testsuite test file.
	 *
	 * @return test file instance
	 */
	@WrapToScript
	public ITestFile getTestFile() {
		return (ITestFile) getScriptEngine().getVariable(TestSuiteScriptEngine.TEST_FILE_VARIABLE);
	}

	/**
	 * Append generic data to the current test, testfile or test suite.
	 *
	 * @param name
	 *            key to use. Has to be unique for this test object
	 * @param value
	 *            data to be stored
	 */
	@WrapToScript
	public void addMetaData(String name, Object value) {
		final IMetadata metadata = IRuntimeFactory.eINSTANCE.createMetadata();
		metadata.setKey(name);
		metadata.setValue(value);

		if (getScriptEngine() instanceof IDebugEngine)
			metadata.setStackTrace(((IDebugEngine) getScriptEngine()).getStackTrace());

		if (fCurrentTest != null)
			fCurrentTest.getMetadata().add(metadata);
		else
			getTestFile().getMetadata().add(metadata);
	}

	/**
	 * Expect two objects to be equal.
	 *
	 * @param expected
	 *            expected result
	 * @param actual
	 *            actual result
	 * @param errorDescription
	 *            optional error text to be displayed when not equal
	 * @return assertion containing comparison result
	 */
	@WrapToScript
	public static IAssertion assertEquals(final Object expected, final Object actual,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		if (expected != null)
			return new DefaultAssertion(expected.equals(actual),
					(errorDescription == null) ? "Objects do not match: expected <" + expected + ">, actual <" + actual + ">" : errorDescription.toString());

		return assertNull(actual, errorDescription);
	}

	/**
	 * Asserts when provided value does not match to a given regular expression pattern.
	 *
	 * @param pattern
	 *            pattern to match
	 * @param candidate
	 *            text to be matched
	 * @param errorMessage
	 *            error message in case of a mismatch
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertMatch(String pattern, String candidate, @ScriptParameter(defaultValue = ScriptParameter.NULL) String errorMessage) {
		if (Pattern.matches(pattern, candidate))
			return IAssertion.VALID;

		return new DefaultAssertion((errorMessage != null) ? errorMessage : "\"" + candidate + "\" does not match pattern \"" + pattern + "\"");
	}

	/**
	 * Expect two objects not to be equal.
	 *
	 * @param expected
	 *            unexpected result
	 * @param actual
	 *            actual result
	 * @param errorDescription
	 *            optional error text to be displayed when equal
	 * @return assertion containing comparison result
	 */
	@WrapToScript
	public static IAssertion assertNotEquals(final Object expected, final Object actual,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		if (expected != null)
			return new DefaultAssertion(!expected.equals(actual), (errorDescription == null) ? "Objects match" : errorDescription.toString());

		return assertNotNull(actual, errorDescription);
	}

	/**
	 * Asserts when provided value is not <code>null</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertNull(final Object actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(actual == null, (errorDescription == null) ? "Object is not null, actual <" + actual + ">" : errorDescription.toString());
	}

	/**
	 * Asserts when provided value is <code>null</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertNotNull(final Object actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(actual != null, (errorDescription == null) ? "Object is null" : errorDescription.toString());
	}

	/**
	 * Asserts when provided value is <code>false</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertTrue(final Boolean actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(actual, (errorDescription == null) ? "Value is false" : errorDescription.toString());
	}

	/**
	 * Asserts when provided value is <code>true</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertFalse(final Boolean actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(!actual, (errorDescription == null) ? "Value is true" : errorDescription.toString());
	}

	/**
	 * Changes behavior for assertion handling. By default assertions do not cause an exception to be thrown, which supports classic (functional) test mode.
	 * Object oriented test mode enhances exceptions to be thrown on assertions.
	 *
	 * @param throwOnFailure
	 *            <code>true</code> to thow exceptions on assertions
	 */
	public void setThrowOnFailure(boolean throwOnFailure) {
		fThrowOnFailure = throwOnFailure;
	}

	@Override
	public String getPreExecutionCode(Method method) {
		return "";
	}

	@Override
	public String getPostExecutionCode(final Method method) {
		if ("JavaScript".equals(getScriptEngine().getDescription().getSupportedScriptTypes().get(0).getName())) {
			if (IAssertion.class.isAssignableFrom(method.getReturnType()))
				return ASSERTION_FUNCION_NAME + "(" + IScriptFunctionModifier.RESULT_NAME + ");\n";
		}

		return "";
	}

	/**
	 * Create a new assertion for the current test. According to the assertion status an error might be added to the current testcase.
	 *
	 * @param reason
	 *            assertion to be checked
	 * @throws AssertionException
	 *             in case {@link #setThrowOnFailure(boolean)} is enabled
	 */
	@WrapToScript
	public final void assertion(final IAssertion reason) throws AssertionException {
		if (!reason.isValid()) {
			if (fThrowOnFailure)
				reason.throwOnFailure();
			else
				failure(reason.toString());
		}
	}

	public ITestContainer addTestClass(String className) {
		if (fCurrentTestClass != null) {
			// testclass finished
			fCurrentTestClass.setEntityStatus(TestStatus.FINISHED);
			fCurrentTestClass = null;
		}

		if (className != null) {
			// new testclass created
			final ITestFile testFile = getTestFile();
			fCurrentTestClass = IRuntimeFactory.eINSTANCE.createTestClass();
			fCurrentTestClass.setName(className);
			fCurrentTestClass.setEntityStatus(TestStatus.RUNNING);

			if (getScriptEngine() instanceof IDebugEngine)
				fCurrentTestClass.setStackTrace(((IDebugEngine) getScriptEngine()).getStackTrace());

			testFile.getChildren().add(fCurrentTestClass);
		}

		return fCurrentTestClass;
	}

	/**
	 * Ignore the current test.
	 *
	 * @param reason
	 *            message why the test got ignored.
	 */
	@WrapToScript
	public void ignoreTest(@ScriptParameter(defaultValue = "") String reason) {
		if (fCurrentTest != null)
			addResult(TestStatus.DISABLED, reason, null);
	}

	/**
	 * Force a failure (=assertion) for the current test entity (test/testclass/testfile/testsuite).
	 *
	 * @param message
	 *            failure message
	 */
	@WrapToScript
	public void failure(String message) {
		failure(message, null);
	}

	/**
	 * Called from the javascript runner.
	 *
	 * @param message
	 *            failure message
	 * @param stackTrace
	 *            stacktrace of failure event
	 */
	public void failure(String message, ScriptStackTrace stackTrace) {
		final ITestSuiteDefinition definition = getTestSuite().getDefinition();
		if ((definition != null) && (definition.getFlag(Flag.PROMOTE_FAILURE_TO_ERROR, false)))
			error(message, stackTrace);
		else
			addResult(TestStatus.FAILURE, message, stackTrace);
	}

	private void addResult(TestStatus status, String message, ScriptStackTrace stackTrace) {
		final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
		result.setStatus(status);
		result.setMessage(message);

		if (stackTrace != null)
			result.setStackTrace(stackTrace);

		else if (getScriptEngine() instanceof IDebugEngine)
			result.setStackTrace(((IDebugEngine) getScriptEngine()).getStackTrace());

		getCurrentTest().getResults().add(result);
	}

	/**
	 * Get the current unit test.
	 *
	 * @return the current test or a generic global test scope if called outside of a valid testcase
	 */
	@WrapToScript
	public ITest getCurrentTest() {
		if (fCurrentTest == null)
			return getTestFile().getTest(ITestEntity.GLOBAL_SCOPE_TEST);

		return fCurrentTest;
	}

	/**
	 * Get the current test suite.
	 *
	 * @return test suite instance
	 */
	@WrapToScript
	public ITestSuite getTestSuite() {
		return getTestFile().getTestSuite();
	}

	/**
	 * Force an error for the current test entity (test/testclass/testfile/testsuite).
	 *
	 * @param message
	 *            error message
	 */
	@WrapToScript
	public void error(String message) throws Exception {
		error(message, null);

		throw new ExitException();
	}

	/**
	 * Called from the javascript runner.
	 *
	 * @param message
	 *            error message
	 * @param stackTrace
	 *            stacktrace of error event
	 */
	public void error(String message, ScriptStackTrace stackTrace) {
		addResult(TestStatus.ERROR, message, stackTrace);
	}

	/**
	 * Set the timeout for the current test. If test execution takes longer than the timeout, the test is marked as failed.
	 *
	 * @param timeout
	 *            timeout in [ms]
	 */
	@WrapToScript
	public void setTestTimeout(long timeout) {
		if (fCurrentTest != null)
			getCurrentTest().setDurationLimit(timeout);
	}

	/**
	 * Execute code registered in the testsuite.
	 *
	 * @param location
	 *            name of the code fragment to execute.
	 * @return execution result
	 * @throws Exception
	 *             when no user specific code can be found or the injected code throws
	 */
	@WrapToScript
	public Object executeUserCode(String location) throws Exception {
		final ITestSuiteDefinition definition = getTestSuite().getDefinition();
		if (definition != null) {
			final ICode customCode = definition.getCustomCode(location);
			if (customCode != null)
				return getScriptEngine().inject(new Script(customCode.getLocation(), customCode.getContent()));
			else
				throw new Exception("No user specific code for \"" + location + "\" found.");

		} else
			throw new Exception("No user specific code for \"" + location + "\" found.");
	}
}
