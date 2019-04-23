/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform.uibuilder;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

/**
 * Create dynamic views by adding SWT controls via script commands to a dynamic {@link GridLayout}. Each element allow to provide layout data, which accepts a
 * fairly simple syntax: &lt;coordinates&gt; &lt;horizontal align&gt; &lt;vertical align&gt;
 *
 * <dl>
 * <dd>coordinates</dd>
 * <dt>Provides X/Y coordinates for elements in the grid where 1/1 denotes the upper left corner. 2/1 would create the element in the second column of the first
 * row. Coordinates may also provide rowSpan/columnSpan information: 1-3/2-3 would create the element spanning columns 1 to 3 in rows 2 to 3.
 * {@module #showGrid(boolean)} adds visual indications to help in layouting.</dt>
 *
 * <dd>horizontal alignment</dd>
 * <dt>
 * <ul>
 * <li>&lt; ... align left</li>
 * <li>x ... align center</li>
 * <li>&gt; ... align right</li>
 * <li>o ... fill</li>
 * <li>o! ... grab horizontal space (! may be added to any alignment)</li>
 * </ul>
 * </dt>
 *
 * <dd>vertical alignment</dd>
 * <dt>
 * <ul>
 * <li>^ ... align top</li>
 * <li>x ... align middle</li>
 * <li>v ... align bottom</li>
 * <li>o ... fill</li>
 * <li>o! ... grab vertical space (! may be added to any alignment)</li>
 * </ul>
 * </dt> Most methods of this module deal with SWT components. To access them the code needs to be run in the UI thread. See {@link UIModule} for more
 * information.
 * </dl>
 */
public class UIBuilderModule extends AbstractScriptModule {

	private ViewModel fViewModel;
	private CompositeRenderer fRenderer;
	private ScriptableDialog fScriptableDialog;

	/**
	 * Create a view with scripted content. Automatically sets the active composite for further commands. This view will not be stored when the workbench gets
	 * closed.
	 *
	 * @param title
	 *            view title
	 * @param onTopOfView
	 *            name/ID of an existing view to put this view on top of
	 * @return view instance
	 * @throws Throwable
	 *             when the view cannot be created
	 */
	@WrapToScript
	public MPart createView(String title, @ScriptParameter(defaultValue = ScriptParameter.NULL) String onTopOfView) throws Throwable {
		final RunnableWithResult<MPart> runnable = new RunnableWithResult<MPart>() {

			@Override
			public void runWithTry() throws Throwable {
				final EPartService partService = PlatformUI.getWorkbench().getService(EPartService.class);
				final IWorkbench workbench = PlatformUI.getWorkbench().getService(IWorkbench.class);
				final MApplication mApplication = workbench.getApplication();

				final EModelService modelService = PlatformUI.getWorkbench().getService(EModelService.class);

				// create part
				final MPart part = MBasicFactory.INSTANCE.createPart();
				part.setLabel(title);
				part.setElementId("org.eclipse.ease.view.dynamic:1");
				part.setCloseable(true);
				part.getPersistedState().put(IWorkbench.PERSIST_STATE, Boolean.FALSE.toString());

				// find stack to put it into
				final List<MPartStack> stacks = modelService.findElements(mApplication, null, MPartStack.class, null);
				stacks.get(0).getChildren().add(part);
				partService.showPart(part, PartState.ACTIVATE);

				setResult(part);

				setComposite((Composite) part.getWidget());

				// make sure to close this window before we terminate. Eclipse would store it in its layout actually destroying all layout data.
				PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

					@Override
					public boolean preShutdown(org.eclipse.ui.IWorkbench workbench, boolean forced) {
						partService.hidePart(part, true);
						return true;
					}

					@Override
					public void postShutdown(org.eclipse.ui.IWorkbench workbench) {
					}
				});
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResultFromTry();
	}

	/**
	 * Create a dialog with scripted content. To populate the dialog we need to provide a callback method that creates UI elements for the dialog area. After
	 * the setup the dialog can be shown by calling dialog.open(). To retrieve input data after the dialog got closed call dialog.getData(uiComponent).
	 *
	 * @param layoutCode
	 *            script code to be called when dialog window is created
	 * @param title
	 *            dialog title text displayed in header area
	 * @param message
	 *            dialog info message displayed in header area
	 * @return dialog instance
	 * @throws Throwable
	 *             when dialog cannot be created.
	 */
	@WrapToScript
	public ScriptableDialog createDialog(Object layoutCode, @ScriptParameter(defaultValue = "Dialog") String title,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) String message) throws Throwable {
		return runInUIThread(new RunnableWithResult<ScriptableDialog>() {
			@Override
			public void runWithTry() throws Throwable {
				final ScriptableDialog dialog = new ScriptableDialog(Display.getDefault().getActiveShell(), new DialogRunnable() {

					@Override
					public void run() {
						UIBuilderModule.this.setComposite(getComposite());
						fScriptableDialog = getDialog();
						getScriptEngine().inject(layoutCode);
						fScriptableDialog = null;
					}
				});

				dialog.setTitleText(title);
				dialog.setMessageText(message);

				setResult(dialog);
			}
		});
	}

	/**
	 * Sets the active composite for further commands. The composite layout will be set to {@link GridLayout} if not already done.
	 *
	 * @param composite
	 *            composite to be used
	 */
	@WrapToScript
	public void setComposite(Composite composite) {
		if ((composite.getLayout() == null) || !(composite.getLayout() instanceof GridLayout)) {
			final GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;

			composite.setLayout(gridLayout);
		}

		fRenderer = new CompositeRenderer(composite);
		fViewModel = new ViewModel(fRenderer);
	}

	/**
	 * Get the active composite.
	 *
	 * @return active composite or <code>null</code>
	 */
	@WrapToScript
	public Composite getComposite() {
		return fRenderer.getParent();
	}

	/**
	 * Set the minimum column count. If the current column count is already equal or higher then this method does nothing.
	 *
	 * @param columns
	 *            minimum amount of columns
	 */
	@WrapToScript
	public void setColumnCount(int columns) {
		Display.getDefault().syncExec(() -> {
			fViewModel.setColumnCount(columns);
			fRenderer.update();
		});
	}

	/**
	 * Create a label.
	 *
	 * @param label
	 *            label text
	 * @param layout
	 *            layout data (see module documentation)
	 * @return label instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public Label createLabel(String label, @ScriptParameter(defaultValue = "<") String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<Label>() {
			@Override
			public void runWithTry() throws Throwable {
				final Label swtLabel = new Label(fRenderer.getParent(), SWT.NONE);
				swtLabel.setText(label);

				fViewModel.insertElement(swtLabel, new Location(layout));
				fRenderer.update();

				setResult(swtLabel);
			}
		});
	}

	/**
	 * Create a separator.
	 *
	 * @param horizontal
	 *            <code>true</code> for horizontal, <code>false</code> for vertical
	 * @param layout
	 *            layout data (see module documentation)
	 * @return separator instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public Label createSeparator(@ScriptParameter(defaultValue = "true") boolean horizontal, @ScriptParameter(defaultValue = "o") String layout)
			throws Throwable {
		return runInUIThread(new RunnableWithResult<Label>() {
			@Override
			public void runWithTry() throws Throwable {
				final Label swtLabel = new Label(fRenderer.getParent(), SWT.SEPARATOR | (horizontal ? SWT.HORIZONTAL : SWT.VERTICAL));

				fViewModel.insertElement(swtLabel, new Location(layout));
				fRenderer.update();

				setResult(swtLabel);
			}
		});
	}

	/**
	 * Create a single line text input.
	 *
	 * @param layout
	 *            layout data (see module documentation)
	 * @return text instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public Text createText(@ScriptParameter(defaultValue = "o!") String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<Text>() {
			@Override
			public void runWithTry() throws Throwable {
				final Text text = new Text(fRenderer.getParent(), SWT.BORDER);

				fViewModel.insertElement(text, new Location(layout));
				fRenderer.update();

				setResult(text);
			}
		});
	}

	/**
	 * Create a multi line text input.
	 *
	 * @param layout
	 *            layout data (see module documentation)
	 * @return text instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public Text createTextBox(@ScriptParameter(defaultValue = "o!") String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<Text>() {
			@Override
			public void runWithTry() throws Throwable {
				final Text text = new Text(fRenderer.getParent(), SWT.BORDER | SWT.MULTI | SWT.WRAP);

				fViewModel.insertElement(text, new Location(layout));
				fRenderer.update();

				setResult(text);
			}
		});
	}

	/**
	 * Create a push button.
	 *
	 * @param label
	 *            button text
	 * @param callback
	 *            callback code when button gets pressed
	 * @param layout
	 *            layout data (see module documentation)
	 * @return button instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public Button createButton(String label, Object callback, @ScriptParameter(defaultValue = ScriptParameter.NULL) String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<Button>() {
			@Override
			public void runWithTry() throws Throwable {
				final Button button = new Button(fRenderer.getParent(), SWT.NONE);
				button.setText(label);

				button.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						getScriptEngine().inject(callback);
					}
				});

				fViewModel.insertElement(button, new Location(layout));
				fRenderer.update();

				setResult(button);
			}
		});
	}

	/**
	 * Create a checkbox.
	 *
	 * @param label
	 *            checkbox text
	 * @param callback
	 *            callback code when the checkbox gets ticked/unticked
	 * @param layout
	 *            layout data (see module documentation)
	 * @return checkbox instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public Button createCheckBox(String label, @ScriptParameter(defaultValue = ScriptParameter.NULL) Object callback,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<Button>() {
			@Override
			public void runWithTry() throws Throwable {
				final Button button = new Button(fRenderer.getParent(), SWT.CHECK);
				button.setText(label);

				if (callback != null) {
					button.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							getScriptEngine().inject(callback);
						}
					});
				}

				fViewModel.insertElement(button, new Location(layout));
				fRenderer.update();

				setResult(button);
			}
		});
	}

	/**
	 * Create a combo box. The first element will automatically be selected.
	 *
	 * @param elements
	 *            combo elements to display.
	 * @param callback
	 *            callback code when selection changes
	 * @param layout
	 *            layout data (see module documentation)
	 * @return combo viewer instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public ComboViewer createCombo(String[] elements, @ScriptParameter(defaultValue = ScriptParameter.NULL) Object callback,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<ComboViewer>() {
			@Override
			public void runWithTry() throws Throwable {
				final ComboViewer comboViewer = new ComboViewer(fRenderer.getParent());
				comboViewer.setLabelProvider(new LabelProvider());
				comboViewer.setContentProvider(ArrayContentProvider.getInstance());
				comboViewer.setInput(elements);

				comboViewer.setSelection(new StructuredSelection(elements[0]));

				if (callback != null) {
					comboViewer.addSelectionChangedListener(event -> getScriptEngine().inject(callback));
				}

				fViewModel.insertElement(comboViewer.getControl(), new Location(layout));
				fRenderer.update();

				if (fScriptableDialog != null)
					fScriptableDialog.registerViewer(comboViewer.getControl(), comboViewer);

				setResult(comboViewer);
			}
		});
	}

	/**
	 * Create a list viewer.
	 *
	 * @param elements
	 *            list elements to display
	 * @param callback
	 *            callback when selection changes
	 * @param layout
	 *            layout data (see module documentation)
	 * @return list viewer instance
	 * @throws Throwable
	 *             on any SWT error
	 */
	@WrapToScript
	public ListViewer createList(String[] elements, @ScriptParameter(defaultValue = ScriptParameter.NULL) Object callback,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<ListViewer>() {
			@Override
			public void runWithTry() throws Throwable {
				final ListViewer listViewer = new ListViewer(fRenderer.getParent());
				listViewer.setLabelProvider(new LabelProvider());
				listViewer.setContentProvider(ArrayContentProvider.getInstance());
				listViewer.setInput(elements);

				if (callback != null) {
					listViewer.addSelectionChangedListener(event -> getScriptEngine().inject(callback));
				}

				fViewModel.insertElement(listViewer.getControl(), new Location(layout));
				fRenderer.update();

				if (fScriptableDialog != null)
					fScriptableDialog.registerViewer(listViewer.getControl(), listViewer);

				setResult(listViewer);
			}
		});
	}

	/**
	 * Add a generic control.
	 *
	 * @param control
	 *            control to add
	 * @param layout
	 *            layout data (see module documentation)
	 * @return control instance
	 * @throws Throwable
	 *             when control cannot be added
	 */
	@WrapToScript
	public Control addControl(Control control, @ScriptParameter(defaultValue = ScriptParameter.NULL) String layout) throws Throwable {
		return runInUIThread(new RunnableWithResult<Control>() {
			@Override
			public void runWithTry() throws Throwable {
				fViewModel.insertElement(control, new Location(layout));
				fRenderer.update();

				setResult(control);
			}
		});
	}

	/**
	 * Display a grid aiding in layouting. When the grid is displayed, empty cells do show a label with their location coordinates.
	 *
	 * @param showGrid
	 *            <code>true</code> to display.
	 */
	@WrapToScript
	public void showGrid(@ScriptParameter(defaultValue = "true") boolean showGrid) {
		Display.getDefault().syncExec(() -> fRenderer.setShowGrid(showGrid));
	}

	private <T> T runInUIThread(RunnableWithResult<T> runnable) throws Throwable {
		Display.getDefault().syncExec(runnable);

		return runnable.getResultFromTry();
	}
}
