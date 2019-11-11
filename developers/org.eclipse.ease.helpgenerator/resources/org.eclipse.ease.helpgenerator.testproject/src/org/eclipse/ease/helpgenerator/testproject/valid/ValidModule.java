package org.eclipse.ease.helpgenerator.testproject.valid;

import java.io.IOException;
import java.util.function.Function;

import org.eclipse.ease.modules.WrapToScript;

/**
 * This is a test module.
 */
public class ValidModule {

	/** The answer to all your questions. */
	@WrapToScript
	public static final int UNIVERSAL_TRUTH = 42;
	
	/**
	 * Simple method documentation.
	 */
	@WrapToScript
	public void simple() {
	}

	/**
	 * Method with parameters.
	 * 
	 * @param a
	 *            integer parameter
	 * @param b
	 *            parameter of type long
	 * @param data
	 *            java string
	 * @return always 0
	 */
	@WrapToScript
	public int baseParameters(int a, long b, String data) {
		return 0;
	}

	/**
	 * Method with method name alias.
	 * 
	 * @return always <code>null</code>
	 */
	@WrapToScript(alias = "shortName")
	public String thisIsAMethodWithALongName() {
		return null;
	}
	
	/**
	 * Method that always throws.
	 * @throws IOException in any case
	 */
	@WrapToScript
	public void pleaseThrow() throws IOException {
		throw new IOException("Bad thing happened");
	}

	/**
	 * Simple method that converts a long to string.
	 *
	 * @param myLongNumber
	 *            A function to execute and whose return value will be returned
	 * @return result of the function
	 */
	@WrapToScript
	public String thisMethodHasNoParameters(long myLongNumber) {
		return String.valueOf(myLongNumber);
	}

	/**
	 * Method that uses generic parameters. For a simpler case, use {@link #thisMethodHasNoParameters(long)}, just to link to it from this javadoc.
	 *
	 * @param myFunctionParameter
	 *            A function to execute and whose return value will be returned
	 * @return result of the function
	 */
	@WrapToScript
	public String thisMethodHasGenericParameters(Function<Long, String> myFunctionParameter) {
		return myFunctionParameter.apply(100L);
	}
}
