package org.apache.isis.core.runtime.contextmanger;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * @since 2.0.0-M3
 */
public interface ContextManager extends ManagedObjectResolver {

	ManagedObjectResolver resolverFor(ObjectSpecification spec);
	
}
