package org.apache.isis.core.runtime.contextmanger;

import java.net.URI;

import javax.enterprise.inject.Instance;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

/**
 * @since 2.0.0-M3
 */
public interface ManagedObjectResolver {

	/**
	 * Returns object identifier, which holds sufficient information for the framework to 
	 * retrieve a reference to given managedObject later.
	 * 
	 * @param managedObject
	 * @return identifier 
	 */
	URI identifierOf(ManagedObject managedObject);
	
	
	/**
	 * Retrieve a reference to the ManagedObject as identified by the identifier. 
	 * @param spec 
	 * @param identifier
	 * @return
	 */
	Instance<ManagedObject> resolve(ObjectSpecId spec, URI identifier);
	
}
