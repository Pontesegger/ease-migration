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

package org.eclipse.ease.ui.dialogs;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.ui.dnd.ModulesDropHandler;
import org.eclipse.ease.ui.help.hovers.MethodHelp;
import org.eclipse.ease.ui.help.hovers.ModuleHelp;
import org.eclipse.ease.ui.modules.ui.ModulesTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ParametersDialog extends TitleAreaDialog {
	private final Method fMethod;
	private final Parameter[] fParameters;

	private final List<String> fUserInput = new ArrayList<>();

	public ParametersDialog(Shell shell, Method method, Parameter[] parameters) {
		super(shell);

		fMethod = method;
		fParameters = parameters;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(String.format("Call %s()", fMethod.getName()));
		setMessage(String.format(
				"Provide parameters for the method call. Strings will be transformed to the correct parameter format if possible. Use '%s' for null values.",
				ModulesDropHandler.NULL_INDICATOR));

		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		for (int index = 0; index < fParameters.length; index++) {
			final Parameter parameter = fParameters[index];

			final Label label = new Label(container, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			label.setText(String.format("%s:", parameter.getName()));

			final Text text = new Text(container, SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			final String defaultValue = ScriptParameter.Helper.getDefaultValue(parameter);
			if (ScriptParameter.NULL.equals(defaultValue))
				text.setText(ModulesDropHandler.NULL_INDICATOR);
			else if (defaultValue != null)
				text.setText(defaultValue);

			try {
				final String tooltip = getHelpMessage(fMethod, index);

				label.setToolTipText(tooltip);
				text.setToolTipText(tooltip);
			} catch (final Exception e) {
				// silently swallow, no tooltip available
			}
		}

		return area;
	}

	private String getHelpMessage(Method method, int parameterIndex) throws Exception {
		final URL helpLocation = ModuleHelp.getModuleHelpLocation(ModulesTools.getDeclaringModule(method));
		if (helpLocation != null) {
			final MethodHelp methodHelp = new ModuleHelp(helpLocation).getMethodHelp(method);
			return methodHelp.getParameterDescriptions().get(parameterIndex).getDescription();
		}

		return null;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		final Composite dialogArea = (Composite) getDialogArea();
		final Composite container = (Composite) dialogArea.getChildren()[1];

		for (final Control child : container.getChildren()) {
			if (child instanceof Text)
				fUserInput.add(((Text) child).getText());
		}

		super.okPressed();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(550, 400);
	}

	public List<String> getParameters() {
		return fUserInput;
	}
}
