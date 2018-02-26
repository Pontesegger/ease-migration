/*******************************************************************************
 * Copyright (c) 2016 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.ease.modules.ModuleDefinition;
import org.junit.Before;
import org.junit.Test;

public class ModuleHelpTest {

	private Object fSampleModule;
	private ModuleDefinition fSampleModuleDefinition;
	private Method fSampleMethod;
	private Field fSampleField;

	@Before
	public void setUp() throws Exception {
		fSampleModule = new org.eclipse.ease.ui.help.hovers.SampleModule();
		fSampleModuleDefinition = ModuleDefinition.getDefinition(fSampleModule);

		fSampleMethod = fSampleModule.getClass().getMethod("sum", double.class, double.class);
		fSampleField = fSampleModule.getClass().getField("PI");
	}

	@Test
	public void getModuleHelpLocation() {
		assertNotNull(ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition));
	}

	@Test
	public void getModuleHelp() throws Exception {
		final URL helpLocation = ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition);

		final ModuleHelp moduleHelp = new ModuleHelp(helpLocation);
		assertNotNull(moduleHelp);

		assertEquals("Sample Module", moduleHelp.getName());
		assertEquals("Module only used for unit testing.", moduleHelp.getDescription());

		assertTrue(moduleHelp.getHoverContent().length() > moduleHelp.getDescription().length());
	}

	@Test
	public void getMethodHelp() throws Exception {
		final URL helpLocation = ModuleHelp.getModuleHelpLocation(fSampleModuleDefinition);

		final ModuleHelp moduleHelp = new ModuleHelp(helpLocation);

		final IHoverHelp methodHelp = moduleHelp.getMethodHelp(fSampleMethod);

		assertEquals("sum", methodHelp.getName());
		assertEquals("Provide sum of 2 variables.", methodHelp.getDescription());

		assertTrue(moduleHelp.getHoverContent().length() > methodHelp.getDescription().length());
	}

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
