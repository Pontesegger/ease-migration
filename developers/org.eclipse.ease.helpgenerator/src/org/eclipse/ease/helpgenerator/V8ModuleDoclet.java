/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.helpgenerator;

import java.util.Arrays;

import org.eclipse.ease.helpgenerator.sunapi.Java5ModuleDoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class V8ModuleDoclet extends Doclet {

	public static final String OPTION_PROJECT_ROOT = "-root";
	public static final String OPTION_LINK = "-link";
	public static final String OPTION_LINK_OFFLINE = "-linkoffline";
	public static final String OPTION_FAIL_ON_HTML_ERRORS = "-failOnHTMLError";
	public static final String OPTION_FAIL_ON_MISSING_DOCS = "-failOnMissingDocs";

	public static boolean start(final RootDoc root) {
		final Java5ModuleDoclet doclet = new Java5ModuleDoclet();
		doclet.setRootDoc(root);

		// parse options
		final String[][] options = root.options();
		for (final String[] option : options) {

			if (OPTION_PROJECT_ROOT.equals(option[0]))
				doclet.setParameter(OPTION_PROJECT_ROOT, Arrays.asList(option[1]));

			else if (OPTION_LINK.equals(option[0]))
				doclet.registerLinks(option[1]);

			else if (OPTION_LINK_OFFLINE.equals(option[0]))
				doclet.registerOfflineLinks(option[1], option[2]);

			else if (OPTION_FAIL_ON_HTML_ERRORS.equals(option[0]))
				doclet.setParameter(OPTION_FAIL_ON_HTML_ERRORS, Arrays.asList(option[1]));

			else if (OPTION_FAIL_ON_MISSING_DOCS.equals(option[0]))
				doclet.setParameter(OPTION_FAIL_ON_MISSING_DOCS, Arrays.asList(option[1]));
		}

		return doclet.run();
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	public static int optionLength(final String option) {
		if (OPTION_PROJECT_ROOT.equals(option))
			return 2;

		if (OPTION_LINK.equals(option))
			return 2;

		if (OPTION_LINK_OFFLINE.equals(option))
			return 3;

		if (OPTION_FAIL_ON_HTML_ERRORS.equals(option))
			return 2;

		if (OPTION_FAIL_ON_MISSING_DOCS.equals(option))
			return 2;

		if ("-encoding".equals(option))
			return 2;

		if ("-protected".equals(option))
			return 1;

		if ("-author".equals(option))
			return 1;

		if ("-bottom".equals(option))
			return 2;

		if ("-charset".equals(option))
			return 2;

		if ("-docencoding".equals(option))
			return 2;

		if ("-doctitle".equals(option))
			return 2;

		if ("-windowtitle".equals(option))
			return 2;

		if ("-d".equals(option))
			return 2;

		if ("-use".equals(option))
			return 1;

		if ("-version".equals(option))
			return 1;

		return Doclet.optionLength(option);
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}
}
