/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform.uibuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.junit.Test;

public class LocationTest {

	@Test
	public void nullLocation() {
		final Location location = new Location((String) null);
		assertEquals(Location.DYNAMIC_POSITION, location.getPosition().x);
		assertEquals(Location.DYNAMIC_POSITION, location.getPosition().y);

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(1, location.getLayoutData().horizontalSpan);
		assertFalse(location.getLayoutData().grabExcessHorizontalSpace);

		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertEquals(1, location.getLayoutData().verticalSpan);
		assertFalse(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test
	public void emptyLocation() {
		final Location location = new Location("");
		assertEquals(Location.DYNAMIC_POSITION, location.getPosition().x);
		assertEquals(Location.DYNAMIC_POSITION, location.getPosition().y);

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(1, location.getLayoutData().horizontalSpan);
		assertFalse(location.getLayoutData().grabExcessHorizontalSpace);

		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertEquals(1, location.getLayoutData().verticalSpan);
		assertFalse(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test
	public void bounds1x1() {
		final Location location = new Location("1/1");
		assertEquals(1, location.getPosition().x);
		assertEquals(1, location.getPosition().y);

		assertEquals(1, location.getLayoutData().horizontalSpan);
		assertEquals(1, location.getLayoutData().verticalSpan);
	}

	@Test
	public void bounds2x1() {
		final Location location = new Location("2/1");
		assertEquals(2, location.getPosition().x);
		assertEquals(1, location.getPosition().y);

		assertEquals(1, location.getLayoutData().horizontalSpan);
		assertEquals(1, location.getLayoutData().verticalSpan);
	}

	@Test
	public void bounds1x2() {
		final Location location = new Location("1/2");
		assertEquals(1, location.getPosition().x);
		assertEquals(2, location.getPosition().y);

		assertEquals(1, location.getLayoutData().horizontalSpan);
		assertEquals(1, location.getLayoutData().verticalSpan);
	}

	@Test
	public void boundsSpan() {
		final Location location = new Location("3-12/4-7");
		assertEquals(3, location.getPosition().x);
		assertEquals(4, location.getPosition().y);

		assertEquals(10, location.getLayoutData().horizontalSpan);
		assertEquals(4, location.getLayoutData().verticalSpan);
	}

	@Test
	public void horizontalLayoutLeft() {
		final Location location = new Location("<");

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessHorizontalSpace);
	}

	@Test
	public void horizontalLayoutCenter() {
		final Location location = new Location("x");

		assertEquals(SWT.CENTER, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessHorizontalSpace);
	}

	@Test
	public void horizontalLayoutRight() {
		final Location location = new Location(">");

		assertEquals(SWT.RIGHT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessHorizontalSpace);
	}

	@Test
	public void horizontalLayoutFill() {
		final Location location = new Location("o");

		assertEquals(SWT.FILL, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessHorizontalSpace);
	}

	@Test
	public void horizontalLayoutGrab() {
		final Location location = new Location("o!");

		assertEquals(SWT.FILL, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertTrue(location.getLayoutData().grabExcessHorizontalSpace);
	}

	@Test
	public void verticalLayoutTop() {
		final Location location = new Location("^");

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.TOP, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test
	public void verticalLayoutMiddle() {
		final Location location = new Location("< x");

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.CENTER, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test
	public void verticalLayoutBottom() {
		final Location location = new Location("v");

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.BOTTOM, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test
	public void verticalLayoutFill() {
		final Location location = new Location("< o");

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.FILL, location.getLayoutData().verticalAlignment);
		assertFalse(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test
	public void verticalLayoutGrab() {
		final Location location = new Location("< o!");

		assertEquals(SWT.LEFT, location.getLayoutData().horizontalAlignment);
		assertEquals(SWT.FILL, location.getLayoutData().verticalAlignment);
		assertTrue(location.getLayoutData().grabExcessVerticalSpace);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidColumn() {
		new Location("0");
	}
}
