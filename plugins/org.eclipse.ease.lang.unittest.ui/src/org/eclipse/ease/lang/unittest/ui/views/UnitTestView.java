/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.views;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.Logger;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.lang.unittest.TestSuiteScriptEngine;
import org.eclipse.ease.lang.unittest.runtime.IMetadata;
import org.eclipse.ease.lang.unittest.runtime.IRuntimePackage;
import org.eclipse.ease.lang.unittest.runtime.ITest;
import org.eclipse.ease.lang.unittest.runtime.ITestClass;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestResult;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ease.lang.unittest.ui.sourceprovider.TestSuiteSource;
import org.eclipse.ease.ui.tools.DecoratedLabelProvider;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.ExpandAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditor;

public class UnitTestView extends ViewPart {
	public static final String VIEW_ID = "org.eclipse.ease.views.unittest";

	public static final String TEST_STATUS_PROPERTY = "test status";

	private class TestEntityContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			final List<Object> children = new ArrayList<>();

			if (parentElement instanceof ITestEntity) {
				children.addAll(((ITestEntity) parentElement).getMetadata());

				if ((!(parentElement instanceof ITest)) || (((ITestEntity) parentElement).getResults().size() > 1))
					children.addAll(((ITestEntity) parentElement).getResults());

				if (parentElement instanceof ITestContainer) {
					for (final Object child : ((ITestContainer) parentElement).getChildren().toArray()) {
						if (child instanceof ITest)
							children.add(child);
					}
				}
			}

			return children.toArray();
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof ITestEntity)
				return ((ITestEntity) element).getParent();

			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ITestEntity) {
				if (!((ITestEntity) element).getMetadata().isEmpty())
					return true;

				if (((ITestEntity) element).getResults().size() > 1)
					return true;

				if (!(element instanceof ITest) && (!((ITestEntity) element).getResults().isEmpty()))
					return true;

				if (element instanceof ITestContainer) {
					for (final Object child : ((ITestContainer) element).getChildren().toArray()) {
						if (child instanceof ITest)
							return true;
					}
				}
			}

			return false;
		}
	}

	/**
	 * Opens the given resource in an editor. Needs to be run from the UI thread.
	 *
	 * @param resource
	 *            resource to open
	 * @param lineNumber
	 *            line number to jump to in editor
	 */
	private static void openEditor(Object resource, int lineNumber) {
		if (resource instanceof IFile) {
			IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(((IFile) resource).getName());
			if (descriptor == null)
				descriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);

			if (descriptor != null) {
				final IEditorDescriptor editorDescriptor = descriptor;
				try {
					final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(new FileEditorInput((IFile) resource), editorDescriptor.getId());

					if ((lineNumber > 0) && (editor instanceof ITextEditor)) {
						final IDocument document = ((ITextEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
						try {
							((ITextEditor) editor).selectAndReveal(document.getLineOffset(lineNumber - 1), document.getLineLength(lineNumber - 1));
						} catch (final BadLocationException e) {
							// failed to open editor at corresponding line number
						}
					}
				} catch (final PartInitException e) {
					Logger.error(Activator.PLUGIN_ID, "Could not open editor for <" + resource + ">", e);
				}
			}
		}
	}

	private static int getTestFileCount(ITestContainer testContainer) {
		if (testContainer instanceof ITestFile)
			return 1;

		int files = 0;
		for (final ITestContainer child : testContainer.getChildContainers())
			files += getTestFileCount(child);

		return files;
	}

	/**
	 * @param stackTrace
	 */
	private static boolean openEditor(ScriptStackTrace stackTrace) {
		if (stackTrace != null) {
			for (final IScriptDebugFrame trace : stackTrace) {
				if (trace.getScript().getCommand() instanceof IFile) {
					openEditor(trace.getScript().getCommand(), trace.getLineNumber());
					return true;
				}

				if (trace.getScript().getCommand() instanceof File) {
					openEditor(trace.getScript().getCommand(), trace.getLineNumber());
					return true;
				}
			}
		}

		return false;
	}

	private ProgressBar fProgressBar;
	private TreeViewer fFileTreeViewer;
	private TreeViewer fTestTreeViewer;
	private SashForm sashForm;

	private int[] fSashWeights = new int[] { 70, 30 };

	private CollapseAllHandler fCollapseAllHandler;

	private ExpandAllHandler fExpandAllHandler;

	private LocalResourceManager fResourceManager;

	private Label lblErrorCount;
	private Label lblFailureCount;

	private SuiteRuntimeInformation fRuntimeInformation = null;

	private TestSuiteScriptEngine fCurrentEngine;

	/** Counts errors during test execution. */
	private int fErrorCount = 0;
	/** Counts failures during test execution. */
	private int fFailureCount = 0;
	/** Counts finished test files during test execution. */
	private int fFinishedFileCount = 0;

	public UnitTestView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout gl_composite = new GridLayout(6, false);
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		fResourceManager = new LocalResourceManager(JFaceResources.getResources(), composite);

		fLblDuration = new Label(composite, SWT.NONE);
		fLblDuration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		fLblDuration.setText("");

		final Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));

		final Label lblErrorIcon = new Label(composite, SWT.NONE);
		final GridData gdLblErrorIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLblErrorIcon.verticalIndent = 10;
		gdLblErrorIcon.horizontalIndent = 50;
		lblErrorIcon.setLayoutData(gdLblErrorIcon);
		lblErrorIcon.setImage(fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_error.png")));
		lblErrorIcon.setToolTipText("Exceptions and fatal execution errors.");

		final Label lblErrors = new Label(composite, SWT.NONE);
		final GridData gd_lblErrors = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblErrors.verticalIndent = 10;
		lblErrors.setLayoutData(gd_lblErrors);
		lblErrors.setAlignment(SWT.CENTER);
		lblErrors.setText("Errors:");
		lblErrors.setToolTipText("Exceptions and fatal execution errors.");

		lblErrorCount = new Label(composite, SWT.NONE);
		final GridData gd_lblErrorCount = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblErrorCount.verticalIndent = 10;
		gd_lblErrorCount.horizontalIndent = 20;
		lblErrorCount.setLayoutData(gd_lblErrorCount);

		final Label lblFailureIcon = new Label(composite, SWT.NONE);
		final GridData gdLblFailureIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLblFailureIcon.verticalIndent = 10;
		gdLblFailureIcon.horizontalIndent = 50;
		lblFailureIcon.setLayoutData(gdLblFailureIcon);
		lblFailureIcon.setImage(fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_failure.png")));
		lblFailureIcon.setToolTipText("Failed test assertions.");

		final Label lblFailures = new Label(composite, SWT.NONE);
		final GridData gd_lblFailures = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblFailures.verticalIndent = 10;
		lblFailures.setLayoutData(gd_lblFailures);
		lblFailures.setAlignment(SWT.CENTER);
		lblFailures.setText("Failures:");
		lblFailures.setToolTipText("Failed test assertions.");

		lblFailureCount = new Label(composite, SWT.NONE);
		final GridData gdLblFailureCount = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLblFailureCount.verticalIndent = 10;
		gdLblFailureCount.horizontalIndent = 20;
		lblFailureCount.setLayoutData(gdLblFailureCount);

		fProgressBar = new ProgressBar(parent, SWT.NONE);
		fProgressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashForm.setOrientation(SWT.VERTICAL);

		fFileTreeViewer = new TreeViewer(sashForm, SWT.BORDER | SWT.MULTI);
		fFileTreeViewer.setContentProvider(new TestSuiteContentProvider());

		fFileTreeViewer.setFilters(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return (element instanceof ITestContainer);
			}
		});

		fFileTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				return (element instanceof ITestFile) ? 1 : 0;
			}
		});

		// use a decorated label provider
		final LabelProvider provider = new TestSuiteLabelProvider(fResourceManager);
		fFileTreeViewer.setLabelProvider(new DecoratedLabelProvider(provider));

		fFileTreeViewer.addDoubleClickListener(event -> {
			final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();

			if (element instanceof ITestFile)
				openEditor(((ITestFile) element).getResource(), 0);

			if (element instanceof ITestClass)
				openEditor(((ITestClass) element).getStackTrace());
		});

		// a changed selection will update the test detail part
		fFileTreeViewer.addSelectionChangedListener(event -> {
			final ITreeSelection selection = (ITreeSelection) event.getSelection();
			final Object element = selection.getFirstElement();

			if ((element instanceof ITestFile) || (element instanceof ITestClass) || (element instanceof ITestSuite)) {
				// test set selected
				fTestTreeViewer.setInput(element);

				if (sashForm.getWeights()[1] == 0)
					sashForm.setWeights(fSashWeights);

				fTestTreeViewer.refresh();

			} else {
				// test container selected, or no selection at all
				fTestTreeViewer.setInput(null);

				if (sashForm.getWeights()[1] != 0)
					fSashWeights = sashForm.getWeights();

				sashForm.setWeights(new int[] { 100, 0 });
			}
		});

		// create tree viewer for tests
		fTestTreeViewer = createTestArea(sashForm);

		sashForm.setWeights(new int[] { 1, 1 });

		// add context menu support
		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(fFileTreeViewer.getTree());
		fFileTreeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, fFileTreeViewer);

		final MenuManager menuManager2 = new MenuManager();
		final Menu menu2 = menuManager2.createContextMenu(fFileTreeViewer.getTree());
		fTestTreeViewer.getControl().setMenu(menu2);
		getSite().registerContextMenu(menuManager2, fTestTreeViewer);

		// add collapseAll/expandAll handlers
		final IHandlerService handlerService = getSite().getService(IHandlerService.class);
		fCollapseAllHandler = new CollapseAllHandler(fFileTreeViewer);
		handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, fCollapseAllHandler);
		fExpandAllHandler = new ExpandAllHandler(fFileTreeViewer);
		handlerService.activateHandler(ExpandAllHandler.COMMAND_ID, fExpandAllHandler);

		// menuManager.setRemoveAllWhenShown(true);

		final MultiSelectionProvider selectionProvider = new MultiSelectionProvider();
		selectionProvider.addSelectionProvider(fFileTreeViewer);
		selectionProvider.addSelectionProvider(fTestTreeViewer);

		getSite().setSelectionProvider(selectionProvider);
	}

	private TreeViewer createTestArea(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final TreeColumnLayout layout = new TreeColumnLayout();
		composite.setLayout(layout);

		final TreeViewer viewer = new TreeViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		// open resource on the corresponding location a double click
		viewer.addDoubleClickListener(event -> {

			final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if (element instanceof ITest) {
				boolean editorOpen = false;
				if ((TestStatus.ERROR.equals(((ITest) element).getStatus())) || (TestStatus.FAILURE.equals(((ITest) element).getStatus()))) {
					final ITestResult result = ((ITest) element).getWorstResult();
					if (result != null)
						editorOpen = openEditor(result.getStackTrace());
				}

				if (!editorOpen)
					editorOpen = openEditor(((ITest) element).getStackTrace());
			}
		});

		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		viewer.setContentProvider(new TestEntityContentProvider());

		final TreeViewerColumn testColumn = new TreeViewerColumn(viewer, SWT.NONE);
		testColumn.getColumn().setWidth(100);
		testColumn.getColumn().setText("Test");
		layout.setColumnData(testColumn.getColumn(), new ColumnWeightData(30, 50, true));
		testColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				if (element instanceof ITestEntity)
					return super.getText(((ITestEntity) element).getName());

				else if (element instanceof IMetadata)
					return super.getText(((IMetadata) element).getKey());

				else if (element instanceof ITestResult)
					return ((ITestResult) element).getStatus().toString();

				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof ITestEntity)
					element = ((ITestEntity) element).getStatus();

				else if (element instanceof ITestResult)
					element = ((ITestResult) element).getStatus();

				else if (element instanceof IMetadata)
					return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/metadata.png"));

				if (element instanceof TestStatus) {
					switch ((TestStatus) element) {
					case PASS:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_pass.png"));
					case FAILURE:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_failure.png"));
					case ERROR:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_error.png"));
					case RUNNING:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_running.png"));
					case DISABLED:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/status_ignore.png"));
					}
				}

				return super.getImage(element);
			}

			@Override
			public String getToolTipText(final Object element) {
				if (element instanceof ITestEntity)
					return ((ITestEntity) element).getDescription();

				return super.getToolTipText(element);
			}
		});

		final TreeViewerColumn messageColumn = new TreeViewerColumn(viewer, SWT.NONE);
		messageColumn.getColumn().setWidth(100);
		messageColumn.getColumn().setText("Description/Status");
		layout.setColumnData(messageColumn.getColumn(), new ColumnWeightData(70, 50, true));
		messageColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ITest) {
					switch (((ITest) element).getStatus()) {
					case RUNNING:
						return "executing ...";
					case NOT_RUN:
						return "waiting for execution";

					case DISABLED:
						// fall through
					case ERROR:
						// fall through
					case FAILURE:
						final ITestResult result = ((ITest) element).getWorstResult();
						if (result != null)
							return result.getMessage();

						// fall through
					default:
						return super.getText(((ITestEntity) element).getDescription());
					}

				} else if (element instanceof IMetadata) {
					return super.getText(((IMetadata) element).getValue());

				} else if (element instanceof ITestResult)
					return super.getText(((ITestResult) element).getMessage());

				else if (element instanceof ITestEntity)
					return ((ITestEntity) element).getDescription();

				return super.getText(element);
			}
		});

		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

		return viewer;
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

	public TreeViewer getFileTreeViewer() {
		return fFileTreeViewer;
	}

	private enum UpdateUIType {
		STRUCTURE, DECORATOR
	}

	public StructuredViewer getTableViewer() {
		return fTestTreeViewer;
	}

	public void notifyEngineCreation(TestSuiteScriptEngine engine) {
		if ((fCurrentEngine == null) || (fCurrentEngine.isFinished())) {
			// switch to a new engine

			if (fCurrentEngine != null)
				fCurrentEngine.getTestRoot().eAdapters().remove(fContentAdapter);

			fErrorCount = 0;
			fFailureCount = 0;
			fFinishedFileCount = 0;

			fFileTreeViewer.setInput(null);

			fProgressBar.setSelection(0);
			fProgressBar.setMaximum(1);

			lblErrorCount.setText(Integer.toString(fErrorCount));
			lblFailureCount.setText(Integer.toString(fFailureCount));

			lblErrorCount.getParent().layout(true, true);

			fCurrentEngine = engine;
			fCurrentEngine.getTestRoot().eAdapters().add(fContentAdapter);
		}
	}

	private class ContentAdapter extends EContentAdapter {
		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);

			// filter some events
			if (notification.getEventType() == Notification.REMOVING_ADAPTER)
				return;
			if (notification.getEventType() == ITestEntity.CUSTOM_CODE)
				return;

			if (IRuntimePackage.Literals.TEST_ENTITY__END_TIMESTAMP.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.TEST_ENTITY__START_TIMESTAMP.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.TEST_ENTITY__ESTIMATED_DURATION.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.STACK_TRACE_CONTAINER__STACK_TRACE.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.TEST_ENTITY__TERMINATED.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.TEST_ENTITY__DESCRIPTION.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.TEST__DURATION_LIMIT.equals(notification.getFeature()))
				return;
			if (IRuntimePackage.Literals.TEST_SUITE__MASTER_ENGINE.equals(notification.getFeature()))
				return;

			if ((IRuntimePackage.Literals.TEST_ENTITY__RESULTS.equals(notification.getFeature())) && (notification.getEventType() == Notification.ADD)) {
				// new result added, see if it was an error/failure
				final Object value = notification.getNewValue();
				if (value instanceof ITestResult) {
					if (TestStatus.ERROR.equals(((ITestResult) value).getStatus()))
						fErrorCount++;
					if (TestStatus.FAILURE.equals(((ITestResult) value).getStatus()))
						fFailureCount++;
				}
			}

			if ((notification.getNotifier() instanceof ITestFile) && (IRuntimePackage.Literals.TEST_ENTITY__ENTITY_STATUS.equals(notification.getFeature()))
					&& (TestStatus.FINISHED.equals(notification.getNewValue()))) {

				// testfile finished
				fFinishedFileCount++;
			}

			// debug output of events
			// if (!IRuntimePackage.Literals.TEST_ENTITY__ENTITY_STATUS.equals(notification.getFeature())) {
			// System.out.println(notification);
			// System.out.println("\t" + notification.getEventType());
			// System.out.println("\t" + notification.getNotifier().getClass().getSimpleName());
			// if (notification.getFeature() instanceof ENamedElement)
			// System.out.println("\t" + ((ENamedElement) notification.getFeature()).getName());
			// }

			final UpdateUIType eventType = ((notification.getEventType() == Notification.ADD) || (notification.getEventType() == Notification.REMOVE))
					? UpdateUIType.STRUCTURE
					: UpdateUIType.DECORATOR;

			if (notification.getNotifier() instanceof ITestEntity)
				fUIJob.refresh((ITestEntity) notification.getNotifier(), eventType);
		}

		@Override
		public boolean isAdapterForType(Object type) {
			return (type instanceof ITestEntity);
		}
	}

	private final ContentAdapter fContentAdapter = new ContentAdapter();

	private final UIUpdateJob fUIJob = new UIUpdateJob();
	private Label fLblDuration;

	private class UIUpdateJob extends UIJob {

		private final Map<ITestEntity, UpdateUIType> fElements = new HashMap<>();

		public UIUpdateJob() {
			super("Script Unittest UI update");
		}

		public void refresh(ITestEntity entity, UpdateUIType type) {
			synchronized (fElements) {
				fElements.put(entity, UpdateUIType.STRUCTURE.equals(fElements.get(entity)) ? UpdateUIType.STRUCTURE : type);
			}

			schedule(300);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (fFileTreeViewer.getTree().isDisposed())
				return Status.CANCEL_STATUS;

			Map<ITestEntity, UpdateUIType> elements;
			synchronized (fElements) {
				elements = new HashMap<>(fElements);
				fElements.clear();
			}

			if (fFileTreeViewer.getInput() == null) {
				// full update
				fFileTreeViewer.setInput(fCurrentEngine.getTestRoot());
				fFileTreeViewer.expandAll();

				TestSuiteSource.getActiveInstance().setActiveSuite(getCurrentTestSuite());

			} else {
				// partial update
				for (final Entry<ITestEntity, UpdateUIType> entry : elements.entrySet()) {
					if (UpdateUIType.STRUCTURE.equals(entry.getValue()))
						fFileTreeViewer.refresh(entry.getKey());

					else
						fFileTreeViewer.update(entry.getKey(), null);
				}
			}

			// check if we are done
			final ITestSuite testSuite = getCurrentTestSuite();
			if (testSuite.getStatus() != TestStatus.RUNNING) {
				fLblDuration.setText("Finished after " + (testSuite.getDuration() / 1000) + " s");

				// store runtime information
				if (fRuntimeInformation != null) {
					fRuntimeInformation.save();
					fRuntimeInformation = null;
				}

				TestSuiteSource.getActiveInstance().setActiveSuite(getCurrentTestSuite());
			}

			else {
				// update remaining time
				final long estimatedDuration = testSuite.getEstimatedDuration();
				if ((estimatedDuration < 0) && (fRuntimeInformation == null)) {
					// load estimations
					fRuntimeInformation = new SuiteRuntimeInformation(testSuite);
					fLblDuration.setText("running ...");
					schedule(1000);

				} else if (estimatedDuration >= 0) {
					final long estimatedLeft = (testSuite.getStartTimestamp() + estimatedDuration) - System.currentTimeMillis();

					if (estimatedLeft > 0) {
						fLblDuration.setText("Finished in " + getDurationString(estimatedLeft));
						schedule(1000);
					} else
						fLblDuration.setText("running ...");
				}
			}

			// update error/failure counters
			lblErrorCount.setText(Integer.toString(fErrorCount));
			lblFailureCount.setText(Integer.toString(fFailureCount));

			lblErrorCount.getParent().layout(true, true);

			// update progress bar
			if (fProgressBar.getMaximum() == 1) {
				// not initialized
				fProgressBar.setMaximum(getTestFileCount(testSuite));
				fProgressBar.setForeground(null);
			}

			if (fProgressBar.getSelection() != fFinishedFileCount) {
				fProgressBar.setSelection(fFinishedFileCount);
				if (testSuite.hasError())
					fProgressBar.setForeground(fResourceManager.createColor(new RGB(207, 36, 43)));
			}

			if (testSuite.getStatus() != TestStatus.RUNNING) {
				// we are done, no matter what, set the bar to its full length
				fProgressBar.setSelection(fProgressBar.getMaximum());

				switch (testSuite.getStatus()) {
				case ERROR:
					fProgressBar.setForeground(fResourceManager.createColor(new RGB(207, 36, 43)));
					break;
				case PASS:
					fProgressBar.setForeground(fResourceManager.createColor(new RGB(92, 167, 86)));
					break;
				}
			}

			return Status.OK_STATUS;
		}

	}

	private String getDurationString(long duration) {
		if (duration >= (60 * 60 * 1000))
			// >= 1 hour
			return new SimpleDateFormat("HH:mm:ss").format(duration);

		if (duration >= (60 * 1000))
			// >= 1 minute
			return new SimpleDateFormat("mm:ss").format(duration);

		return (duration / 1000) + " s";
	}

	@Override
	public void dispose() {
		if (fCurrentEngine != null)
			fCurrentEngine.getTestRoot().eAdapters().remove(fContentAdapter);

		if (fCollapseAllHandler != null)
			fCollapseAllHandler.dispose();

		if (fExpandAllHandler != null)
			fExpandAllHandler.dispose();

		if (fResourceManager != null) {
			fResourceManager.dispose();
			fResourceManager = null;
		}

		super.dispose();
	}

	public void terminateSuite() {
		if (fCurrentEngine != null)
			fCurrentEngine.terminate();
	}

	/**
	 * @return the currentEngine
	 */
	public TestSuiteScriptEngine getCurrentEngine() {
		return fCurrentEngine;
	}

	public ITestSuite getCurrentTestSuite() {
		final Object input = getFileTreeViewer().getInput();
		if (input instanceof ITestContainer) {
			if (!((ITestContainer) input).getChildren().isEmpty()) {
				final ITestEntity testSuite = ((ITestContainer) input).getChildren().get(0);
				if (testSuite instanceof ITestSuite)
					return (ITestSuite) testSuite;
			}
		}

		return null;
	}
}
