/*******************************************************************************
 * Copyright (c) 2016 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.ease.modules.ModuleDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ModuleHelpTest {

	private Object fSampleModule;
	private ModuleDefinition fSampleModuleDefinition;
	private Method fSampleMethod;
	private Field fSampleField;

	@BeforeEach
	public void setUp() throws Exception {
		fSampleModule = new org.eclipse.ease.ui.help.hovers.SampleModule();
		fSampleModuleDefinition = ModuleDefinition.getDefinition(fSampleModule);

		fSampleMethod = fSampleModule.getClass().getMethod("sum", double.class, double.class);
		fSampleField = fSampleModule.getClass().getField("PI");
	}

	@Disabled
	@Test
	public void getModuleHelpLocation() {
		assertNotNull(ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition));
	}

	@Disabled
	@Test
	public void getModuleHelp() throws Exception {
		final URL helpLocation = ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition);

		final ModuleHelp moduleHelp = new ModuleHelp(helpLocation);
		assertNotNull(moduleHelp);

		assertEquals("Sample Module", moduleHelp.getName());
		assertEquals("Module only used for unit testing.", moduleHelp.getDescription());

		assertTrue(moduleHelp.getHoverContent().length() > moduleHelp.getDescription().length());
	}

	@Disabled
	@Test
	public void getMethodHelp() throws Exception {
		final URL helpLocation = ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition);

		final ModuleHelp moduleHelp = new ModuleHelp(helpLocation);

		final IHoverHelp methodHelp = moduleHelp.getMethodHelp(fSampleMethod);

		assertEquals("sum", methodHelp.getName());
		assertEquals("Provide sum of 2 variables.", methodHelp.getDescription());

		assertTrue(moduleHelp.getHoverContent().length() > methodHelp.getDescription().length());
	}

	@Disabled
	@Test
	public void getConstantHelp() throws Exception {
		final URL helpLocation = ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition);

		final ModuleHelp moduleHelp = new ModuleHelp(helpLocation);

		final IHoverHelp constantHelp = moduleHelp.getConstantHelp(fSampleField);

		assertEquals("PI", constantHelp.getName());
		assertEquals("PI constant.", constantHelp.getDescription());

		assertTrue(moduleHelp.getHoverContent().length() > constantHelp.getDescription().length());
	}
}
