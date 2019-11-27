package org.eclipse.ease.helpgenerator.testproject.valid;

import java.io.IOException;

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
}
