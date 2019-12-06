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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.eclipse.ease.helpgenerator.docletapi.Jep221ModuleDoclet;
import org.eclipse.ease.helpgenerator.sunapi.Java5ModuleDoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

public class ModuleDoclet extends Doclet implements jdk.javadoc.doclet.Doclet {

	public static final String OPTION_PROJECT_ROOT = "-root";
	public static final String OPTION_LINK = "-link";
	public static final String OPTION_LINK_OFFLINE = "-linkoffline";
	public static final String OPTION_FAIL_ON_HTML_ERRORS = "-failOnHTMLError";
	public static final String OPTION_FAIL_ON_MISSING_DOCS = "-failOnMissingDocs";

	// ---------- Java 1.5 API

	public static boolean start(final RootDoc root) {
		final Java5ModuleDoclet doclet = new Java5ModuleDoclet();
		doclet.setRootDoc(root);

		// parse options
		final String[][] options = root.options();
		for (final String[] option : options) {

			if (OPTION_PROJECT_ROOT.equals(option[0]))
				doclet.setParameter(OPTION_PROJECT_ROOT, List.of(option[1]));

			else if (OPTION_LINK.equals(option[0]))
				doclet.registerLinks(option[1]);

			else if (OPTION_LINK_OFFLINE.equals(option[0]))
				doclet.registerOfflineLinks(option[1], option[2] + "/package-list");

			else if (OPTION_FAIL_ON_HTML_ERRORS.equals(option[0]))
				doclet.setParameter(OPTION_FAIL_ON_HTML_ERRORS, List.of(option[1]));

			else if (OPTION_FAIL_ON_MISSING_DOCS.equals(option[0]))
				doclet.setParameter(OPTION_FAIL_ON_MISSING_DOCS, List.of(option[1]));
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

	// ---------- Java 11 API

	private AbstractModuleDoclet fModuleDoclet = null;

	@Override
	public void init(Locale locale, Reporter reporter) {
		fModuleDoclet = new Jep221ModuleDoclet(reporter);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public Set<? extends jdk.javadoc.doclet.Doclet.Option> getSupportedOptions() {
		final HashSet<Option> options = new HashSet<>();

		options.add(new Option(OPTION_PROJECT_ROOT, "Root folder of the plugin (the folder containing the .project file)", "<path>", 1));
		options.add(new Option(OPTION_FAIL_ON_HTML_ERRORS, "Fail the documentation process when HTML comments are not well formed. Defaults to true.",
				"<boolean>", 1));
		options.add(new Option(OPTION_FAIL_ON_MISSING_DOCS,
				"Fail the documentation process when classes/methods/parameters do miss documentation. Defaults to true.", "<boolean>", 1));
		options.add(new Option(OPTION_LINK,
				"Link used classes to existing API documentation. Provide base URI of JavaDoc, eg https://docs.oracle.com/javase/8/docs/api/", "<URL>", 1));
		options.add(new Option(OPTION_LINK_OFFLINE,
				"Link used classes to existing API documentation using a given package file. Provide base URI of JavaDoc as first link, URI of package-list file as second link. Only the package-list file needs to be accessible during documentation creation.",
				"<URL, URL>", 2));

		return options;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_9;
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		((Jep221ModuleDoclet) fModuleDoclet).setDocletEnvironment(environment);
		return ((Jep221ModuleDoclet) fModuleDoclet).run();
	}

	private class Option implements jdk.javadoc.doclet.Doclet.Option {

		private final String fIdentifier;
		private final String fDescription;
		private final String fParameters;
		private final int fArgumentCount;

		public Option(String identifier, String description, String parameters, int argumentCount) {
			fIdentifier = identifier;
			fDescription = description;
			fParameters = parameters;

			fArgumentCount = argumentCount;
		}

		@Override
		public int getArgumentCount() {
			return fArgumentCount;
		}

		@Override
		public String getDescription() {
			return fDescription;
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return List.of(fIdentifier);
		}

		@Override
		public String getParameters() {
			return fParameters;
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			fModuleDoclet.setParameter(option, new ArrayList<>(arguments.subList(0, getArgumentCount())));
			return true;
		}
	}
}
