/**
 * Quickly create projects according to styleguide.
 *
 * create a plug-in:
 * 		new Setup().createPlugin("org.eclipse.skills.myProject", "My Project")
 *
 * create a feature:
 * 		new Setup().createFeature("org.eclipse.skills.my.feature", "My Feature")
 *
 * add nature to all java projects:
 *		new Setup().updateNature("nature.id")
 *
 * copy file to all java projects:
 *		new Setup().copyTemplate("workspace://some.project/source.file", "/path/within/target/project/target.file")
 */
loadModule("/System/Resources");

function Setup() {
	this.replacements = {};

	// project defaults
	this.addReplacement("${releng.name}", "Eclipse EASE");
	this.addReplacement("${java.version}", "JavaSE-1.8");
	this.addReplacement("${bundle.version}", "0.9.0");
	this.addReplacement("${bundle.vendor}", "Eclipse.org");
	this.addReplacement("${project.root.id}", "org.eclipse.ease");

	this.addReplacement("${releng.project}", "org.eclipse.ease.releng");	
	this.addReplacement("${help.project}", "org.eclipse.ease.help");	
}

Setup.prototype.createFeature = function(featureId, featureName) {
	if (typeof (featureName) === 'undefined')
		featureName = featureId;

	this.log("Create feature: " + featureId + " ...");

	this.addReplacement("${bundle.id}", featureId);
	this.addReplacement("${bundle.name}", featureName);

	var project = createProject(featureId);
	this.addNature(project, "org.eclipse.pde.FeatureNature");

	this.createFiles(project, "workspace://${releng.project}/templates/feature");
	this.log("\tdone!");
}

Setup.prototype.createPlugin = function(pluginId, pluginName) {
	if (typeof (pluginName) === 'undefined')
		pluginName = pluginId;

	this.log("Create plug-in: " + pluginId + " ...");

	//	create plugin
	this.addReplacement("${bundle.id}", pluginId);
	this.addReplacement("${bundle.name}", pluginName);

	this.internalCreatePlugin(pluginId, pluginName, "workspace://${releng.project}/templates/plugin");

	// add to master pom
	this.replaceInFile("workspace://${releng.location}/pom.xml", "<!-- plugin insertion point -->", "<module>../../plugins/${bundle.id}</module>\n\t\t<!-- plugin insertion point -->");
	this.replaceInFile("workspace://${releng.location}.coverage/pom.xml", "<!-- plugin insertion point -->", "<dependency>\n\t\t\t<groupId>${project.root.id}</groupId>\n\t\t\t<artifactId>${bundle.name}</artifactId>\n\t\t\t<version>${bundle.version}-SNAPSHOT</version>\n\t\t\t<scope>compile</scope>\n\t\t</dependency>\n\t\t<!-- plugin insertion point -->");
	this.appendToFile("workspace://${help.project}/build.properties", ",\\\n\t\t\t\t\t\tplatform:/plugin/${bundle.name}");

	// create test fragment
	this.createTestFragment(pluginId, pluginName);
}

Setup.prototype.createTestFragment = function(pluginId, pluginName) {
	if (typeof (pluginName) === 'undefined')
		pluginName = pluginId;

	// create test fragment
	this.log("Create test plug-in: " + pluginId + ".test" + " ...");

	pluginId += ".test";
	pluginName += " Unit Tests";
	
	this.addReplacement("${bundle.id}", pluginId);
	this.addReplacement("${bundle.name}", pluginName);
	this.addReplacement("${bundle.host.id}", pluginId);

	this.internalCreatePlugin(pluginId, pluginName, "workspace://${releng.project}/templates/plugin-test");
	
	// add to master pom
	this.replaceInFile("workspace://${releng.project}/pom.xml", "<!-- test insertion point -->", "<module>../../tests/${bundle.id}</module>\n\t\t<!-- test insertion point -->");
	this.replaceInFile("workspace://${releng.project}.coverage/pom.xml", "<!-- test insertion point -->", "<dependency>\n\t\t\t<groupId>${project.root.id}</groupId>\n\t\t\t<artifactId>${bundle.id}</artifactId>\n\t\t\t<version>${bundle.version}-SNAPSHOT</version>\n\t\t\t<scope>test</scope>\n\t\t</dependency>\n\t\t<!-- test insertion point -->");
}

Setup.prototype.internalCreatePlugin = function(pluginId, pluginName, templateFolder) {
	var project = createProject(pluginId);

	// to plugin project
	this.addNature(project, "org.eclipse.jdt.core.javanature");
	this.addNature(project, "org.eclipse.pde.PluginNature");

	createFolder("workspace://" + project.getName() + "/src");

	// checkers
	this.addNature(project, "ch.acanda.eclipse.pmd.builder.PMDNature");
	this.addNature(project, "edu.umd.cs.findbugs.plugin.eclipse.findbugsNature");
	this.addNature(project, "net.sf.eclipsecs.core.CheckstyleNature");

	this.createFiles(project, templateFolder);
}

Setup.prototype.createFiles = function(project, templateFolder) {
	var templateFolder = new java.lang.String(this.replaceText(templateFolder));
	var prefix = templateFolder.substring(templateFolder.indexOf("://") + 3);

	var templates = findFiles("*", templateFolder, true);
	for (index in templates) {
		var sourceFile = templates[index];
		var index = sourceFile.getFullPath().toString().indexOf(prefix);
		var targetFile = "workspace://" + project.getName() + sourceFile.getFullPath().toString().substring(index + prefix.length());

		this.log("\tcreate " + targetFile)
		
		var content = readFile(sourceFile);
		var handle = writeFile(targetFile, this.replaceText(content));
		closeFile(handle);
	}
}

Setup.prototype.addNature = function(project, natureId) {
	var description = project.getDescription();
	if (!description.hasNature(natureId)) {
		this.log("\tadd nature " + natureId);

		var natures = description.getNatureIds();
		var newNatures = java.util.Arrays.copyOf(natures, natures.length + 1);
		newNatures[natures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
}

Setup.prototype.log = function(message) {
	print(message);
}

Setup.prototype.addReplacement = function(key, value) {
	this.replacements[key] = value;
}

Setup.prototype.replaceInFile = function(file, search, replacement) {
	var resolvedFile = this.replaceText(file)
	var content = readFile(resolvedFile);
	var handle = writeFile(resolvedFile, content.replace(search, this.replaceText(replacement)));
	closeFile(handle);
}

Setup.prototype.appendToFile = function(file, text) {
	var resolvedFile = this.replaceText(file)
	var content = readFile(resolvedFile);
	var handle = writeFile(resolvedFile, content + this.replaceText(text));
	closeFile(handle);
}

Setup.prototype.replaceText = function(text) {
	var processed = new java.lang.String(text);
	for (key in this.replacements)
		processed = processed.replaceAll(java.util.regex.Pattern.quote(key), this.replacements[key]);

	return processed;
}

Setup.prototype.updateNature = function(natureId) {
	var projects = getWorkspace().getProjects();
	for (var index = 0; index < projects.length; index++) {
		var project = projects[index];
		if (this.isJavaProject(project))
			this.addNature(project, natureId);
	}
}

Setup.prototype.copyTemplate = function(source, targetRelativeToProject) {
	var projects = getWorkspace().getProjects();
	for (var index = 0; index < projects.length; index++) {
		var project = projects[index];
		if (this.isJavaProject(project))
			copyFile(source, "workspace://" + project.getName() + "/" + targetRelativeToProject);
	}
}

Setup.prototype.isJavaProject = function(project) {
	return  (project.getDescription().hasNature("org.eclipse.jdt.core.javanature"));
}

'create plugin:      new Setup().createPlugin("org.eclipse.ease.myplugin", "plugin name")\n' + 
'create test plugin: new Setup().createTestFragment("org.eclipse.ease.myplugin", "plugin name")'