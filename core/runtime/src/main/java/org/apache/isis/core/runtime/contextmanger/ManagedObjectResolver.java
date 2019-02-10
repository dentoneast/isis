package org.apache.isis.core.runtime.contextmanger;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.apache.isis.core.metamodel.adapter.oid.UniversalOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

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
	 * @param specId 
	 * @param identifier
	 * @return
	 */
	Instance<ManagedObject> resolve(ObjectSpecId specId, URI identifier);
	
	
	/**
	 * convenient shortcut
	 * 
	 * @param universalOid
	 * @return reference to the ManagedObject as identified by the universalOid
	 */
	default Instance<ManagedObject> resolve(UniversalOid universalOid) {
		val spec = universalOid.getObjectSpecId();
		return resolve(spec, universalOid.universalId());
	}


	/**
	 * Returns the authority parts required to make an object identifier (URI)  
	 * 
	 * @param spec
	 * @return
	 */
	Optional<AuthorityDescriptor> authorityFor(ObjectSpecification spec);
	
}
