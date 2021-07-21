/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.BasicContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AbstractPathCompletionProviderTest {

	@Test
	@DisplayName("getPathsFromElements() is empty for no elements")
	public void getPathsFromElements_is_empty_for_empty_element() {
		assertTrue(new Provider().getPaths().isEmpty());
	}

	@Test
	@DisplayName("getPathsFromElements() is empty for root elements only")
	public void getPathsFromElements_is_empty_for_root_elements_only() {
		assertTrue(new Provider().getPaths("foo", "bar").isEmpty());
	}

	@Test
	@DisplayName("getPathsFromElements() contains parent paths")
	public void getPathsFromElements_contains_parent_paths() {
		final Collection<IPath> paths = new Provider().getPaths("foo/bar", "foo/another", "/2nd/path");

		assertEquals(2, paths.size());
		assertEquals(new Path("/foo"), new ArrayList<>(paths).get(0));
		assertEquals(new Path("/2nd"), new ArrayList<>(paths).get(1));
	}

	@Test
	@DisplayName("getPathsFromElements() contains all hierarchies")
	public void getPathsFromElements_contains_all_hierarchies() {
		final Collection<IPath> paths = new Provider().getPaths("first/second/third/fourth");

		assertEquals(3, paths.size());
		assertEquals(new Path("/first"), new ArrayList<>(paths).get(0));
		assertEquals(new Path("/first/second"), new ArrayList<>(paths).get(1));
		assertEquals(new Path("/first/second/third"), new ArrayList<>(paths).get(2));
	}

	@Test
	@DisplayName("filter() removes nothing if context is empty")
	public void filter_removes_nothing_if_context_is_empty() {

		final Collection<String> elements = new Provider().filter(createContext(""), "a", "b", "c");

		assertEquals(3, elements.size());
		assertEquals("a", new ArrayList<>(elements).get(0));
		assertEquals("b", new ArrayList<>(elements).get(1));
		assertEquals("c", new ArrayList<>(elements).get(2));
	}

	@Test
	@DisplayName("filter() removes mismatches on same level")
	public void filter_removes_mismatches_on_same_level() {

		final Collection<String> elements = new Provider().filter(createContext("a"), "a", "b", "c", "alpha");

		assertEquals(2, elements.size());
		assertEquals("a", new ArrayList<>(elements).get(0));
		assertEquals("alpha", new ArrayList<>(elements).get(1));
	}

	@Test
	@DisplayName("filter('') keeps only 1st level")
	public void filter_keeps_only_1st_level_on_empty_filter() {

		final Collection<String> elements = new Provider().filter(createContext(""), "first/second/third", "foo/bar", "root");

		assertEquals(1, elements.size());
		assertEquals("root", new ArrayList<>(elements).get(0));
	}

	@Test
	@DisplayName("filter() keeps only 1st level on matching filter")
	public void filter_keeps_only_1st_level_on_matching_filter() {

		final Collection<String> elements = new Provider().filter(createContext("r"), "first/second/third", "foo/bar", "root");

		assertEquals(1, elements.size());
		assertEquals("root", new ArrayList<>(elements).get(0));
	}

	@Test
	@DisplayName("filter() keeps only matching level on filter")
	public void filter_keeps_only_lower_level_on_matching_filter() {

		final Collection<String> elements = new Provider().filter(createContext("first/b"), "first", "first/bar", "first/mismatch", "first/bar/another");

		assertEquals(1, elements.size());
		assertEquals("first/bar", new ArrayList<>(elements).get(0));
	}

	@Test
	@DisplayName("filter('foo') matches absolute paths")
	public void filter_matches_relative_paths() {

		final Collection<String> elements = new Provider().filter(createContext("foo"), "/fooBar", "/foo", "/foo/none", "/bar");

		assertEquals(2, elements.size());
		assertEquals("/fooBar", new ArrayList<>(elements).get(0));
		assertEquals("/foo", new ArrayList<>(elements).get(1));
	}

	@Test
	@DisplayName("filter('/first/') matches next level")
	public void filter_matches_next_level() {

		final Collection<String> elements = new Provider().filter(createContext("/first/"), "/first", "/first/second", "/first/second/third");

		assertEquals(1, elements.size());
		assertEquals("/first/second", new ArrayList<>(elements).get(0));
	}

	private ICompletionContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}

	private class Provider extends AbstractPathCompletionProvider {

		public Collection<IPath> getPaths(String... elements) {
			return getPathsFromElements(Arrays.asList(elements));
		}

		public Collection<String> filter(ICompletionContext context, String... elements) {
			return filter(Arrays.asList(elements), context);
		}

		@Override
		protected IPath toPath(Object element) {
			return new Path(String.valueOf(element));
		}

		@Override
		protected void prepareProposals(ICompletionContext context) {
			// nothing to do
		}
	}
}
