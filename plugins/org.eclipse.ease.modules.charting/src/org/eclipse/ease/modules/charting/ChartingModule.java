/*******************************************************************************
 * Copyright (c) 2015 Domjan Sansovic and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Domjan Sansovic - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.charting;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.charting.views.Chart;
import org.eclipse.ease.modules.charting.views.ChartView;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation.CursorLineStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Provide functions to display and populate line charts.
 */
public class ChartingModule extends AbstractScriptModule {
	public static final String MODULE_NAME = "Charting";

	private static int fFigureIterator = 1;

	private Chart fChart = null;

	private XYGraph fXYGraph = null;

	private Trace fCurrentPlot = null;

	private final Collection<Annotation> fUserMarkers = new HashSet<>();

	/**
	 * Opens a new view with an empty figure. <i>figureId</i> is used as a view and chart title. If a view with the same <i>figureId</i> already exists, it will
	 * be activated. The last activated figure will be used for all further commands of this modules.
	 *
	 * @param figureId
	 *            name of the figure to be created
	 * @return new or activated chart
	 * @throws Throwable
	 *             when the view could not be initialized
	 */
	@WrapToScript
	public Chart figure(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String figureId) throws Throwable {
		fChart = null;
		final String secondaryId = (figureId == null) ? "Figure " + Integer.toString(fFigureIterator++) : figureId;
		final ChartView view = (ChartView) UIModule.showView(ChartView.VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		view.setViewName(secondaryId);
		fChart = view.getChart();
		fXYGraph = fChart.setPlotTitle(secondaryId);
		return fChart;
	}

	/**
	 * Create a new chart on a given parent composite.
	 *
	 * @param parent
	 *            composite to create chart into
	 * @param figureId
	 *            name of the figure to be created
	 * @return new chart instance
	 */
	@WrapToScript
	public Chart createChart(Composite parent, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String figureId) {
		final String secondaryId = (figureId == null) ? "Figure " + Integer.toString(fFigureIterator++) : figureId;

		Display.getDefault().syncExec(() -> {
			fChart = new Chart(parent, SWT.NONE);
			fXYGraph = fChart.setPlotTitle(secondaryId);
		});

		return fChart;
	}

	/**
	 * Series will be created with the name seriesName. If the name is not given then Series will have the name "Series id" where id is the current number of
	 * the Series Iterator. Series Iterator start at 1 and with every new Series is incremented by one. If Series is already created with seriesName then that
	 * series will be set as currently active Series and on that Series methods will be performed. If there is no active Figure then Figure will be created and
	 * activated.
	 *
	 * @param seriesName
	 *            Name of this series. Default is <code>null</code> if no seriesName is given, in that case the name is "Series id" as explained above
	 * @param format
	 *            default is "", if number is written inside format like "f#25" point size will be set(in this case 25), please write format correctly, if for
	 *            example 2 colors will be written like "rg" then the last one will be taken, in this case g or green, so please set line style, point size,
	 *            color and Marker Type only once. Used matlab syntax to define plot format:
	 *            <table>
	 *            <tr>
	 *            <th>Specifier</th>
	 *            <th>LineStyle</th>
	 *            </tr>
	 *            <tr>
	 *            <td>'-'</td>
	 *            <td>Solid line (default)</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'--'</td>
	 *            <td>Dashed line</td>
	 *            </tr>
	 *            <tr>
	 *            <td>':'</td>
	 *            <td>Dotted line</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'-.'</td>
	 *            <td>Dash-dot line</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'#'</td>
	 *            <td>No line</td>
	 *            </tr>
	 *            </table>
	 *
	 *            <table>
	 *            <tr>
	 *            <th>Specifier</th>
	 *            <th>Color</th>
	 *            </tr>
	 *            <tr>
	 *            <td>r</td>
	 *            <td>Red</td>
	 *            </tr>
	 *            <tr>
	 *            <td>g</td>
	 *            <td>Green</td>
	 *            </tr>
	 *            <tr>
	 *            <td>b</td>
	 *            <td>Blue</td>
	 *            </tr>
	 *            <tr>
	 *            <td>c</td>
	 *            <td>Cyan</td>
	 *            </tr>
	 *            <tr>
	 *            <td>m</td>
	 *            <td>Magenta</td>
	 *            </tr>
	 *            <tr>
	 *            <td>y</td>
	 *            <td>Yellow</td>
	 *            </tr>
	 *            <tr>
	 *            <td>k</td>
	 *            <td>Black</td>
	 *            </tr>
	 *            <tr>
	 *            <td>w</td>
	 *            <td>White</td>
	 *            </tr>
	 *            </table>
	 *
	 *            <table>
	 *            <tr>
	 *            <th>Specifier</th>
	 *            <th>Marker Type</th>
	 *            </tr>
	 *            <tr>
	 *            <td>'+'</td>
	 *            <td>Plus sign</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'o'</td>
	 *            <td>Circle</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'p'</td>
	 *            <td>Point</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'x'</td>
	 *            <td>Cross</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'s'</td>
	 *            <td>Square</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'f'</td>
	 *            <td>Filled Square</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'d'</td>
	 *            <td>Diamond</td>
	 *            </tr>
	 *            <tr>
	 *            <td>'v'</td>
	 *            <td>Downward-pointing triangle</td>
	 *            </tr>
	 *            </table>
	 *
	 * @return series as Trace type to set different properties for this series
	 * @throws Throwable
	 *             if the series could not be initialized
	 */
	@WrapToScript
	public Trace series(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String seriesName, @ScriptParameter(defaultValue = "") final String format)
			throws Throwable {
		return getChart().series(seriesName, format);
	}

	private Chart getChart() throws Throwable {
		if (fChart == null)
			figure(null);

		return fChart;
	}

	/**
	 * Add (x,y) point to the last Series that is set with method series(seriesName,format). If there is no active figure and series then both will be created
	 * and activated.
	 *
	 * @param x
	 *            x coordinate of this point
	 * @param y
	 *            y coordinate of this point
	 * @return series as Trace type to set different properties for this series
	 * @throws Throwable
	 *             when the view could not be initialized
	 */
	@WrapToScript
	public Trace plotPoint(final double x, final double y) throws Throwable {
		fCurrentPlot = getChart().plot(x, y);
		return fCurrentPlot;
	}

	/**
	 * Plot array of points (x[],y[]) on the last series that is set with method series(seriesName, format). If there is no active Figure and Series then both
	 * will be created and activated.
	 *
	 * @param x
	 *            array of x coordinates
	 * @param y
	 *            array of y coordinates
	 * @return series as Trace type to set different properties for this series
	 * @throws Throwable
	 *             when the view could not be initialized
	 */
	@WrapToScript
	public Trace plot(final double[] x, final double[] y) throws Throwable {
		fCurrentPlot = getChart().plot(x, y);
		return fCurrentPlot;
	}

	/**
	 * Sets the title of the active chart.
	 *
	 * @param chartTitle
	 *            title to be set
	 * @return {@link XYGraph} object or <code>null</code> if there is no active graph
	 */
	@WrapToScript(alias = "title")
	public XYGraph setPlotTitle(final String chartTitle) {
		if (fChart != null)
			return fChart.setPlotTitle(chartTitle);

		return null;
	}

	/**
	 * Set X axis name.
	 *
	 * @param label
	 *            label to be used for X axis
	 * @return {@link Axis} object or <code>null</code> if there is no active graph
	 */
	@WrapToScript(alias = "xlabel")
	public Axis setXLabel(final String label) {
		if (fChart != null)
			return fChart.setXLabel(label);

		return null;
	}

	/**
	 * Set Y axis name.
	 *
	 * @param label
	 *            label to be used for Y axis
	 * @return {@link Axis} object or <code>null</code> if there is no active graph
	 */
	@WrapToScript(alias = "ylabel")
	public Axis setYLabel(final String label) {
		if (fChart != null)
			return fChart.setYLabel(label);

		return null;
	}

	/**
	 * Set lower and upper limit of the X axis and the Y axis.
	 *
	 * @param xRange
	 *            Range from x Axis to be set, format is [xmin, xmax]
	 * @param yRange
	 *            Range from Y Axis to be set, format is [ymin, ymax]
	 */
	@WrapToScript(alias = "axis")
	public void setAxisRange(final double[] xRange, final double[] yRange) {
		if (fChart != null)
			fChart.setAxisRange(xRange, yRange);
	}

	/**
	 * Activates or deactivates drawing of a background grid.
	 *
	 * @param showGrid
	 *            if <code>true</code> a grid will be displayed
	 */
	@WrapToScript
	public void showGrid(final boolean showGrid) {
		if (fChart != null)
			fChart.showGrid(showGrid);
	}

	/**
	 * Activates or deactivates auto scaling whenever the cart is updated. An auto scale may also be triggered by double clicking right into the chart area.
	 *
	 * @param performAutoScale
	 *            if <code>true</code> auto scaling will be enabled
	 */
	@WrapToScript
	public void setAutoScale(final boolean performAutoScale) {
		if (fChart != null)
			fChart.setAutoScale(performAutoScale);
	}

	/**
	 * Clears the active chart.
	 */
	@WrapToScript
	public void clear() {
		Display.getDefault().syncExec(() -> {
			if (fXYGraph != null) {
				for (final Annotation marker : fUserMarkers)
					fXYGraph.removeAnnotation(marker);
				// fXYGraph.removeAll();
			}
		});

		if (fChart != null)
			fChart.clear();

		fCurrentPlot = null;
	}

	/**
	 * Export the current figure as png file. When no <i>location</i> is provided, a popup dialog will ask the user for the target location.
	 *
	 * @param location
	 *            where to store image to. Accepts strings, {@link IFile} and {@link File} instances
	 * @param overwrite
	 *            Overwrite flag, if <code>true</code> file will be overwritten without question
	 * @throws Throwable
	 *             when the view could not be initialized
	 */
	@WrapToScript
	public void exportGraph(@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object location,
			@ScriptParameter(defaultValue = "false") final boolean overwrite) throws Throwable {

		if (fChart != null) {
			final Object resolvedLocation = (location != null) ? ResourceTools.resolve(location, getScriptEngine().getExecutedFile()) : null;
			getChart().export(resolvedLocation, overwrite);
		}
	}

	/**
	 * Deletes a given series. If the series does not exists this method does nothing.
	 *
	 * @param seriesName
	 *            name of series to be deleted
	 * @throws Throwable
	 *             when the view could not be initialized
	 */
	@WrapToScript
	public void removeSeries(final String seriesName) throws Throwable {
		getChart().removeSeries(seriesName);
	}

	/**
	 * Add a marker to the chart. A marker is a text label that points to a grid location. By default the marker will point to the data point that was added
	 * last. If positions are provided always <i>xPosition</i> and <i>yPosition</i> need to be provided. The returned marker can be further customized in the UI
	 * thread.
	 *
	 * @param text
	 *            text to display
	 * @param xPosition
	 *            if set, this will be the X position the marker points to
	 * @param yPosition
	 *            if set, this will be the Y position the marker points to
	 * @return marker implementation
	 */
	@WrapToScript
	public Annotation addMarker(String text, @ScriptParameter(defaultValue = ScriptParameter.NULL) Double xPosition,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) Double yPosition) {

		if (fCurrentPlot != null) {
			final Annotation annotation = new Annotation(text, fCurrentPlot);
			annotation.setEnabled(true);
			annotation.setCursorLineStyle(CursorLineStyle.NONE);

			Display.getDefault().asyncExec(() -> {
				fXYGraph.addAnnotation(annotation);

				// we need to schedule a fresh UI Job as it seems XY Chart needs to render the annotation first. Only after that it can be moved.
				Display.getDefault().asyncExec(() -> {
					if ((xPosition != null) && (yPosition != null))
						annotation.setValues(xPosition, yPosition);
				});
			});

			fUserMarkers.add(annotation);

			return annotation;
		}

		return null;
	}
}
