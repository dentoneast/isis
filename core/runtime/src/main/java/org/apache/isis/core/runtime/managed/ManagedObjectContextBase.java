package org.apache.isis.core.runtime.managed;

import java.util.stream.Stream;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;

/**
 * 
 * @since 2.0.0-M3
 *
 */
public abstract class ManagedObjectContextBase implements ManagedObjectContext {

	// -- FINAL FIELDS
	
    @Getter protected final IsisConfiguration configuration = IsisContext.getConfiguration();
    @Getter protected final ServiceInjector serviceInjector = IsisContext.getServiceInjector();
    @Getter protected final ServiceRegistry serviceRegistry = IsisContext.getServiceRegistry();
    @Getter protected final SpecificationLoader specificationLoader = IsisContext.getSpecificationLoader();
    @Getter protected final AuthenticationSession authenticationSession = 
    		IsisContext.getAuthenticationSession()
    		.orElse(null);
    
    // -- OBJECT ADAPTER SUPPORT
    
    @Override
    public Stream<ObjectAdapter> streamServiceAdapters() {
		return ps().streamServices();
	}
    
    @Override
    public ObjectAdapter adapterFor(Object pojo) {
		return ps().adapterFor(pojo);
	}
    
    // -- ENTITY SUPPORT
    
    @Override
    public ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec) {
		return ps().newTransientInstance(domainTypeSpec);
	}
	
    @Override
    public void makePersistentInTransaction(ObjectAdapter objectAdapter) {
		ps().makePersistentInTransaction(objectAdapter);
	}
    
    @Override
    public Object fetchPersistentPojoInTransaction(RootOid rootOid) {
		return ps().fetchPersistentPojoInTransaction(rootOid);
	}

    @Override
    public boolean isTransient(Object domainObject) {
		return ps().isTransient(domainObject);	
	}
    
    // -- PERSISTENCE SESSION FOR VIEWERS (rename?)
    
    private ManagedObjectPersistence ps() {
    	return persistenceSessionForViewers.get();
    }

    private final _Lazy<ManagedObjectPersistence> persistenceSessionForViewers = 
    		_Lazy.of(this::createPersistenceSessionForViewers);
    
    private ManagedObjectPersistence createPersistenceSessionForViewers() {
        return IsisContext.getPersistenceSession()
        		.map(ManagedObjectPersistence::of)
        		.orElse(null)
        		;
    }
    
    // --
	
}
