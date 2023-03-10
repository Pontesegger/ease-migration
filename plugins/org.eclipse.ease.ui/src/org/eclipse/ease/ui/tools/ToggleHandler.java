/*******************************************************************************
 * Copyright (c) 2009 Ralf Ebert and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Ralf Ebert - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.tools;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * Use this handler for style="toggle" command contributions. You need to declare a state for your command to use ToggleHandler:
 * 
 * <pre>
 * &lt;command id=&quot;somecommand&quot; name=&quot;SomeCommand&quot;&gt;
 *    &lt;state class=&quot;org.eclipse.jface.commands.ToggleState&quot; id=&quot;STYLE&quot;/&gt;
 * &lt;/command&gt;
 * </pre>
 * 
 * The id="STYLE" was chosen because of IMenuStateIds.STYLE - maybe this will work without any Handler foo in later Eclipse versions.
 * 
 * See http://www.ralfebert.de/eclipse/2009_01_21_togglehandler/ http://eclipsesource.com/blogs/2009/01/15/toggling-a-command-contribution/
 * 
 * @author
 */
public abstract class ToggleHandler extends AbstractHandler implements IElementUpdater {

	private String fCommandId;

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		fCommandId = event.getCommand().getId();

		// update toggled state
		final State state = event.getCommand().getState(IMenuStateIds.STYLE);
		if (state == null)
			throw new ExecutionException("You need to declare a ToggleState with id=STYLE for your command to use ToggleHandler!");
		final boolean currentState = (Boolean) state.getValue();
		final boolean newState = !currentState;
		state.setValue(newState);

		// trigger element update
		executeToggle(event, newState);
		commandService.refreshElements(event.getCommand().getId(), null);

		// return value is reserved for future apis
		return null;
	}

	protected abstract void executeToggle(ExecutionEvent event, boolean checked);

	/**
	 * Update command element with toggle state
	 */
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		if (fCommandId != null) {
			final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			final Command command = commandService.getCommand(fCommandId);
			final State state = command.getState(IMenuStateIds.STYLE);
			if (state != null)
				element.setChecked((Boolean) state.getValue());
		}
	}
}