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

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

public class V9ModuleDoclet implements jdk.javadoc.doclet.Doclet {

	public static final String OPTION_PROJECT_ROOT = "-root";
	public static final String OPTION_LINK = "-link";
	public static final String OPTION_LINK_OFFLINE = "-linkoffline";
	public static final String OPTION_FAIL_ON_HTML_ERRORS = "-failOnHTMLError";
	public static final String OPTION_FAIL_ON_MISSING_DOCS = "-failOnMissingDocs";

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
