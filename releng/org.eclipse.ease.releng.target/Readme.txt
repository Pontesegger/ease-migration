tpd file handling
=================

	For target platforms use following eclipse plugin:
		https://github.com/mbarbero/fr.obeo.releng.targetplatform

	It resolves and sets *.tpd files.


Tycho naming
============
	
	The file named 'org.eclipse.ease.releng.target.target' will be used for tycho builds, so make sure to manually override this file after creating a new target file.


Available targets
=================

	* Luna.tpd - oldest platform to resolve plugin dependencies from
	* Mars.tpd
	* Neon.tpd
	* Oxygen.tpd - current build platform
	
	* Developers.tpd - target platform to be set by developers. Contains additional dependencies for modules, pydev, ...