package org.eclipse.ease.helpgenerator.testproject.valid;

import org.eclipse.ease.modules.WrapToScript;

/**
 * This is a deprecated module.
 * @deprecated dead class, do not use
 */
@Deprecated
public class DeprecatedModule {

	/**
	 * The answer to all your questions. This is really it.
	 */
	@WrapToScript
	public static final int UNIVERSAL_TRUTH = 42;

	/**
	 * Simple method documentation.
	 */
	@WrapToScript
	public void simple() {
	}
}
