/*
	Javascript utility code for configuration scripts.
*/

/*
	Import Java System class, so the System.setProperty function can be 
	called to set a Java system property.
*/
importClass(java.lang.System);

/*
 * Import those Mireka classes into the global Javascript namespace, which are
 * useful in a configuration.
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
importPackage(Packages.mireka.startup);
importPackage(Packages.mireka.submission);
importPackage(Packages.mireka.transmission);
importPackage(Packages.mireka.transmission.dsn);
importPackage(Packages.mireka.transmission.queue);
importPackage(Packages.mireka.transmission.queuing);
importPackage(Packages.mireka.transmission.immediate);

/*
 * Make the include function global. The include function reads and executes
 * another Javascript configuration file. It has a single parameter, the file
 * path. The path can be relative, in that case the parent directory is the
 * Mireka home directory.
 */
var include = configuration.include;

/*
 * Initializes an object by optionally creating a new instance, setting its
 * properties, injecting dependencies and registering it for automatic
 * startup/shutdown. If the bean parameter is a class name, then it will create
 * a new instance of this class. If the bean parameter is an object instance,
 * then it will set the properties of that JavaBean.
 * 
 * The optional content parameter is a Javascript object, intended to be
 * specified using the object literal syntax, of which attributes will be copied
 * into the specified by the bean argument.
 * 
 * Dependencies are injected into properties which were not explicitly specified
 * by the content parameter. Only those properties are considered whose setter
 * method is marked with the @Inject annotation. The objects available for
 * injection are the ones on which the setAsDefault method were called. The
 * injected object is selected by examining which is assignable to the setter
 * type. If the dependency cannot be fulfilled because neither of them is
 * assignable, or if the result is ambiguous, because more than one object is
 * suitable, then an exception will occur.
 * 
 * After the configuration script is completely executed, an optional start
 * function of these objects will be called in the order of their registration.
 * On shutdown an optional stop function of these objects are called in reversed
 * order. Start and stop methods must be annotated using the @PostConstruct and
 * @PreDestroy annotations.
 * 
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
	
	var assignedProperties = new Array();
	for(propertyName in content) {
		assignedProperties.push(propertyName);
	}
	configuration.injectMissingPropertyValues(object, assignedProperties);
	
	configuration.manage(object);
	return object;
}

/*
 * Makes the object available for injection. These object are used as default
 * values for those properties of the configuration objects that which are
 * marked with an @Inject annotation .
 */
function useAsDefault(object) {
	configuration.addInjectableObject(object);
	return object;
}

/*
 * Convenience function, sets up the configuration object and makes it available
 * for injection. The result is the same as calling setup and useAsDefault.
 */
function setupDefault(bean, content) {
	return useAsDefault(setup(bean, content));
}

/*
 * Returns an array which contains all elements of the arrays and all non-array
 * objects which were supplied in the argument list.
 */
function deepList() {
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

/*
 * Convenience function which creates a recipient - destination mapping using
 * the specified recipientSpecification and destination.
 */
function map(recipientSpecification, destination) {
	return setup(RecipientSpecificationDestinationPair, {
		recipientSpecification: recipientSpecification,
		destination: destination
	});
}

/*
 * Convenience function which creates a fully specified recipient address -
 * destination mapping.
 */
function mapAddress(address, destination) {
	return setup(RecipientDestinationPair, {
		recipient: address,
		destination: destination
	});
}

/*
 * Convenience function which creates a fully specified alias address -
 * canonical address mapping.
 */
function alias(aliasAddress, canonicalAddress) {
	return mapAddress(aliasAddress, setup(AliasDestination, {
		recipient: canonicalAddress
	}));
}

/*
 * Convenience function for creating a mail forwarding mapping. It has variable
 * number of parameters. The first parameter is the fully specified recipient
 * address. Mails sent to this address are forwarded to the fully specified
 * addresses given in the second, third etc. parameters.
 */
function forward() {
	var recipientAddress = arguments[0];
	var members = [];
	for (var i = 1; i < arguments.length; i++) {
		members.push(setup(Member, {
			address: arguments[i]
		}));
	}
	
	return setup(RecipientDestinationPair, {
		recipient: recipientAddress,
		destination: setup(ForwardDestination, {
			members: members
		})
	}); 
}	
	
/*
 * Convenience function which creates a mailing list and the corresponding
 * recipient-to-destination mapping in a single step.
 */
function mailingList(content) {
	return setup(ListMapper, {
		list: setup(ListDestination, content)
	});
}

/*
 Convenience function which maps a large set of recipient addresses to a backend 
 server where mails will be proxied to. 
*/ 
function massProxy(backendServer, recipientSpecifications) {
	return setup(RecipientSpecificationDestinationPair, {
		destination: setup(RelayDestination, {
			backendServer: backendServer
		}),
		recipientSpecifications: deepList(recipientSpecifications)
	});
}

