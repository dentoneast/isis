package org.apache.isis.core.runtime.contextmanger;

import java.net.URI;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * @since 2.0.0-M3
 */
public interface ContextHandler extends ManagedObjectResolver {

	/**
	 * To support chain-of-responsibility pattern.
	 * @param objectSpec
	 * @return whether this manager sees itself responsible to manage objects represented by given objectSpec
	 */
	boolean recognizes(ObjectSpecification objectSpec);
	
	/**
	 * To support chain-of-responsibility pattern.
	 * @param uri
	 * @return whether this manager sees itself responsible to manage objects represented by given uri
	 */
	boolean recognizes(URI uri);
	

	
}
