package org.apache.isis.core.plugins.ioc;

public interface RequestContextHandle extends AutoCloseable {

	/**
	 * Refined to not throw a catched exception
	 */
	@Override void close();
	
}
