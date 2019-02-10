package org.apache.isis.core.runtime.contextmanger;

import java.util.Optional;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * @since 2.0.0-M3
 */
public interface ContextManager extends ManagedObjectResolver {

	Optional<ManagedObjectResolver> resolverFor(ObjectSpecification spec);
	
	default ManagedObjectResolver resolverForIfAny(ObjectSpecification spec) {
		return resolverFor(spec).orElse(null);
	}
	
}
