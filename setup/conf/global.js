/*
	Javascript utility code for configuration scripts.
*/

/*
	Import Java System class, so the System.setProperty function can be 
	called to set a Java system property.
*/
importClass(java.lang.System);

/*
	Import those Mireka classes into the global Javascript namespace, which 
	are useful in a configuration. 
*/
importPackage(Packages.mireka);
importPackage(Packages.mireka.destination);
importPackage(Packages.mireka.filter);
importPackage(Packages.mireka.filter.misc);
importPackage(Packages.mireka.filter.dnsbl);
importPackage(Packages.mireka.filter.local);
importPackage(Packages.mireka.filter.local.table);
importPackage(Packages.mireka.filter.proxy);
importPackage(Packages.mireka.filter.spf);
importPackage(Packages.mireka.filterchain);
importPackage(Packages.mireka.forward);
importPackage(Packages.mireka.list);
importPackage(Packages.mireka.login);
importPackage(Packages.mireka.pop);
importPackage(Packages.mireka.pop.store);
importPackage(Packages.mireka.smtp);
importPackage(Packages.mireka.smtp.server);
importPackage(Packages.mireka.submission);
importPackage(Packages.mireka.transmission);
importPackage(Packages.mireka.transmission.dsn);
importPackage(Packages.mireka.transmission.queue);
importPackage(Packages.mireka.transmission.queuing);
importPackage(Packages.mireka.transmission.immediate);

/*
	Make the include function global. The include function reads and 
	executes another Javascript configuration file. It has a single 
	parameter, the file path. The path can be relative, in that case the 
	parent directory is the Mireka home directory.
*/
var include = mireka.include;

/*
	Initializes an object by optionally creating a new instance, setting
	its properties, and registering it for automatic startup/shutdown.
	If the bean parameter is a class name, then it will create a new 
	instance of this class. If the bean parameter is an object instance, 
	then it will set the properties of that JavaBean.
	The optional content parameter is a Javascript object, intended to be 
	specified using the object literal syntax, of which attributes will be 
	copied into the specified by the bean argument.

	After the configuration script is completely executed, an optional start 
	function of these objects will be called in the order of their 
	registration. On shutdown an optional stop function of these objects are 
	called in reversed order. Start and stop methods must be annotated using 
	the @PostConstruct and @PreDestroy annotations.

*/
function setup(bean, content) {
	var object;
	if(typeof bean == "function") {
		var beanString = bean + "";
		var className = beanString.substring(11, beanString.length - 1);
		object = eval("(new Packages." + className + ")");
	} else {
		object = bean;
	}
	for(propertyName in content) {
		try {
			object[propertyName] = content[propertyName];
		} catch (ex) {
			if(ex.rhinoException && ex.rhinoException != null) {
				ex.rhinoException.printStackTrace();
			}			
			var typeName = typeof bean == "function" ? bean + "." : "";
			ex.message = "Error when setting property " + typeName + 
				propertyName + ". " + ex.message;
			throw ex;
		}
	}
	mireka.manage(object);
	return object;
}

/* 
	Returns an array which contains all elements of the arrays and all 
	non-array objects which were supplied in the argument list. 
*/
function list() {
	var result = [];
	for (var i = 0; i < arguments.length; i++) {
		var argument = arguments[i];
		if (Array.isArray(argument))
			result = result.concat(argument);
		else 
			result.push(argument);
	}
	return result;
}

