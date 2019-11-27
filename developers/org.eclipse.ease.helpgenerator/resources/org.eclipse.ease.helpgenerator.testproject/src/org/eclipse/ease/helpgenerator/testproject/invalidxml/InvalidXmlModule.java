package org.eclipse.ease.helpgenerator.testproject.invalidxml;

import org.eclipse.ease.modules.WrapToScript;

/**
 * This module contains <some> invalid patterns.
 */
public class InvalidXmlModule {
	
	/**
	 * The XML code contains <i> some unnclosed tags.
	 */
	@WrapToScript
	public void simple() {
	}
}
