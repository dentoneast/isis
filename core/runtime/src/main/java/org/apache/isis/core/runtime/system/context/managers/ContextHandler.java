package org.apache.isis.core.runtime.system.context.managers;

import java.net.URI;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * @since 2.0.0-M3
 */
public interface ContextHandler extends ManagedObjectResolver {

	/**
	 * To support chain-of-responsibility pattern.
	 * @param spec
	 * @return whether this manager sees itself responsible to manage objects represented by given spec
	 */
	boolean recognizes(ObjectSpecification spec);
	
	/**
	 * To support chain-of-responsibility pattern.
	 * @param objectUri
	 * @return whether this manager sees itself responsible to manage objects represented by given objectUri
	 */
	boolean recognizes(URI objectUri);
	

	
}
