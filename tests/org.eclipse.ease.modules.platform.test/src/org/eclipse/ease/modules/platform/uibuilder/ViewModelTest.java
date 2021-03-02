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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ViewModelTest {

	private static IRenderer fRenderer;

	private static class TextPlaceHolder implements IPlaceHolder {
		private final Point fPosition;

		public TextPlaceHolder(Point position) {
			fPosition = position;
		}

		public TextPlaceHolder(int column, int row) {
			this(new Point(column, row));
		}

		@Override
		public String toString() {
			return fPosition.x + "/" + fPosition.y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((fPosition == null) ? 0 : fPosition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final TextPlaceHolder other = (TextPlaceHolder) obj;
			if (fPosition == null) {
				if (other.fPosition != null)
					return false;
			} else if (!fPosition.equals(other.fPosition))
				return false;
			return true;
		}
	}

	@BeforeAll
	public static void classSetup() {
		fRenderer = new IRenderer() {

			@Override
			public void setViewModel(ViewModel viewModel) {
			}

			@Override
			public IPlaceHolder createPlaceHolder(Point position) {
				return new TextPlaceHolder(position);
			}
		};
	}

	@Test
	public void emptyModel() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		assertTrue(viewModel.getElements().isEmpty());
		assertEquals(0, viewModel.getColumnCount());
		assertEquals(0, viewModel.getRowCount());
	}

	@Test
	public void singleElementOn1x1() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.insertElement("Element1x1", new Location("1/1"));
		assertEquals(1, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1" }, viewModel.getElements().toArray());

		assertEquals(1, viewModel.getColumnCount());
		assertEquals(1, viewModel.getRowCount());
	}

	@Test
	public void singleColumn1x3() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.insertElement("Element1x1", new Location(""));
		viewModel.insertElement("Element1x2", new Location(""));
		viewModel.insertElement("Element1x3", new Location(""));
		assertEquals(3, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1", "Element1x2", "Element1x3" }, viewModel.getElements().toArray());

		assertEquals(1, viewModel.getColumnCount());
		assertEquals(3, viewModel.getRowCount());
	}

	@Test
	public void singleRow3x1() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(3);
		viewModel.insertElement("Element1x1", new Location(""));
		viewModel.insertElement("Element2x1", new Location(""));
		viewModel.insertElement("Element3x1", new Location(""));
		assertEquals(3, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1", "Element2x1", "Element3x1" }, viewModel.getElements().toArray());

		assertEquals(3, viewModel.getColumnCount());
		assertEquals(1, viewModel.getRowCount());
	}

	@Test
	public void insertionGaps() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(2);
		viewModel.insertElement("Element2x3", new Location("2/3"));

		assertEquals(6, viewModel.getElements().size());
		assertArrayEquals(new Object[] { new TextPlaceHolder(1, 1), new TextPlaceHolder(2, 1), new TextPlaceHolder(1, 2), new TextPlaceHolder(2, 2),
				new TextPlaceHolder(1, 3), "Element2x3" }, viewModel.getElements().toArray());

		assertEquals(2, viewModel.getColumnCount());
		assertEquals(3, viewModel.getRowCount());
	}

	@Test
	public void insertRandomOrder() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(3);
		viewModel.insertElement("Element2x2", new Location("2/2"));
		viewModel.insertElement("Element1x1", new Location("1/1"));
		viewModel.insertElement("Element3x1", new Location("3/1"));
		viewModel.insertElement("Element1x2", new Location("1/2"));
		viewModel.insertElement("Element3x2", new Location("3/2"));
		viewModel.insertElement("Element2x1", new Location("2/1"));

		assertEquals(6, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1", "Element2x1", "Element3x1", "Element1x2", "Element2x2", "Element3x2" },
				viewModel.getElements().toArray());

		assertEquals(3, viewModel.getColumnCount());
		assertEquals(2, viewModel.getRowCount());
	}

	@Test
	public void replaceElement() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.insertElement("Element1x1", new Location("1/1"));
		viewModel.insertElement("Replacement", new Location("1/1"));

		assertEquals(1, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Replacement" }, viewModel.getElements().toArray());

		assertEquals(1, viewModel.getColumnCount());
		assertEquals(1, viewModel.getRowCount());
	}

	@Test
	public void extendColumns() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(2);
		viewModel.insertElement("Element1x1", new Location("1/1"));
		viewModel.insertElement("Element2x1", new Location("2/1"));
		viewModel.insertElement("Element1x2", new Location("1/2"));
		viewModel.insertElement("Element2x2", new Location("2/2"));

		viewModel.setColumnCount(3);

		assertEquals(6, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1", "Element2x1", new TextPlaceHolder(3, 1), "Element1x2", "Element2x2", new TextPlaceHolder(3, 2) },
				viewModel.getElements().toArray());

		assertEquals(3, viewModel.getColumnCount());
		assertEquals(2, viewModel.getRowCount());
	}

	@Test
	public void horizontalSpanElement() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(3);
		viewModel.insertElement("Element1x1", new Location("1/1"));
		viewModel.insertElement("Element2-3x1", new Location("2-3/1"));
		viewModel.insertElement("Element1x2", new Location("1/2"));
		viewModel.insertElement("Element2x2", new Location("2/2"));
		viewModel.insertElement("Element3x2", new Location("3/2"));

		assertEquals(6, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1", "Element2-3x1", new Reference("Element2-3x1"), "Element1x2", "Element2x2", "Element3x2" },
				viewModel.getElements().toArray());

		assertEquals(3, viewModel.getColumnCount());
		assertEquals(2, viewModel.getRowCount());
	}

	@Test
	public void verticalSpanElement() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(2);
		viewModel.insertElement("Element1x1", new Location("1/1"));
		viewModel.insertElement("Element2x1", new Location("2/1"));
		viewModel.insertElement("Element1x2-3", new Location("1/2-3"));
		viewModel.insertElement("Element2x2", new Location("2/2"));
		viewModel.insertElement("Element2x3", new Location("2/3"));

		assertEquals(6, viewModel.getElements().size());
		assertArrayEquals(new Object[] { "Element1x1", "Element2x1", "Element1x2-3", "Element2x2", new Reference("Element1x2-3"), "Element2x3" },
				viewModel.getElements().toArray());

		assertEquals(2, viewModel.getColumnCount());
		assertEquals(3, viewModel.getRowCount());
	}

	@Test
	public void replaceSpanElement() {
		final ViewModel viewModel = new ViewModel(fRenderer);
		viewModel.setColumnCount(3);
		viewModel.insertElement("Element1x1", new Location("1/1"));
		viewModel.insertElement("Element2x1", new Location("2/1"));
		viewModel.insertElement("Element3x1", new Location("3/1"));
		viewModel.insertElement("Element1x2", new Location("1/2"));
		viewModel.insertElement("Element2-3x2-3", new Location("2-3/2-3"));
		viewModel.insertElement("Element1x3", new Location("1/3"));

		viewModel.insertElement("Replacement", new Location("3-4/2"));

		assertEquals(12, viewModel.getElements().size());
		assertArrayEquals(
				new Object[] { "Element1x1", "Element2x1", "Element3x1", new TextPlaceHolder(4, 1), "Element1x2", new TextPlaceHolder(2, 2), "Replacement",
						new Reference("Replacement"), "Element1x3", new TextPlaceHolder(2, 3), new TextPlaceHolder(3, 3), new TextPlaceHolder(4, 3) },
				viewModel.getElements().toArray());

		assertEquals(4, viewModel.getColumnCount());
		assertEquals(3, viewModel.getRowCount());
	}
}
