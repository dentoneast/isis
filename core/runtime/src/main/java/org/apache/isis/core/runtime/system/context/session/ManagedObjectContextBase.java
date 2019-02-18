package org.apache.isis.core.runtime.system.context.session;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.persistence.FixturesInstalledState;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
    
    @Override //FIXME [2033] decouple from JDO
    public ObjectAdapter adapterOfPojo(Object pojo) {
		return ps().adapterOfPojo(pojo);
	}
    
    @Override //FIXME [2033] decouple from JDO
    public ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data) {
		return ps().adapterOfMemento(spec, oid, data);
	}
 
    // -- FIXTURE SCRIPT STATE SUPPORT
    
    @Override //FIXME [2033] decouple from JDO
    public FixturesInstalledState getFixturesInstalledState() {
    	return ps().getFixturesInstalledState();
    }
    
    // -- HOMEPAGE LOOKUP SUPPORT
    
    @Override
    public Tuple2<ObjectAdapter, ObjectAction> findHomePageAction() {
    	val finderMixin = new ManagedObjectContextBase_findHomepage(this);
    	return finderMixin.findHomePageAction();
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
    public ManagedObjectState stateOf(Object domainObject) {
		return ps().stateOf(domainObject);	
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
