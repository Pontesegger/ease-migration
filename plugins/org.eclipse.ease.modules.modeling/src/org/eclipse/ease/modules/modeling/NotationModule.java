/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.modeling.selector.GMFNotationSelector;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.NotationPackage;

/**
 * This module help to handle Notation models.
 */
public class NotationModule extends AbstractScriptModule {

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);

		getEcoreModule().initEPackage(NotationPackage.eNS_URI);
	}

	private EcoreModule getEcoreModule() {
		return getEnvironment().getModule(EcoreModule.class);
	}

	private SelectionModule getSelectionModule() {
		return getEnvironment().getModule(SelectionModule.class);
	}

	/**
	 * Returns the currently selected model element, either in the editor or the outline view. If several elements are selected, only the first is returned.
	 *
	 * @return the currently selected model element.
	 */
	@WrapToScript
	public EObject getSelection() {
		final Object selection = getSelectionModule().getCustomSelectionFromSelector(GMFNotationSelector.SELECTOR_ID);
		if (selection instanceof EObject) {
			return (EObject) selection;
		} else {
			final String message = "Unable to retreive a EObject from the selection";
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", message);
			return null;
		}
	}
}
