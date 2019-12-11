/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.eclipse.ease.helpgenerator.model.Category;
import org.eclipse.ease.helpgenerator.model.ModuleDefinition;

public abstract class AbstractModuleDoclet {

	protected static ModuleDefinition findModule(String className, Set<ModuleDefinition> modules) {
		for (final ModuleDefinition module : modules) {
			if (className.equals(module.getClassName()))
				return module;
		}

		return null;
	}

	public static String createHTMLFileName(final String moduleID) {
		return "module_" + escape(moduleID) + ".html";
	}

	private static String escape(final String data) {
		return data.replace(' ', '_').toLowerCase();
	}

	private final Map<String, List<String>> fParameters = new HashMap<>();
	private IReporter fReporter;

	private final LinkProvider fLinkProvider = new LinkProvider();

	public boolean run() {
		try {
			for (final ModuleDefinition module : getModules()) {
				final AbstractClassModel classModel = getClassModel(module);
				if (classModel != null)
					createHTMLFile(module, classModel);
				else
					getReporter().report(IReporter.WARNING, "No class definition found for module \"" + module.getName() + "\"");
			}

			if (hasModuleDefinitions()) {
				// create module TOC files
				final Set<String> tocFiles = createModuleTOCFiles();

				// update plugin.xml
				updatePluginXML(tocFiles);

				// update MANIFEST.MF
				updateManifest();

				// update build.properties
				updateBuildProperties();
			}

		} catch (final Exception e) {
			getReporter().report(IReporter.ERROR, e.getMessage());
			e.printStackTrace();
			return false;
		}

		return !getReporter().hasErrors();
	}

	private boolean hasModuleDefinitions() throws Exception {
		return !getModules().isEmpty() || !getCategories().isEmpty();
	}

	public void setParameter(String option, List<String> arguments) {
		if (V8ModuleDoclet.OPTION_LINK.equals(option))
			registerLinks(arguments.get(0));

		else if (V8ModuleDoclet.OPTION_LINK_OFFLINE.equals(option))
			registerOfflineLinks(arguments.get(0), arguments.get(1) + "/package-list");

		else
			fParameters.put(option, arguments);
	}

	public IReporter getReporter() {
		if (fReporter == null)
			fReporter = createReporter();

		return fReporter;
	}

	public Set<ModuleDefinition> getModules() throws Exception {
		final Set<ModuleDefinition> registeredModules = new HashSet<>();

		final IMemento document = getPluginDefinition();

		for (final IMemento extensionNode : document.getChildren("extension")) {
			if ("org.eclipse.ease.modules".equals(extensionNode.getString("point"))) {
				for (final IMemento instanceNode : extensionNode.getChildren("module"))
					registeredModules.add(new ModuleDefinition(instanceNode));
			}
		}

		return registeredModules;
	}

	public Set<Category> getCategories() {
		final Set<Category> categories = new HashSet<>();

		try {
			final IMemento document = getPluginDefinition();

			for (final IMemento extensionNode : document.getChildren("extension")) {
				if ("org.eclipse.ease.modules".equals(extensionNode.getString("point"))) {
					for (final IMemento instanceNode : extensionNode.getChildren("category"))
						categories.add(new Category(instanceNode));
				}
			}
		} catch (final Exception e) {
		}

		return categories;
	}

	public IMemento getPluginDefinition() throws Exception {
		final InputStreamReader reader = new InputStreamReader(new FileInputStream(getPluginOrFragmentFile()));
		final IMemento root = XMLMemento.createReadRoot(reader);
		reader.close();

		return root;
	}

	public File getProjectFile(String path, boolean shallExist) throws IOException {
		final File pluginDefinition = getRootFolder().toPath().resolve(path).toFile();
		if ((pluginDefinition.exists()) || !shallExist)
			return pluginDefinition;

		throw new IOException(path + " not found");
	}

	public File getPluginOrFragmentFile() throws IOException {
		try {
			return getProjectFile("plugin.xml", true);
		} catch (final Exception e) {
		}

		return getProjectFile("fragment.xml", true);
	}

	public File getBuildPropertiesFile() throws IOException {
		return getProjectFile("build.properties", true);
	}

	public File getManifestFile() throws IOException {
		return getProjectFile("META-INF/MANIFEST.MF", true);
	}

	public File getRootFolder() throws IOException {
		final List<String> parameter = fParameters.get(V8ModuleDoclet.OPTION_PROJECT_ROOT);
		if ((parameter == null) || (parameter.size() != 1))
			throw new IOException("Root folder not found, use " + V8ModuleDoclet.OPTION_PROJECT_ROOT + " parameter");

		final File rootFolder = new File(parameter.get(0));

		if (!rootFolder.exists())
			throw new FileNotFoundException("Root folder \"" + parameter.get(0) + "\"does not exist");

		if (!rootFolder.isDirectory())
			throw new IOException("Root folder is not a directory");

		return rootFolder;
	}

	public boolean failOnMissingDocs() {
		final List<String> parameter = fParameters.get(V8ModuleDoclet.OPTION_FAIL_ON_MISSING_DOCS);
		if ((parameter == null) || (parameter.size() != 1))
			return true;

		return Boolean.parseBoolean(parameter.get(0));
	}

	public boolean failOnHtmlErrors() {
		final List<String> parameter = fParameters.get(V8ModuleDoclet.OPTION_FAIL_ON_HTML_ERRORS);
		if ((parameter == null) || (parameter.size() != 1))
			return true;

		return Boolean.parseBoolean(parameter.get(0));
	}

	/**
	 * Verifies that the HTML content is well formed and correct. This guarantees that the code can be displayed in help hovers and code completion proposals.
	 *
	 * @throws Exception
	 *             when content is not well formed
	 */
	public void verifyXmlContent(String content, String className) {
		try {
			// try to read content into an XMLMemento
			XMLMemento.createReadRoot(new StringReader(content));
		} catch (final Exception e) {
			getReporter().reportInvalidHtml("invalid file content for " + className + ":\t" + e.getMessage());

		}
	}

	public void createHTMLFile(ModuleDefinition module, final AbstractClassModel classModel) throws IOException {
		fLinkProvider.setClassModel(classModel);

		final HtmlWriter htmlWriter = new HtmlWriter(module, classModel, fLinkProvider, getReporter());
		final String content = htmlWriter.createContents();

		verifyXmlContent(content, classModel.getClassName());

		if (!Files.exists(getProjectFile("help", false).toPath()))
			Files.createDirectory(getProjectFile("help", false).toPath());

		Files.write(getHtmlHelpFilePath(module.getId()), content.getBytes());
	}

	private Path getHtmlHelpFilePath(String moduleID) throws IOException {
		return getProjectFile("help/" + "module_" + escape(moduleID) + ".html", false).toPath();
	}

	public void updateBuildProperties() throws IOException {
		final File buildFile = getBuildPropertiesFile();

		final Properties properties = new Properties();
		properties.load(new FileInputStream(buildFile));
		final String property = properties.getProperty("bin.includes");
		if (!property.contains("help/")) {
			if (property.trim().isEmpty())
				properties.setProperty("bin.includes", "help/");
			else
				properties.setProperty("bin.includes", "help/," + property.trim());

			final FileOutputStream out = new FileOutputStream(buildFile);
			properties.store(out, "");
			out.close();
		}
	}

	public void updateManifest() throws IOException {
		final File manifestFile = getManifestFile();

		final Manifest manifest = new Manifest();
		manifest.read(new FileInputStream(manifestFile));

		final Attributes mainAttributes = manifest.getMainAttributes();
		final String require = mainAttributes.getValue("Require-Bundle");

		if ((require == null) || (require.isEmpty()))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.5.0,4.0.0)\"");

		else if (!require.contains("org.eclipse.help"))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.5.0,4.0.0)\"," + require);

		else
			// manifest contains reference to org.eclipse.help, bail out
			return;

		final FileOutputStream out = new FileOutputStream(manifestFile);
		manifest.write(out);
		out.close();
	}

	public Set<String> createModuleTOCFiles() throws Exception {
		final Map<String, IMemento> tocDefinitions = new HashMap<>();

		// create categories
		for (final Category category : getCategories()) {
			final XMLMemento memento = XMLMemento.createWriteRoot("toc");
			memento.putString("label", category.getName());
			memento.putString("link_to", category.getHelpLink());

			final IMemento topicNode = memento.createChild("topic");
			topicNode.putString("label", category.getName());
			topicNode.putBoolean("sort", true);

			topicNode.createChild("anchor").putString("id", "modules_anchor");
			tocDefinitions.put(category.getFileName(), memento);
		}

		// create modules
		for (final ModuleDefinition module : getModules()) {
			final String categoryID = module.getCategoryId();
			final String fileName = Category.createCategoryFileName(categoryID).replace("category_", "modules_");

			IMemento memento;
			if (tocDefinitions.containsKey(fileName))
				memento = tocDefinitions.get(fileName);

			else {
				memento = XMLMemento.createWriteRoot("toc");
				memento.putString("label", "Modules");
				memento.putString("link_to", Category.createCategoryLink(categoryID));

				tocDefinitions.put(fileName, memento);
			}

			final IMemento topicNode = memento.createChild("topic");
			topicNode.putString("href", "help/" + createHTMLFileName(module.getId()));
			topicNode.putString("label", module.getName());
		}

		for (final Entry<String, IMemento> entry : tocDefinitions.entrySet())
			Files.write(getProjectFile("help/" + entry.getKey(), false).toPath(), entry.getValue().toString().getBytes());

		return tocDefinitions.keySet();
	}

	public void updatePluginXML(final Collection<String> tocs) throws Exception {
		final HashSet<String> toDo = new HashSet<>(tocs);

		final IMemento memento = getPluginDefinition();
		for (final IMemento extensionNode : memento.getChildren("extension")) {
			final String extensionPoint = extensionNode.getString("point");
			if ("org.eclipse.help.toc".equals(extensionPoint)) {
				// a help topic is already registered
				for (final IMemento tocNode : extensionNode.getChildren("toc")) {
					final String tocLocation = tocNode.getString("file");
					if (tocLocation.length() > 5)
						toDo.remove(tocLocation.substring(5));
				}
			}
		}

		for (final String fileLocation : toDo) {
			// some TOCs not registered yet
			final IMemento extensionNode = memento.createChild("extension");
			extensionNode.putString("point", "org.eclipse.help.toc");
			final IMemento tocNode = extensionNode.createChild("toc");
			tocNode.putString("file", "help/" + fileLocation);
			tocNode.putBoolean("primary", false);
		}

		if (!toDo.isEmpty()) {
			final File pluginFile = getPluginOrFragmentFile();
			Files.write(pluginFile.toPath(), memento.toString().replace("&#x0A;", "\n").getBytes());
		}
	}

	public LinkProvider getLinkProvider() {
		return fLinkProvider;
	}

	protected void registerLinks(String url) {
		registerOfflineLinks(url, url + "/package-list");
	}

	protected void registerOfflineLinks(String remoteUrl, String packageContentUrl) {
		try {
			try (InputStream packageData = new URL(packageContentUrl).openStream()) {
				final List<String> packages = new BufferedReader(new InputStreamReader(packageData)).lines().collect(Collectors.toList());
				getLinkProvider().registerAddress(remoteUrl, packages);
			}

		} catch (final MalformedURLException e) {
			getReporter().report(IReporter.WARNING, "Cannot open location \"" + packageContentUrl + "\"");
		} catch (final IOException e) {
			getReporter().report(IReporter.WARNING, "Cannot read package data from \"" + packageContentUrl + "\"");
		}
	}

	protected abstract AbstractClassModel getClassModel(ModuleDefinition module);

	protected abstract IReporter createReporter();
}
