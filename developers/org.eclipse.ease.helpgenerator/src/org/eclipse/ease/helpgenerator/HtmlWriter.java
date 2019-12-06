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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.eclipse.ease.helpgenerator.model.Description;
import org.eclipse.ease.helpgenerator.model.ExceptionValue;
import org.eclipse.ease.helpgenerator.model.Field;
import org.eclipse.ease.helpgenerator.model.Method;
import org.eclipse.ease.helpgenerator.model.ModuleDefinition;
import org.eclipse.ease.helpgenerator.model.Parameter;
import org.eclipse.ease.helpgenerator.model.ScriptExample;

public class HtmlWriter {

	private static final String LINE_DELIMITER = "\n";

	private static void addText(final StringBuffer buffer, final Object text) {
		buffer.append(text);
	}

	private static void addLine(final StringBuffer buffer, final Object text) {
		buffer.append(text).append(LINE_DELIMITER);
	}

	private static String escapeText(String text) {
		return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	private final IReporter fReporter;
	private final ModuleDefinition fModule;
	private final AbstractClassModel fClassModel;
	private final LinkProvider fLinkProvider;

	public HtmlWriter(ModuleDefinition module, AbstractClassModel classModel, LinkProvider linkProvider, IReporter reporter) {
		fModule = module;
		fClassModel = classModel;
		fLinkProvider = linkProvider;
		fReporter = reporter;
	}

	protected IReporter getReporter() {
		return fReporter;
	}

	private String getModuleName() {
		return fModule.getName();
	}

	public String createContents() {
		final StringBuffer buffer = new StringBuffer();

		addLine(buffer, "<html>");
		addLine(buffer, "<head>");
		addLine(buffer, "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
		addLine(buffer, "	<link rel=\"stylesheet\" type=\"text/css\" href=\"../../org.eclipse.ease.help/help/css/modules_reference.css\" />");
		addLine(buffer, "</head>");
		addLine(buffer, "<body>");
		addText(buffer, "	<div class=\"module\" title=\"");
		addText(buffer, getModuleName());
		addLine(buffer, " Module\">");

		// header
		addText(buffer, "		<h1>");
		addText(buffer, getModuleName());
		addLine(buffer, " Module</h1>");

		// class description
		addText(buffer, "		<p class=\"description\">");
		final Description classComment = fClassModel.getClassDocumentation();
		if (!classComment.isEmpty())
			addText(buffer, classComment);
		else
			getReporter().reportMissingDocs("Missing class comment for " + fClassModel.getClassName());

		addLine(buffer, "</p>");

		// dependencies
		addLine(buffer, createDependenciesSection());

		// end title div
		addLine(buffer, "	</div>");

		// constants
		addLine(buffer, createConstantsSection());

		// function overview
		addLine(buffer, createOverviewSection());

		// function details
		addLine(buffer, createDetailSection());

		addLine(buffer, "</body>");
		addLine(buffer, "</html>");

		return fLinkProvider.insertLinks(buffer.toString());
	}

	private StringBuffer createDependenciesSection() {

		final StringBuffer buffer = new StringBuffer();
		if (fModule.hasDependencies()) {

			addLine(buffer, "\t<h3>Dependencies</h3>");
			addLine(buffer, "\t<p>This module depends on following other modules which will automatically be loaded.</p>");
			addLine(buffer, "\t<ul class=\"dependency\">");

			for (final ModuleDefinition dependency : fModule.getDependencies())
				addLine(buffer, "\t\t<li>{@module " + dependency.getId() + "}</li>");

			addLine(buffer, "\t</ul>");
		}

		return buffer;
	}

	private StringBuffer createDetailSection() {
		final StringBuffer buffer = new StringBuffer();

		addLine(buffer, "\t<h2>Methods</h2>");

		for (final Method method : fClassModel.getExportedMethods()) {
			// heading
			addText(buffer, "\t<div class=\"command");
			if (method.isDeprecated())
				addText(buffer, " deprecated");
			addText(buffer, "\" data-method=\"");
			addText(buffer, method.getName());
			addLine(buffer, "\">");

			addLine(buffer, "\t\t<h3" + (method.isDeprecated() ? " class=\"deprecatedText\"" : "") + "><a id=\"" + method.getName() + "\">" + method.getName()
					+ "</a></h3>");

			// synopsis
			addLine(buffer, createSynopsis(method));

			// main description
			addLine(buffer, "\t\t<p class=\"description\">" + method.getComment() + "</p>");
			if (method.getComment().isEmpty())
				getReporter().reportMissingDocs("Missing comment for " + fClassModel.getClassName() + "." + method.getName() + "()");

			if (method.isDeprecated()) {
				String deprecationText = method.getDeprecationMessage();
				if (deprecationText.isEmpty())
					deprecationText = "This method is deprecated and might be removed in future versions.";

				addLine(buffer, "\t\t<p class=\"warning\"><b>Deprecation warning:</b> " + deprecationText + "</p>");
			}

			// aliases
			addLine(buffer, createAliases(method));

			// parameters
			addLine(buffer, createParametersArea(method));

			// return value
			addLine(buffer, createReturnValueArea(method));

			// declared exceptions
			addLine(buffer, createExceptionArea(method));

			// examples
			addLine(buffer, createExampleArea(method));

			addLine(buffer, "\t</div>");
		}

		return buffer;
	}

	private StringBuffer createExampleArea(final Method method) {
		final StringBuffer buffer = new StringBuffer();

		if (!method.getExamples().isEmpty()) {
			addLine(buffer, "		<dl class=\"examples\">");

			for (final ScriptExample example : method.getExamples()) {
				addLine(buffer, "			<dt>" + example.getCode() + "</dt>");
				addText(buffer, "			<dd class=\"description\">" + example.getComment());
				addLine(buffer, "           </dd>");
			}

			addLine(buffer, "		</dl>");
		}

		return buffer;
	}

	private StringBuffer createReturnValueArea(final Method method) {
		final StringBuffer buffer = new StringBuffer();

		if (!method.getReturnType().isVoid()) {
			addText(buffer, "		<p class=\"return\">");

			final Description comment = method.getReturnType().getComment();
			if (comment.isEmpty())
				getReporter().reportMissingDocs("Missing return statement documentation for " + fClassModel.getClassName() + "." + method.getName() + "()");
			else
				addText(buffer, comment);

			addLine(buffer, "</p>");
		}

		return buffer;
	}

	private StringBuffer createParametersArea(final Method method) {
		final StringBuffer buffer = new StringBuffer();

		if (!method.getParameters().isEmpty()) {

			addLine(buffer, "		<dl class=\"parameters\">");

			for (final Parameter parameter : method.getParameters()) {
				addLine(buffer, "			<dt>" + parameter.getName() + "</dt>");
				addText(buffer, "			<dd class=\"description\" data-parameter=\"" + parameter.getName() + "\">" + parameter.getComment());
				if (parameter.getComment().isEmpty())
					getReporter().reportMissingDocs(
							"Missing parameter documentation for " + fClassModel.getClassName() + "." + method.getName() + "(" + parameter.getName() + ")");

				String defaultValue = parameter.getDefaultValue();
				if (defaultValue != null) {
					addText(buffer, "<span class=\"optional\"><b>Optional:</b> defaults to &lt;<i>");

					if ((!String.class.getName().equals(parameter.getTypeName())) && (defaultValue.length() > 2))
						// remove quotes from default value
						defaultValue = defaultValue.substring(1, defaultValue.length() - 1);

					if (defaultValue.contains("org.eclipse.ease.modules.ScriptParameter.null"))
						addText(buffer, "null");
					else
						addText(buffer, escapeText(defaultValue));

					addText(buffer, "</i>&gt;.</span>");
				}
				addLine(buffer, "</dd>");
			}
			addLine(buffer, "		</dl>");
		}

		return buffer;
	}

	private StringBuffer createExceptionArea(final Method method) {
		final StringBuffer buffer = new StringBuffer();

		if (!method.getExceptions().isEmpty()) {
			addLine(buffer, "		<dl class=\"exceptions\">");

			for (final Parameter exception : method.getExceptions()) {
				addLine(buffer, "			<dt>{@link " + exception.getName() + "}</dt>");
				addText(buffer, "			<dd class=\"description\" data-exception=\"" + exception.getName() + "\">" + exception.getComment());
				addLine(buffer, "           </dd>");

				if (exception.getComment().isEmpty())
					getReporter().reportMissingDocs(
							"Missing exception documentation for " + fClassModel.getClassName() + "." + method.getName() + "() - " + exception.getName());
			}

			addLine(buffer, "		</dl>");
		}

		return buffer;
	}

	private StringBuffer createAliases(final Method method) {
		final StringBuffer buffer = new StringBuffer();

		final Collection<String> aliases = method.getAliases();
		if (!aliases.isEmpty()) {
			addLine(buffer, "		<p class=\"synonyms\"><em>Alias:</em>");

			for (final String alias : aliases)
				addText(buffer, " " + alias + "(),");

			// remove last comma
			buffer.deleteCharAt(buffer.length() - 1);

			addLine(buffer, "</p>");
		}

		return buffer;
	}

	private StringBuffer createSynopsis(final Method method) {
		final StringBuffer buffer = new StringBuffer();

		addText(buffer, "		<p class=\"synopsis\">");
		addText(buffer, "{@link " + method.getReturnType().getTypeName() + "}");
		addText(buffer, " ");
		addText(buffer, method.getName());
		addText(buffer, "(");
		for (final Parameter parameter : method.getParameters()) {
			final String defaultValue = parameter.getDefaultValue();
			if (defaultValue != null)
				addText(buffer, "<i>[");

			addText(buffer, "{@link " + parameter.getTypeName() + "}");
			addText(buffer, " ");
			addText(buffer, parameter.getName());
			if (defaultValue != null)
				addText(buffer, "]</i>");

			addText(buffer, ", ");
		}
		if (!method.getParameters().isEmpty())
			buffer.delete(buffer.length() - 2, buffer.length());

		addText(buffer, ")");

		final List<ExceptionValue> exceptions = method.getExceptions();
		if (!exceptions.isEmpty()) {
			addText(buffer, " ");
			addText(buffer, exceptions.stream().map(e -> "{@link " + e.getTypeName() + "}").collect(Collectors.joining(", ")));
		}

		addLine(buffer, "</p>");

		return buffer;
	}

	private StringBuffer createOverviewSection() {
		final StringBuffer buffer = new StringBuffer();

		addLine(buffer, "	<h2>Method Overview</h2>");
		addLine(buffer, "	<table class=\"functions\">");
		addLine(buffer, "		<tr>");
		addLine(buffer, "			<th>Method</th>");
		addLine(buffer, "			<th>Description</th>");
		addLine(buffer, "		</tr>");

		final List<Overview> overview = new ArrayList<>();

		for (final Method method : fClassModel.getExportedMethods()) {
			overview.add(new Overview(method.getName(), method.getName(), method.getComment(), method.isDeprecated()));
			for (final String alias : method.getAliases())
				overview.add(new Overview(alias, method.getName(),
						new Description("Alias for <a href=\"#" + method.getName() + "\">" + method.getName() + "()</a>."), method.isDeprecated()));
		}

		Collections.sort(overview);

		for (final Overview entry : overview) {
			addLine(buffer, "		<tr>");
			if (!entry.fDeprecated) {
				addLine(buffer, "			<td><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a>()</td>");
				addLine(buffer, "			<td>" + entry.fDescription.getFirstSentence() + "</td>");

			} else {
				addLine(buffer, "			<td class=\"deprecatedText\"><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a>()</td>");
				addLine(buffer, "			<td class=\"deprecatedDescription\"><b>Deprecated:</b> " + entry.fDescription.getFirstSentence() + "</td>");
			}
			addLine(buffer, "		</tr>");
		}

		addLine(buffer, "	</table>");
		addLine(buffer, "");

		return buffer;
	}

	private StringBuffer createConstantsSection() {
		final StringBuffer buffer = new StringBuffer();

		final List<Field> fields = fClassModel.getExportedFields();
		Collections.sort(fields, (o1, o2) -> o1.getName().compareTo(o2.getName()));

		if (!fields.isEmpty()) {
			addLine(buffer, "");
			addLine(buffer, "	<h2>Constants</h2>");
			addLine(buffer, "	<table class=\"constants\">");
			addLine(buffer, "		<tr>");
			addLine(buffer, "			<th>Constant</th>");
			addLine(buffer, "			<th>Description</th>");
			addLine(buffer, "		</tr>");

			for (final Field field : fields) {
				addLine(buffer, "\t\t<tr>");

				if (field.getComment().isEmpty())
					getReporter().reportMissingDocs("Field documentation missing for " + fModule.getClassName() + "." + field.getName());

				if (!field.isDeprecated()) {
					addLine(buffer, " <td><a id=\"" + field.getName() + "\">" + field.getName() + "</a></td>");
					addLine(buffer, " <td class=\"description\" data-field=\"" + field.getName() + "\">" + field.getComment() + "</td>");

				} else {
					addLine(buffer, " <td><a id=\"" + field.getName() + "\" class=\"deprecatedText\">" + field.getName() + "</a></td>");
					addText(buffer, " <td>" + field.getComment());
					String deprecationText = field.getDeprecationMessage();
					if (deprecationText.isEmpty())
						deprecationText = "This constant is deprecated and might be removed in future versions.";

					addText(buffer, " <div class=\"warning\"><b>Deprecation warning:</b> " + deprecationText + "</div>");
					addLine(buffer, "</td>");
				}

				addLine(buffer, " </tr>");
			}

			addLine(buffer, "	</table>");
			addLine(buffer, "");
		}

		return buffer;
	}

	private class Overview implements Comparable<Overview> {
		private final String fTitle;
		private final String fLinkID;
		private final Description fDescription;
		private final boolean fDeprecated;

		public Overview(final String title, final String linkID, final Description description, final boolean deprecated) {
			fTitle = title;
			fLinkID = linkID;
			fDescription = description;
			fDeprecated = deprecated;
		}

		@Override
		public int compareTo(final Overview arg0) {
			return fTitle.compareTo(arg0.fTitle);
		}
	};
}
