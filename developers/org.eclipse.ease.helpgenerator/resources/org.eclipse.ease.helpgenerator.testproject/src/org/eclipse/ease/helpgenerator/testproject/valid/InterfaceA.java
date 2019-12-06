package org.eclipse.ease.helpgenerator.testproject.valid;

import org.eclipse.ease.modules.WrapToScript;

public interface InterfaceA {
	
	/** Constant defined in InterfaceA. */ 
	@WrapToScript
	static int INTERFACE_CONSTANT = 22;
	
	/**
	 * Method documented in InterfaceA only.
	 */
	void interfaceAMethod();
}
