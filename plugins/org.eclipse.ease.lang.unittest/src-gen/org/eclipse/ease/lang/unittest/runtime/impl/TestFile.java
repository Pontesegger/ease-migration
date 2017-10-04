/**
 */
package org.eclipse.ease.lang.unittest.runtime.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.unittest.TestSuiteScriptEngine;
import org.eclipse.ease.lang.unittest.definition.Flag;
import org.eclipse.ease.lang.unittest.definition.ICode;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.execution.ITestExecutionStrategy;
import org.eclipse.ease.lang.unittest.runtime.IRuntimeFactory;
import org.eclipse.ease.lang.unittest.runtime.IRuntimePackage;
import org.eclipse.ease.lang.unittest.runtime.ITestClass;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestResult;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Test File</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.ease.lang.unittest.runtime.impl.TestFile#getInsertionOrder <em>Insertion Order</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TestFile extends TestContainer implements ITestFile {

	/** Script engine used while executing the test file content. May be <code>null</code>. */
	private IScriptEngine fScriptEngine = null;

	/**
	 * The default value of the '{@link #getInsertionOrder() <em>Insertion Order</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #getInsertionOrder()
	 * @generated
	 * @ordered
	 */
	protected static final int INSERTION_ORDER_EDEFAULT = 0;
	/**
	 * The cached value of the '{@link #getInsertionOrder() <em>Insertion Order</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #getInsertionOrder()
	 * @generated
	 * @ordered
	 */
	protected int insertionOrder = INSERTION_ORDER_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	protected TestFile() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IRuntimePackage.Literals.TEST_FILE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public int getInsertionOrder() {
		return insertionOrder;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void setInsertionOrder(int newInsertionOrder) {
		final int oldInsertionOrder = insertionOrder;
		insertionOrder = newInsertionOrder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IRuntimePackage.TEST_FILE__INSERTION_ORDER, oldInsertionOrder, insertionOrder));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case IRuntimePackage.TEST_FILE__INSERTION_ORDER:
			return getInsertionOrder();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case IRuntimePackage.TEST_FILE__INSERTION_ORDER:
			setInsertionOrder((Integer) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case IRuntimePackage.TEST_FILE__INSERTION_ORDER:
			setInsertionOrder(INSERTION_ORDER_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case IRuntimePackage.TEST_FILE__INSERTION_ORDER:
			return insertionOrder != INSERTION_ORDER_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		final StringBuffer result = new StringBuffer(super.toString());
		result.append(" (insertionOrder: ");
		result.append(insertionOrder);
		result.append(')');
		return result.toString();
	}

	/**
	 * @generated NOT
	 */
	@Override
	public void run(ITestExecutionStrategy strategy) {
		// first check for disabled tests
		if (TestStatus.DISABLED.equals(getStatus()))
			return;

		fScriptEngine = strategy.createScriptEngine(getTestSuite(), getResource());
		if (fScriptEngine != null) {

			setEntityStatus(TestStatus.RUNNING);

			// execute resource
			try {
				fScriptEngine.setVariable(TestSuiteScriptEngine.TEST_FILE_VARIABLE, this);
				fScriptEngine.setVariable(TestSuiteScriptEngine.TEST_SUITE_VARIABLE, getTestSuite());
				fScriptEngine.setTerminateOnIdle(false);
				fScriptEngine.schedule();

				// testfile setup
				runSetupTeardownCode(ITestSuiteDefinition.CODE_LOCATION_TESTFILE_SETUP);

				// classic test execution
				if (!hasError()) {

					// load service directly to allow to work in headless mode
					final IScriptService scriptService = ScriptService.getInstance();
					final ScriptType scriptType = scriptService.getScriptType(getResource().toString());

					if (scriptType.getName().equals("Python")) {
						// load the unittest runner prefix
						try {
							final URL url = new URL("platform:/plugin/org.eclipse.ease.lang.unittest/resources/testrunner/testrunner_definitions.py");
							final ScriptResult executionResult = fScriptEngine.executeSync(url);
							if (executionResult.hasException())
								getTest("[EASE Testrunner]").addError(executionResult.getException().getMessage(), fScriptEngine);

						} catch (final MalformedURLException e) {
							getTest("[EASE Testrunner]").addError(e.getMessage(), fScriptEngine);
						}
					}

					ScriptResult executionResult = fScriptEngine.executeSync(getResource());

					if (executionResult.hasException()) {
						// we should mark the last test as invalid
						if (!getChildren().isEmpty()) {
							final ITestEntity test = getChildren().get(getChildren().size() - 1);
							if (test.getStatus() == TestStatus.RUNNING) {
								final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
								result.setStatus(TestStatus.ERROR);
								result.setMessage(executionResult.getException().getMessage());

								if (fScriptEngine instanceof IDebugEngine)
									result.setStackTrace(((IDebugEngine) fScriptEngine).getExceptionStackTrace());

								test.getResults().add(result);

								test.setEntityStatus(TestStatus.FINISHED);

							} else {
								// error in global scope
								getTest(ITestEntity.GLOBAL_SCOPE_TEST).addError(executionResult.getException().getMessage(), fScriptEngine);
							}

						} else {
							// error in global scope
							getTest(ITestEntity.GLOBAL_SCOPE_TEST).addError(executionResult.getException().getMessage(), fScriptEngine);
						}
					}

					// now check if there were any tests included. If not use testrunner to launch tests
					if (getChildren().isEmpty()) {

						// TODO check if we can have a smarter binding than a string constant
						if (scriptType.getName().equals("JavaScript")) {
							if (!fScriptEngine.hasVariable("__EASE_UnitTest_TestRunner")) {
								// load the unittest runner
								try {
									final URL url = new URL("platform:/plugin/org.eclipse.ease.lang.unittest/resources/testrunner/testrunner.js");
									executionResult = fScriptEngine.executeSync(url);
									if (executionResult.hasException())
										getTest("[EASE Testrunner]").addError(executionResult.getException().getMessage(), fScriptEngine);

								} catch (final MalformedURLException e) {
									getTest("[EASE Testrunner]").addError(e.getMessage(), fScriptEngine);
								}
							}

						} else if (scriptType.getName().equals("Python")) {
							// load the unittest runner
							try {
								final URL url = new URL("platform:/plugin/org.eclipse.ease.lang.unittest/resources/testrunner/testrunner.py");
								executionResult = fScriptEngine.executeSync(url);
								if (executionResult.hasException())
									getTest("[EASE Testrunner]").addError(executionResult.getException().getMessage(), fScriptEngine);

							} catch (final MalformedURLException e) {
								getTest("[EASE Testrunner]").addError(e.getMessage(), fScriptEngine);
							}
						}
					}
				}

			} catch (final InterruptedException e) {
				getTest("[user event]").addError("Test aborted by user", fScriptEngine);

			} finally {
				// testfile teardown
				if ((!hasError()) || ((getTestSuite().getDefinition() != null) && (getTestSuite().getDefinition().getFlag(Flag.RUN_TEARDOWN_ON_ERROR, true))))
					runSetupTeardownCode(ITestSuiteDefinition.CODE_LOCATION_TESTFILE_TEARDOWN);

				// make sure all tests are marked as finished (cleanup for badly written tests from users)
				for (final ITestEntity test : getChildren()) {
					if (TestStatus.RUNNING.equals(test.getStatus()))
						test.setEntityStatus(TestStatus.PASS);
				}

				fScriptEngine.terminate();
				fScriptEngine = null;

				setEntityStatus(TestStatus.FINISHED);
				if (TestStatus.FINISHED.equals(getStatus()))
					setEntityStatus(TestStatus.PASS);
			}

		} else {
			getTest("[Setup]").addError("No engine found for <" + getResource().toString() + ">", null);
		}
	}

	/**
	 * @generated NOT
	 */
	private void runSetupTeardownCode(String codeLocation) {
		final ITestSuiteDefinition definition = getTestSuite().getDefinition();
		if (definition != null) {
			final ICode customCode = definition.getCustomCode(codeLocation);
			if ((customCode != null) && (!customCode.getContent().trim().isEmpty())) {
				if (fScriptEngine != null) {
					ScriptResult result;
					try {
						result = fScriptEngine.executeSync(new Script(customCode.getLocation(), customCode.getContent()));
					} catch (final InterruptedException e) {
						getTest("[user event]").addError("Aborted by user", fScriptEngine);
						return;
					}
					if (result.hasException()) {
						getTest("[" + codeLocation + "]").addError(result.getException().getMessage(), fScriptEngine);
						return;
					}
				} else {
					getTest("[" + codeLocation + "]").addError("Could not detect setup/teardown engine", null);
					return;
				}
			}
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated NOT
	 */
	@Override
	public void setTerminated(boolean newTerminated) {
		super.setTerminated(newTerminated);

		if (fScriptEngine != null)
			fScriptEngine.terminate();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated NOT
	 */
	@Override
	public void reset() {
		// remove single tests
		for (final Object child : getChildren().toArray()) {
			if (child instanceof ITestClass)
				getChildren().remove(child);
		}

		super.reset();

		// disable if set in test suite (needs to be done after super.reset() as wew change the status here)
		final ITestSuite testSuite = getTestSuite();
		if (testSuite != null) {
			final ITestSuiteDefinition definition = getTestSuite().getDefinition();
			if (definition != null) {
				final IPath path = getFullPath().makeRelativeTo(getTestSuite().getFullPath());
				if (definition.getDisabledResources().contains(path.makeAbsolute())) {
					// test is disabled
					setDisabled("File disabled in testsuite");
				}
			}
		}
	}

	/**
	 * @generated NOT
	 */
	@Override
	public long getEstimatedDuration() {
		return (TestStatus.DISABLED.equals(getStatus())) ? 0 : super.getEstimatedDuration();
	}
} // TestFile
