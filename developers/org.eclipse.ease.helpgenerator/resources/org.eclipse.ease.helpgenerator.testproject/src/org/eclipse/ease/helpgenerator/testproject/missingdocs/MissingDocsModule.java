package org.eclipse.ease.helpgenerator.testproject.missingdocs;

import java.io.IOException;

import org.eclipse.ease.modules.WrapToScript;

public class MissingDocsModule {

	@WrapToScript
	public static final int UNIVERSAL_TRUTH = 42;
	
	@WrapToScript
	public void simple() {
	}

	/**
	 * Method with parameters.
	 * 
	 * @param a
	 *            integer parameter
	 * @return always 0
	 */
	@WrapToScript
	public int baseParameters(int a, long b, String data) {
		return 0;
	}

	/**
	 * Method with method name alias.
	 */
	@WrapToScript(alias = "shortName")
	public String thisIsAMethodWithALongName() {
		return null;
	}
	
	/**
	 * Method that always throws.
	 */
	@WrapToScript
	public void pleaseThrow() throws IOException {
		throw new IOException("Bad thing happened");
	}
}
