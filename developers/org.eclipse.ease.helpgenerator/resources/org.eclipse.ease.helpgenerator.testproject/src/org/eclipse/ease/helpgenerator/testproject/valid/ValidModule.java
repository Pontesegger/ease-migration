package org.eclipse.ease.helpgenerator.testproject.valid;

import java.io.IOException;
import java.util.function.Function;

import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;

/**
 * This is a test module.
 */
public class ValidModule extends AbstractClassA implements InterfaceB {

	/**
	 * The answer to all your questions. This is really it.
	 */
	@WrapToScript
	public static final int UNIVERSAL_TRUTH = 42;

	/**
	 * Some old stuff.
	 * 
	 * @deprecated we do not need this anymore
	 */
	@Deprecated
	@WrapToScript
	public static final String OLD_VALUE = "not needed anymore";

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
	 * Method with method name alias. The documentation to this method is long to make sure it is handled with a line break in the source file. Further it
	 * contains some valid HTML tags that need to be processed correctly by the doclet.
	 * <p>
	 * This is a separate paragraph.
	 * </p>
	 * 
	 * @return always <code>null</code>
	 */
	@WrapToScript(alias = "shortName")
	public String thisIsAMethodWithALongName() {
		return null;
	}

	/**
	 * Method that always throws.
	 * 
	 * @throws IOException
	 *             in any case
	 */
	@WrapToScript
	public void pleaseThrow() throws IOException {
		throw new IOException("Bad thing happened");
	}

	/**
	 * Method with optional parameters. Check out {@module #baseParameters(int, long, String)}
	 *
	 * @param mandatory
	 *            Mandatory parameter,
	 * @param optionalDefaultsTo1
	 *            simple integer parameter
	 * @param optionalDefaultsToNull
	 *            second optional parameter, {@module #UNIVERSAL_TRUTH}
	 * @return result of the function
	 * @scriptExample optionalParameters(new Thread(), 22, "nothing") first way to call this method
	 * @scriptExample optionalParameters(new Thread()) ... using default values for parameter 2 and 3
	 */
	@WrapToScript
	public String optionalParameters(Thread mandatory, @ScriptParameter(defaultValue = "1") int optionalDefaultsTo1,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) String optionalDefaultsToNull) {
		return null;
	}

	/**
	 * Method that uses generic parameters.
	 *
	 * @param myFunctionParameter
	 *            A function to execute and whose return value will be returned
	 * @return result of the function
	 */
	@WrapToScript
	public String thisMethodHasGenericParameters(Function<Long, String> myFunctionParameter) {
		return myFunctionParameter.apply(100L);
	}

	@WrapToScript
	public void methodToBeOverridden() {
	}

	@WrapToScript
	public void abstractMethodToBeOverridden() {
	}

	@WrapToScript
	public void interfaceBMethod() {
	}
	
	/**
	 * This method is outdated.
	 * @deprecated Please use another method. This one will be removed in future versions.
	 */
	@Deprecated
	@WrapToScript
	public void deprecatedMethod() {
	}
}
