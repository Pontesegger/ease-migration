package org.eclipse.ease.helpgenerator.testproject.valid;

import org.eclipse.ease.modules.WrapToScript;

/**
 * This is a test module.
 */
public abstract class AbstractClassA implements InterfaceA {

	/** Constant defined in the base class. */
	@WrapToScript
	public static final String DEFINED_IN_BASE_CLASS = "1";

	/**
	 * This method is only defined in the base class.
	 * 
	 * @return nothing of importance
	 */
	@WrapToScript
	public String methodFromBaseClass() {
		return null;
	}

	/**
	 * Method with documentation in base class. Method body is implemented in the derived class.
	 */
	@WrapToScript
	public void methodToBeOverridden() {
	}

	/**
	 * Abstract method with documentation in base class.
	 */
	@WrapToScript
	public abstract void abstractMethodToBeOverridden();

	@WrapToScript
	public void interfaceAMethod() {
	}
}
