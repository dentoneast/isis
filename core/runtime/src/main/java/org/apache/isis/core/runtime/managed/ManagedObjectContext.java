package org.apache.isis.core.runtime.managed;

import java.util.stream.Stream;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;

/**
 * 
 * @since 2.0.0-M3
 *
 */
public interface ManagedObjectContext {

	AuthenticationSession getAuthenticationSession();
    IsisConfiguration getConfiguration();
    SpecificationLoader getSpecificationLoader();
    ServiceInjector getServiceInjector();
    ServiceRegistry getServiceRegistry();
    
	Stream<ObjectAdapter> streamServiceAdapters();
	ObjectAdapter lookupService(String serviceId);
	ObjectAdapter adapterForPojo(Object pojo);
	ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec);
	void makePersistentInTransaction(ObjectAdapter objectAdapter);
	Object fetchPersistentPojoInTransaction(RootOid rootOid);
	boolean isTransient(Object domainObject);
	
	void logoutAuthenticationSession();
	
}
