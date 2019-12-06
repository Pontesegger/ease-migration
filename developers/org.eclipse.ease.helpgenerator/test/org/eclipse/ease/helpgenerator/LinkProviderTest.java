/*******************************************************************************
 * Copyright (c) 2019 ponteseg and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ponteseg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;

import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.junit.Before;
import org.junit.Test;

public class LinkProviderTest {

	private static final String ROOT_URI = "http://test.org/api";

	private LinkProvider fLinkProvider;

	@Before
	public void setup() {
		fLinkProvider = new LinkProvider();
		fLinkProvider.registerAddress(ROOT_URI, List.of("java.lang", "java.util", "java.io", "org.test.some"));
		fLinkProvider.setClassModel(new AbstractClassModel() {

			@Override
			public void populateModel() {
			}

			@Override
			public Collection<String> getImportedClasses() {
				return List.of("java.io.IOException", "java.util.Collection");
			}
		});
	}

	@Test
	public void insertLinksOnNull() {
		assertNull(fLinkProvider.insertLinks(null));
	}

	@Test
	public void insertLinksOnStandardText() {
		final String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr";
		assertEquals(text, fLinkProvider.insertLinks(text));
	}

	@Test
	public void resolveQualifiedClassLink() {
		final String input = "{@link java.lang.String}";
		final String expected = "<a href='" + ROOT_URI + "/java/lang/String.html'>String</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveQualifiedClassLinkWithGenerics() {
		final String input = "{@link java.util.List<java.lang.String>}";
		final String expected = "<a href='" + ROOT_URI + "/java/util/List.html'>List</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveImportedClassLink() {
		final String input = "{@link IOException}";
		final String expected = "<a href='" + ROOT_URI + "/java/io/IOException.html'>IOException</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveBaseApiClassLink() {
		final String input = "{@link String}";
		final String expected = "<a href='" + ROOT_URI + "/java/lang/String.html'>String</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void java8ResolveQualifiedMethodLink() {
		fLinkProvider.setApi(LinkProvider.JavaDocApi.JAVA_8);
		final String input = "{@link java.lang.String#charAt(int)}";
		final String expected = "<a href='" + ROOT_URI + "/java/lang/String.html#charAt-int-'>String.charAt(int)</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void java8ResolveQualifiedMethodLinkWithMultipleParameters() {
		final String input = "{@link java.lang.String#getBytes(int, int, byte[], int)}";
		final String expected = "<a href='" + ROOT_URI + "/java/lang/String.html#getBytes-int-int-byte:A-int-'>String.getBytes(int, int, byte[], int)</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void java8ResolveQualifiedMethodLinkWithGenerics() {
		final String input = "{@link java.util.List<java.lang.String>#addAll(int, Collection<java.lang.String>)}";
		final String expected = "<a href='" + ROOT_URI + "/java/util/List.html#addAll-int-java.util.Collection-'>List.addAll(int, Collection)</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void java8ResolveMethodOnImportedClassLink() {
		final String input = "{@link IOException#getStackTrace()}";
		final String expected = "<a href='" + ROOT_URI + "/java/io/IOException.html#getStackTrace--'>IOException.getStackTrace()</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void java8ResolveMethodOnBaseApiClassLink() {
		final String input = "{@link String#chars()}";
		final String expected = "<a href='" + ROOT_URI + "/java/lang/String.html#chars--'>String.chars()</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void java8ResolveMethodInSameClassLink() {
		final String input = "{@link #chars()}";
		final String expected = "<a href='#chars--'>chars()</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveModuleLink() {
		final String input = "{@module org.eclipse.ease.platform.module.UIModule}";
		final String expected = "<a href='../../org.eclipse.ease.platform.module/help/module_org.eclipse.ease.platform.module.uimodule.html'>UIModule module</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveModuleMethodLinkWithParameters() {
		final String input = "{@module org.eclipse.ease.platform.module.UIModule#openView(java.lang.String)}";
		final String expected = "<a href='../../org.eclipse.ease.platform.module/help/module_org.eclipse.ease.platform.module.uimodule.html#openView'>UIModule.openView(String)</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveModuleMethodLinkWithEmptyParameters() {
		final String input = "{@module org.eclipse.ease.platform.module.UIModule#openView()}";
		final String expected = "<a href='../../org.eclipse.ease.platform.module/help/module_org.eclipse.ease.platform.module.uimodule.html#openView'>UIModule.openView()</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveModuleMethodLinkWithoutParameters() {
		final String input = "{@module org.eclipse.ease.platform.module.UIModule#openView}";
		final String expected = "<a href='../../org.eclipse.ease.platform.module/help/module_org.eclipse.ease.platform.module.uimodule.html#openView'>UIModule.openView()</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}

	@Test
	public void resolveModuleMethodLinkInSameDocument() {
		final String input = "{@module #openView}";
		final String expected = "<a href='#openView'>openView()</a>";
		assertEquals(expected, fLinkProvider.insertLinks(input));
	}
}
