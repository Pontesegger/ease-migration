/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.documentation.linkcreators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModuleLinkCreatorTest {

	private ILinkCreator fLinkCreator;

	@BeforeEach
	public void beforeEach() {
		fLinkCreator = new ModuleLinkCreator();
	}

	@Test
	@DisplayName("createLink() throws for @link references")
	public void createThrowsForModuleReferences() {
		assertThrows(IOException.class, () -> fLinkCreator.createLink("{@link #someMethod()}"));
	}

	@Test
	@DisplayName("createLink() throws for invalid references")
	public void createThrowsForInvalidReferences() {
		assertThrows(IOException.class, () -> fLinkCreator.createLink("{@module-not-available}"));
	}

	@Test
	@DisplayName("createLink() for {@module #methodName()}")
	public void createLinkForMethodWithoutParameters() throws IOException {
		assertEquals("<a href='#methodName'>methodName()</a>", fLinkCreator.createLink("{@module #methodName()}"));
	}

	@Test
	@DisplayName("createLink() for {@module #methodName(int, String)}")
	public void createLinkForMethodWithParameters() throws IOException {
		assertEquals("<a href='#methodName'>methodName()</a>", fLinkCreator.createLink("{@module #methodName()}"));
	}

	@Test
	@DisplayName("createLink() for {@module #CONSTANT}")
	public void createLinkForConstant() throws IOException {
		assertEquals("<a href='#CONSTANT'>CONSTANT</a>", fLinkCreator.createLink("{@module #CONSTANT}"));
	}

	@Test
	@DisplayName("createLink() for {@module target.plugin.moduleId}")
	public void createLinkForRemoteModule() throws IOException {
		assertEquals("<a href='../../target.plugin/help/module_target.plugin.moduleid.html'>moduleId</a>",
				fLinkCreator.createLink("{@module target.plugin.moduleId}"));
	}

	@Test
	@DisplayName("createLink() for {@module target.plugin.moduleId#methodName()}")
	public void createLinkForRemoteMethodWithoutParameters() throws IOException {
		assertEquals("<a href='../../target.plugin/help/module_target.plugin.moduleid.html#methodName'>moduleId.methodName()</a>",
				fLinkCreator.createLink("{@module target.plugin.moduleId#methodName()}"));
	}

	@Test
	@DisplayName("createLink() for {@module target.plugin.moduleId#methodName(int, String)}")
	public void createLinkForRemoteMethodWithParameters() throws IOException {
		assertEquals("<a href='../../target.plugin/help/module_target.plugin.moduleid.html#methodName'>moduleId.methodName()</a>",
				fLinkCreator.createLink("{@module target.plugin.moduleId#methodName(int, String)}"));
	}

	@Test
	@DisplayName("createLink() for {@module target.plugin.moduleId#CONSTANT}")
	public void createLinkForRemoteConstant() throws IOException {
		assertEquals("<a href='../../target.plugin/help/module_target.plugin.moduleid.html#CONSTANT'>moduleId.CONSTANT</a>",
				fLinkCreator.createLink("{@module target.plugin.moduleId#CONSTANT}"));
	}
}
