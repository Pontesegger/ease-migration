/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This module help to handle UML models
 */
public class UMLModule extends AbstractScriptModule {

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);

		getEcoreModule().initEPackage(NotationPackage.eNS_URI);
	}

	private EcoreModule getEcoreModule() {
		return getEnvironment().getModule(EcoreModule.class);
	}

	/**
	 * Get the UML model from the active editor.
	 *
	 * @return UML model
	 */
	@WrapToScript
	public Model getModel() {
		final EditingDomain editingDomain = getEcoreModule().getEditingDomain();
		if (editingDomain == null) {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to retrieve editing domain");
		}
		final ResourceSet resourceSet = editingDomain.getResourceSet();
		if (resourceSet == null) {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to retrieve the resource set");
		}
		for (final Resource r : resourceSet.getResources()) {
			final Model result = lookForModel(r);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private Model lookForModel(final Resource r) {
		final URI resourceURI = r.getURI();
		if (resourceURI != null) {
			if (UMLPackage.eNS_PREFIX.equals(resourceURI.fileExtension())) {
				final EList<EObject> content = r.getContents();
				if (!content.isEmpty()) {
					final EObject root = content.get(0);
					if (root instanceof Model) {
						return (Model) root;
					}
				}
			}
		}
		return null;
	}
}
