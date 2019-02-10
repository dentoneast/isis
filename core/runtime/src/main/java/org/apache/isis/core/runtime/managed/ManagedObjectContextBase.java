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
import lombok.RequiredArgsConstructor;

/**
 * 
 * @since 2.0.0-M3
 *
 */
@RequiredArgsConstructor
public abstract class ManagedObjectContextBase implements ManagedObjectContext {

	// -- FINAL FIELDS
	
    @Getter protected final IsisConfiguration configuration;
    @Getter protected final ServiceInjector serviceInjector;
    @Getter protected final ServiceRegistry serviceRegistry;
    @Getter protected final SpecificationLoader specificationLoader;
    @Getter protected final AuthenticationSession authenticationSession;
    
    // -- NO ARG CONSTRUCTOR
    
    protected ManagedObjectContextBase() {
    	configuration = IsisContext.getConfiguration();
        serviceInjector = IsisContext.getServiceInjector();
        serviceRegistry = IsisContext.getServiceRegistry();
        specificationLoader = IsisContext.getSpecificationLoader();
        authenticationSession = IsisContext.getAuthenticationSession().orElse(null);
    }
    
    // -- OBJECT ADAPTER SUPPORT
    
    @Override
    public Stream<ObjectAdapter> streamServiceAdapters() {
		return ps().streamServices();
	}
    
    @Override
    public ObjectAdapter adapterForPojo(Object pojo) {
		return ps().adapterFor(pojo);
	}
    
    @Override
	public ObjectAdapter lookupService(String serviceId) {
		return ps().lookupService(serviceId);
	}
    
    // -- AUTH
    
    @Override
    public void logoutAuthenticationSession() {
    	// we do the logout (removes this session from those valid)
        // similar code in wicket viewer (AuthenticatedWebSessionForIsis#onInvalidate())
        final AuthenticationSession authenticationSession = getAuthenticationSession();
        IsisContext.getAuthenticationManager().closeSession(authenticationSession);
        IsisContext.getSessionFactory().closeSession();	
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
    
    // -- PERSISTENCE SUPPORT FOR MANAGED OBJECTS
    
    private ManagedObjectPersistence ps() {
    	return managedObjectPersistence.get();
    }

    private final _Lazy<ManagedObjectPersistence> managedObjectPersistence = 
    		_Lazy.of(this::createPersistenceSessionForViewers);
    
    private ManagedObjectPersistence createPersistenceSessionForViewers() {
        return IsisContext.getPersistenceSession()
        		.map(ManagedObjectPersistence::of)
        		.orElse(null)
        		;
    }
    
    // --
	
}
