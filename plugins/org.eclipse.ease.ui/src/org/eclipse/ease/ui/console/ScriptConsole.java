/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.console;

import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Script;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.themes.IThemeManager;

public class ScriptConsole extends IOConsole implements IExecutionListener, IScriptEngineProvider, IPropertyChangeListener {

	private static final String TITLE_TERMINATED = " [terminated]";
	public static final String CONSOLE_ACTIVE = "ACTIVE";

	public static ScriptConsole create(final String title, final IScriptEngine engine) {
		final ScriptConsole console = new ScriptConsole(title, engine);

		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);

		return console;
	}

	public static ScriptConsole create(final IScriptEngine engine) {
		return create(engine.getName(), engine);
	}

	private IOConsoleOutputStream fOutputStream = null;
	private IOConsoleOutputStream fErrorStream = null;
	private IScriptEngine fEngine = null;
	private ILaunch fLaunch = null;
	private ScriptConsolePageParticipant fScriptConsolePageParticipant;

	private ScriptConsole(final String name, final IScriptEngine engine) {
		this(name, getConsoleType(), null, engine);
	}

	private ScriptConsole(final String name, final String consoleType, final ImageDescriptor imageDescriptor, final IScriptEngine engine) {
		super(name, consoleType, imageDescriptor, true);

		setScriptEngine(engine);

		initializeStreams();

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected void init() {
		super.init();

		final IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP)) {
			setConsoleWidth(store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH));
		}
		setTabWidth(store.getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH));

		if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT)) {
			final int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
			final int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
			setWaterMarks(lowWater, highWater);
		}

		Display.getDefault().asyncExec(() -> {
			setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
			setBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
		});
	}

	private void initializeStreams() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		final IOConsoleOutputStream outputStream = getOutputStream();
		outputStream.setActivateOnWrite(store.getBoolean(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_OUT));

		final IOConsoleOutputStream errorStream = getErrorStream();
		errorStream.setActivateOnWrite(store.getBoolean(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_ERR));

		// set error stream color
		final IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		final ColorRegistry colorRegistry = themeManager.getCurrentTheme().getColorRegistry();
		errorStream.setColor(colorRegistry.get("ERROR_COLOR"));

		// set input stream color
		getInputStream().setColor(colorRegistry.get("CONSOLE_INPUT_COLOR"));
	}

	public static String getConsoleType() {
		return "Text console type";
	}

	public IOConsoleOutputStream getErrorStream() {
		if ((fErrorStream == null) || (fErrorStream.isClosed()))
			fErrorStream = newOutputStream();

		return fErrorStream;
	}

	public IOConsoleOutputStream getOutputStream() {
		if ((fOutputStream == null) || (fOutputStream.isClosed()))
			fOutputStream = newOutputStream();

		return fOutputStream;
	}

	@Override
	protected void dispose() {
		final Activator activator = Activator.getDefault();
		if (activator != null)
			activator.getPreferenceStore().removePropertyChangeListener(this);

		setScriptEngine(null);

		super.dispose();
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		// do not react on engines that are no longer tracked by this console
		if (engine.equals(getScriptEngine())) {
			switch (status) {
			case ENGINE_END:
				terminate();
				break;
			}
		}
	}

	public synchronized void terminate() {
		Display.getDefault().asyncExec(() -> setName(getName() + TITLE_TERMINATED));

		setScriptEngine(null);
		closeStreams();
	}

	private void closeStreams() {
		// close streams
		try {
			if (fOutputStream != null)
				fOutputStream.close();
		} catch (final IOException e) {

		}
		try {
			if (fErrorStream != null)
				fErrorStream.close();
		} catch (final IOException e) {
		}
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fEngine;
	}

	public void setLaunch(final ILaunch launch) {
		fLaunch = launch;
	}

	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		final IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		final String property = event.getProperty();

		if (property.equals(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_OUT))
			getOutputStream().setActivateOnWrite((Boolean) event.getNewValue());

		else if (property.equals(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_ERR))
			getErrorStream().setActivateOnWrite((Boolean) event.getNewValue());

		else if (property.equals(IDebugPreferenceConstants.CONSOLE_WRAP) || property.equals(IDebugPreferenceConstants.CONSOLE_WIDTH)) {
			final boolean fixedWidth = store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP);
			if (fixedWidth) {
				final int width = store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH);
				setConsoleWidth(width);
			} else {
				setConsoleWidth(-1);
			}

		} else if (property.equals(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT) || property.equals(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK)
				|| property.equals(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK)) {
			final boolean limitBufferSize = store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT);
			if (limitBufferSize) {
				final int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
				final int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
				if (highWater > lowWater) {
					setWaterMarks(lowWater, highWater);
				}
			} else {
				setWaterMarks(-1, -1);
			}

		} else if (property.equals(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH)) {
			final int tabWidth = store.getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH);
			setTabWidth(tabWidth);

		} else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR)) {
			if (fOutputStream != null)
				fOutputStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR));

		} else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR)) {
			if (fErrorStream != null)
				fErrorStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));

		} else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR)) {
			// if (fInput != null && fInput instanceof IOConsoleInputStream) {
			// ((IOConsoleInputStream) fInput).setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_INPUT_STREAM));
			// }

		} else if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
			setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));

		} else if (property.equals(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR)) {
			setBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
		}
	}

	public synchronized void setScriptEngine(final IScriptEngine scriptEngine) {
		if ((scriptEngine == null) || (!scriptEngine.equals(fEngine))) {
			// new engine detected

			if (fEngine != null) {
				fEngine.removeExecutionListener(this);
				closeStreams();
			}

			fEngine = scriptEngine;

			if (fEngine != null)
				fEngine.addExecutionListener(this);

			if (fScriptConsolePageParticipant != null)
				fScriptConsolePageParticipant.engineChanged();
		}
	}

	public void setPageParticipant(final ScriptConsolePageParticipant scriptConsolePageParticipant) {
		fScriptConsolePageParticipant = scriptConsolePageParticipant;
	}
}
