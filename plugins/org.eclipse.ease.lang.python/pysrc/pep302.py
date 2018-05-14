import sys
import imp
 
class EASEImporter(object):
    """EASE module loader.

    Allows to use classic python import statements to load EASE modules.

    .. _PEP 302:
        https://www.python.org/dev/peps/pep-0302/#specification-part-1-the-importer-protocol
    """
            
    def find_module(self, fullname, path=None):
        """PEP 302 import finder.

        PEP 302 import finder to locate EASE modules. Will accept basically anything that starts with 'eclipse.'.

        Args:
            fullname (str): fully qualified name of the module
            path (str): None for a top-level module, or package.__path__ for submodules or subpackages

        Returns:
            self for EASE modules and paths, None in any other case
        """
        
        if (fullname == "eclipse") and (path == None):
            return self
            
        if fullname.startswith("eclipse."):
            return self

        return None
 
    def load_module(self, fullname):
        """PEP 302 import loader.

        PEP 302 import loader dynamically loads EASE modules into the provided python namespace.

        Args:
            fullname (str): fully qualified name of the module

        Returns:
            loaded module instance
        
        Raises:
            ImportError when the EASE module could not be located or loaded
        """
        
        if (self.is_module_path(fullname)):
            mod = sys.modules.setdefault(fullname, imp.new_module(fullname))
            mod.__file__ = "<%s>" % self.__class__.__name__
            mod.__loader__ = self
            mod.__path__ = []
            mod.__package__ = fullname
            return mod
       
        elif (self.is_module(fullname)):
            code = self.get_code(fullname)
            mod = sys.modules.setdefault(fullname, imp.new_module(fullname))
            mod.__file__ = "<%s>" % self.__class__.__name__
            mod.__loader__ = self
            mod.__package__ = fullname.rpartition('.')[0]
            exec(code, mod.__dict__)
            return mod
   
        raise ImportError("No EASE module named '" + fullname + "'")
        
    def is_module(self, fullname):
        """Verify that a FQN matches a module name.

        Args:
            fullname (str): fully qualified name of the module candidate

        Returns:
            True when fullname matches a module name
        """
        
        return org.eclipse.ease.lang.python.Pep302ModuleImporter.isModule(fullname)

    def is_module_path(self, fullname):
        """Verify that a FQN matches a module path.

        Args:
            fullname (str): fully qualified name of the path candidate

        Returns:
            True when fullname matches a package name
        """
        
        return org.eclipse.ease.lang.python.Pep302ModuleImporter.isModulePath(fullname)
        
    def get_code(self, fullname):
        """Create dynamic code parts for module..
        
        Generates dynamic code to access eclipse module.
        
        Args:
            fullname (str): fully qualified name of the module

        Returns:
            generated code as string
        """
        
        return org.eclipse.ease.lang.python.Pep302ModuleImporter.getCode(fullname, globals()["__EASE_MOD_org_eclipse_ease_modules_EnvironmentModule"]) 

# Add our import hook to sys.meta_path
sys.meta_path.append(EASEImporter())
