/*******************************************************************************
 * Copyright (c) 2013, 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Martin Kloesch - extensions
 *******************************************************************************/
package org.eclipse.ease.ui.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.CodeCompletionAggregator;
import org.eclipse.ease.ui.completion.CompletionLabelProvider;
import org.eclipse.ease.ui.completion.IImageResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.provider.AbstractCompletionProvider.DescriptorImageResolver;
import org.eclipse.ease.ui.completion.provider.ICompletionProvider;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.ease.ui.dnd.ShellDropTarget;
import org.eclipse.ease.ui.handler.ToggleDropinsSection;
import org.eclipse.ease.ui.help.hovers.ContentProposalModifier;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.ease.ui.views.shell.dropins.DropinTools;
import org.eclipse.ease.ui.views.shell.dropins.IShellDropin;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osgi.service.prefs.Preferences;

/**
 * The JavaScript shell allows to interactively execute JavaScript code.
 */
public class ScriptShell extends ViewPart implements IPropertyChangeListener, IScriptEngineProvider, IExecutionListener, ITabbedPropertySheetPageContributor {

	public static final String VIEW_ID = "org.eclipse.ease.ui.views.scriptShell";

	private static final String XML_HISTORY_NODE = "history";

	private static final int[] DEFAULT_SASH_WEIGHTS = { 70, 30 };

	private class AutoFocus implements KeyListener {

		@Override
		public void keyReleased(final KeyEvent e) {
			if ((e.keyCode == 'v') && ((e.stateMask & SWT.CONTROL) != 0)) {
				// CTRL-v pressed
				final Clipboard clipboard = new Clipboard(Display.getDefault());
				final Object content = clipboard.getContents(TextTransfer.getInstance());
				if (content != null)
					fInputCombo.setText(fInputCombo.getText() + content.toString());

				fInputCombo.setFocus();
				fInputCombo.setSelection(new Point(fInputCombo.getText().length(), fInputCombo.getText().length()));
			}
		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (!((e.keyCode == 'c') && ((e.stateMask & SWT.CONTROL) != 0)) && (e.keyCode != SWT.CONTROL)) {
				fInputCombo.setText(fInputCombo.getText() + e.character);
				fInputCombo.setFocus();
				fInputCombo.setSelection(new Point(fInputCombo.getText().length(), fInputCombo.getText().length()));
			}
		}
	}

	private SashForm fSashForm;

	private Combo fInputCombo;

	private ScriptHistoryText fOutputText;

	private int[] fSashWeights = DEFAULT_SASH_WEIGHTS;

	private IReplEngine fScriptEngine;

	private IMemento fInitMemento;

	private int fHistoryLength;

	private boolean fKeepCommand;

	private AutoFocus fAutoFocusListener = null;

	private CodeCompletionAggregator fCompletionDispatcher = null;

	private Collection<IShellDropin> fDropins = null;

	private String[] fHistory;

	/**
	 * Default constructor.
	 */
	public ScriptShell() {
		super();

		// FIXME add preferences lookup
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public final void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);

		// cannot restore command history right now, do this in
		// createPartControl()
		fInitMemento = memento;
	}

	@Override
	public final void saveState(final IMemento memento) {
		// save command history
		for (final String item : fInputCombo.getItems())
			memento.createChild(XML_HISTORY_NODE).putTextData(item);

		super.saveState(memento);
	}

	@Override
	public final void createPartControl(final Composite parent) {

		// setup layout
		parent.setLayout(new GridLayout());

		fSashForm = new SashForm(parent, SWT.NONE);
		fSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fOutputText = new ScriptHistoryText(fSashForm, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		fOutputText.setAlwaysShowScrollBars(false);

		fOutputText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// copy line under cursor in input box
				final String selected = fOutputText.getLine(fOutputText.getLineIndex(e.y));
				if (!selected.isEmpty()) {
					fInputCombo.setText(selected);
					fInputCombo.setFocus();
					fInputCombo.setSelection(new Point(0, selected.length()));
				}
			}
		});

		final TabFolder tabFolder = new TabFolder(fSashForm, SWT.BOTTOM);

		fDropins = getAvailableDropins();
		for (final IShellDropin dropin : fDropins) {
			final TabItem tab = new TabItem(tabFolder, SWT.NONE);
			tab.setText(dropin.getTitle());
			tab.setControl(dropin.getPartControl(getSite(), tabFolder));
		}
		showDropinsPane(shallDisplayDropins());

		fInputCombo = new Combo(parent, SWT.NONE);
		fInputCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				final String input = fInputCombo.getText();
				fInputCombo.setText("");
				fScriptEngine.execute(new Script("User input", input, true));
			}
		});

		fInputCombo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(final MouseEvent e) {
				if (e.count == 3)
					// select whole line
					fInputCombo.setSelection(new Point(0, fInputCombo.getText().length()));
			}
		});

		fInputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// restore command history
		if (fInitMemento != null) {
			for (final IMemento node : fInitMemento.getChildren(XML_HISTORY_NODE)) {
				if (node.getTextData() != null)
					fInputCombo.add(node.getTextData());
			}
		}
		fHistory = fInputCombo.getItems().clone();

		// clear reference as we are done with initialization
		fInitMemento = null;

		// add DND support
		ShellDropTarget.addDropSupport(fOutputText, this);

		// read default preferences
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		fHistoryLength = prefs.getInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, IPreferenceConstants.DEFAULT_SHELL_HISTORY_LENGTH);
		fKeepCommand = prefs.getBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, IPreferenceConstants.DEFAULT_SHELL_KEEP_COMMAND);

		final boolean autoFocus = prefs.getBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, IPreferenceConstants.DEFAULT_SHELL_AUTOFOCUS);
		if (autoFocus) {
			if (fAutoFocusListener == null)
				fAutoFocusListener = new AutoFocus();

			fOutputText.addKeyListener(fAutoFocusListener);
		}

		final TextSelectionProvider selectionProvider = new TextSelectionProvider();
		fOutputText.addSelectionListener(selectionProvider);
		getSite().setSelectionProvider(selectionProvider);

		// UI is ready, start script engine
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);

		// try to load preferred engine
		final String engineID = prefs.get(IPreferenceConstants.SHELL_DEFAULT_ENGINE, IPreferenceConstants.DEFAULT_SHELL_DEFAULT_ENGINE);
		EngineDescription engineDescription = scriptService.getEngineByID(engineID);

		if (engineDescription == null) {
			// not found, try to load any JavaScript engine
			engineDescription = scriptService.getEngine("JavaScript");

			if (engineDescription == null) {
				// no luck either, get next engine of any type
				final Collection<EngineDescription> engines = scriptService.getEngines();
				if (!engines.isEmpty())
					engineDescription = engines.iterator().next();
			}
		}

		if (engineDescription != null)
			setEngine(engineDescription.getID());

		else {
			final ScriptResult invalidEngine = new ScriptResult();
			invalidEngine.setException(new ScriptExecutionException("No script engines available"));
			fOutputText.printResult(invalidEngine);
		}

		getSite().setSelectionProvider(new DropinsSelectionProvider(tabFolder, getAvailableDropins()));
	}

	private Collection<IShellDropin> getAvailableDropins() {
		if (fDropins == null)
			fDropins = DropinTools.getAvailableDropins();

		return fDropins;
	}

	private boolean shallDisplayDropins() {
		final ICommandService service = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command command = service.getCommand(ToggleDropinsSection.COMMAND_ID);

		final State state = command.getState(RegistryToggleState.STATE_ID);
		if (state == null)
			return true;
		if (!(state.getValue() instanceof Boolean))
			return true;

		return ((Boolean) state.getValue()).booleanValue();
	}

	private void addAutoCompletion() {
		fCompletionDispatcher.addCompletionProvider(new ICompletionProvider() {

			private final IImageResolver fImageResolver = new DescriptorImageResolver(
					Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/history.png"));

			@Override
			public boolean isActive(ICompletionContext context) {
				return true;
			}

			@Override
			public Collection<ScriptCompletionProposal> getProposals(ICompletionContext context) {
				return Arrays.asList(fHistory).stream().filter(t -> t.startsWith(context.getText()))
						.map(t -> new ScriptCompletionProposal(context, t, t, fImageResolver, ScriptCompletionProposal.ORDER_HISTORY, null))
						.collect(Collectors.toList());
			}
		});

		final ContentProposalModifier contentAssistAdapter = new ContentProposalModifier(fInputCombo, new ComboContentAdapter(), fCompletionDispatcher,
				KeyStroke.getInstance(SWT.CTRL, ' '), new char[] { '.' });

		contentAssistAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		contentAssistAdapter.setLabelProvider(new CompletionLabelProvider());
		contentAssistAdapter.setAutoActivationDelay(500);
	}

	public void runStartupCommands() {
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		for (final ScriptType scriptType : fScriptEngine.getDescription().getSupportedScriptTypes()) {
			final String initCommands = prefs.get(IPreferenceConstants.SHELL_STARTUP + scriptType.getName(), "").trim();
			if (!initCommands.isEmpty())
				fScriptEngine.execute(initCommands);
			else {
				final ICodeFactory codeFactory = ScriptService.getCodeFactory(fScriptEngine);
				if (codeFactory != null) {
					final String helpComment = codeFactory.createCommentedString("use help(\"<topic>\") to get more information", false);
					fScriptEngine.execute(helpComment);
				}
			}
		}
	}

	/**
	 * Add a command to the command history. History is stored in a ring buffer, so old entries will drop out once new entries are added. History will be
	 * preserved over program sessions.
	 *
	 * @param input
	 *            command to be stored to history.
	 */
	private void addToHistory(final String input) {
		Display.getDefault().asyncExec(() -> {
			if (fInputCombo.getSelectionIndex() != -1)
				fInputCombo.remove(fInputCombo.getSelectionIndex());

			else {
				// new element; check if we already have such an element in our
				// history
				for (int index = 0; index < fInputCombo.getItemCount(); index++) {
					if (fInputCombo.getItem(index).equals(input)) {
						fInputCombo.remove(index);
						break;
					}
				}
			}

			// avoid history overflows
			while (fInputCombo.getItemCount() >= fHistoryLength)
				fInputCombo.remove(fInputCombo.getItemCount() - 1);

			fInputCombo.add(input, 0);

			fHistory = fInputCombo.getItems().clone();
		});
	}

	@Override
	public final void dispose() {
		if (fScriptEngine != null) {
			fScriptEngine.removeExecutionListener(this);
			fScriptEngine.terminate();
		}

		super.dispose();
	}

	@Override
	public final void setFocus() {
		fInputCombo.setFocus();
	}

	public final void clearOutput() {
		fOutputText.clear();
	}

	public final void showDropinsPane(boolean show) {
		if (show) {
			fSashForm.setWeights(fSashWeights);
			if (fSashWeights[1] == 0)
				fSashForm.setWeights(DEFAULT_SASH_WEIGHTS);

		} else {
			fSashWeights = fSashForm.getWeights();
			fSashForm.setWeights(new int[] { 100, 0 });
		}

		fDropins.stream().forEach(d -> d.setHidden(!show));
	}

	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		// a preference property changed

		if (IPreferenceConstants.SHELL_AUTOFOCUS.equals(event.getProperty())) {
			if (Boolean.parseBoolean(event.getNewValue().toString())) {
				if (fAutoFocusListener == null)
					fAutoFocusListener = new AutoFocus();

				fOutputText.addKeyListener(fAutoFocusListener);

			} else
				fOutputText.removeKeyListener(fAutoFocusListener);

		} else if (IPreferenceConstants.SHELL_KEEP_COMMAND.equals(event.getProperty())) {
			fKeepCommand = Boolean.parseBoolean(event.getNewValue().toString());

		} else if (IPreferenceConstants.SHELL_HISTORY_LENGTH.equals(event.getProperty())) {
			fHistoryLength = Integer.parseInt(event.getNewValue().toString());
		}
	}

	public void stopScriptEngine() {
		getScriptEngine().terminateCurrent();
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fScriptEngine;
	}

	public void changePartName(String newPartName) {
		setPartName(newPartName);
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {

		if (status == SCRIPT_START) {
			try {
				// store code in history
				addToHistory(script.getCode());

				if (fKeepCommand) {
					final String code = script.getCode();
					Display.getDefault().asyncExec(() -> {

						if (!fInputCombo.isDisposed()) {
							fInputCombo.setText(code);
							fInputCombo.setSelection(new Point(0, code.length()));
						}
					});
				}
			} catch (final Exception e) {
				// script.getCode() failed, gracefully continue
			}
		}
	}

	public final void setEngine(final String id) {
		if (fScriptEngine != null) {
			fOutputText.removeScriptEngine(fScriptEngine);

			fScriptEngine.removeExecutionListener(this);
			fScriptEngine.terminate();
		}

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final IScriptEngine candidate = scriptService.getEngineByID(id).createEngine();
		if (candidate instanceof IReplEngine)
			fScriptEngine = (IReplEngine) candidate;
		else {
			final ScriptResult invalidEngine = new ScriptResult();
			invalidEngine.setException(new ScriptExecutionException("Invalid engine selected for shell: " + id));
			fOutputText.printResult(invalidEngine);
		}

		fInputCombo.setEnabled(fScriptEngine != null);

		if (fScriptEngine != null) {
			fScriptEngine.setTerminateOnIdle(false);

			// set view title
			final String partName = scriptService.getEngineByID(id).getName() + " Script Shell";
			setPartName(partName);

			// prepare console
			final ScriptConsole console = ScriptConsole.create(partName, fScriptEngine);
			fScriptEngine.setOutputStream(console.getOutputStream());
			fScriptEngine.setErrorStream(console.getErrorStream());
			fScriptEngine.setInputStream(console.getInputStream());

			// register at script engine
			fScriptEngine.addExecutionListener(this);

			// start script engine
			fScriptEngine.schedule();

			fOutputText.addScriptEngine(fScriptEngine);

			// execute startup scripts
			// TODO currently we cannot run this on the first launch as the UI
			// is not ready yet
			if (fInputCombo != null)
				runStartupCommands();

			// update drop-ins
			for (final IShellDropin dropin : fDropins)
				dropin.setScriptEngine(fScriptEngine);

			// set script engine
			fCompletionDispatcher = new CodeCompletionAggregator(fScriptEngine);
			addAutoCompletion();
		}
	}

	@Override
	public String getContributorId() {
		return VIEW_ID;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IPropertySheetPage.class)
			return adapter.cast(new TabbedPropertySheetPage(this));

		return super.getAdapter(adapter);
	}
}
