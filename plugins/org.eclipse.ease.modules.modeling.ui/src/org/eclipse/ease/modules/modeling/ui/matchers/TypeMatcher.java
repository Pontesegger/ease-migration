/*******************************************************************************
 * Copyright (c) 2015 CNES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     JF Rolland (Atos) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.ui.matchers;

import java.util.Collection;

import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

public class TypeMatcher implements IMatcher {

	@Override
	public Collection<EObject> getElements(final String string, IEditingDomainProvider currentEditor) throws MatcherException {

		throw new RuntimeException("Currently disabled due to API breakage in guava");

		// Iterator<EObject> filter = Iterators.emptyIterator();
		// for (Resource r : currentEditor.getEditingDomain().getResourceSet().getResources()) {
		// if (r != null) {
		// filter = Iterators.concat(filter, filter(r.getAllContents(), new Predicate<EObject>() {
		// @Override
		// public boolean apply(EObject input) {
		// List<EClass> allClasses = new LinkedList<EClass>(input.eClass().getEAllSuperTypes());
		// allClasses.add(input.eClass());
		// for (EClass e : allClasses) {
		// if (e.getName().equalsIgnoreCase(string)) {
		// return true;
		// }
		// }
		// return false;
		// }
		// }));
		// }
		// }
		// return Lists.newArrayList(filter);
	}

	@Override
	public String getText() {
		return Messages.TypeMatcher_COMBO_TEXT_TYPE;
	}

	@Override
	public String getHelp() {
		return Messages.TypeMatcher_HELP_TYPE;
	}

}
