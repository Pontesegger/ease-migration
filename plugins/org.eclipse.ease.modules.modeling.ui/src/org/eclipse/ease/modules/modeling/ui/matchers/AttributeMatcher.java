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
import java.util.regex.Pattern;

import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

public class AttributeMatcher implements IMatcher {
	private static Pattern ATTRIBUTE_SEARCH = Pattern.compile("(.*)=(.*)", Pattern.DOTALL); //$NON-NLS-1$

	@Override
	public Collection<EObject> getElements(String string, IEditingDomainProvider currentEditor) throws MatcherException {

		throw new RuntimeException("Currently disabled due to API breakage in guava");

		// Matcher matcherAtt = ATTRIBUTE_SEARCH.matcher(string);
		// if (matcherAtt.matches()) {
		// final String name = matcherAtt.group(1);
		// final String regex = matcherAtt.group(2);
		// try {
		// try {
		// final Pattern reg = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		// Iterator<EObject> filter = Iterators.emptyIterator();
		// for (Resource r : currentEditor.getEditingDomain().getResourceSet().getResources()) {
		// if (r != null) {
		// filter = Iterators.concat(filter, filter(r.getAllContents(), new Predicate<EObject>() {
		// @Override
		// public boolean apply(EObject input) {
		// for (EAttribute a : input.eClass().getEAllAttributes()) {
		// if (a.getName().equalsIgnoreCase(name)) {
		// Object val = input.eGet(a);
		// if (val == null) {
		// val = ""; //$NON-NLS-1$
		// }
		// if (reg.matcher(val.toString()).matches()) {
		// return true;
		// }
		// }
		// }
		// return false;
		// }
		// }));
		// }
		// }
		// return newArrayList(filter);
		// } catch (PatternSyntaxException e) {
		// throw new MatcherException(regex + Messages.AttributeMatcher_INCORRECT_PATTERN);
		// }
		// } finally {
		// }
		// } else {
		// throw new MatcherException(Messages.AttributeMatcher_UNRECOGNIZED_PATTERN);
		// }
	}

	@Override
	public String getText() {
		return Messages.AttributeMatcher_TEXT_COMBO_ATTRIBUTE;
	}

	@Override
	public String getHelp() {
		return Messages.AttributeMatcher_HELP_ATTRIBUTE;
	}

}
