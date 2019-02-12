package org.apache.isis.core.runtime.system.context.session;

import java.util.stream.Stream;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * 
 * @since 2.0.0-M3
 *
 */
public interface ManagedObjectPersistence {

	// -- METHODS NEEDED BY RO-VIEWER ...
	
	Stream<ObjectAdapter> streamServices(); //TODO [2033] use ServiceRegistry instead
	ManagedObjectState stateOf(Object domainObject);
	ObjectAdapter lookupService(final String serviceId); //TODO [2033] use ServiceRegistry instead
	
	//
	
	ObjectAdapter adapterFor(Object pojo); //TODO [2033] use a ObjectAdapter providing service instead

	// 
	
	ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec);
	void makePersistentInTransaction(ObjectAdapter objectAdapter);
	Object fetchPersistentPojoInTransaction(RootOid rootOid);
	
	
	// -- FACTORIES
	
	//TODO [2033] this delegates to PersistenceSession but should rather delegate to a ManagedObjectManager
	
	static ManagedObjectPersistence of(PersistenceSession persistenceSession) {
		return new ManagedObjectPersistence() {
			
			@Override
			public Stream<ObjectAdapter> streamServices() {
				return persistenceSession.streamServices();
			}
			
			@Override
			public ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec) {
				return persistenceSession.newTransientInstance(domainTypeSpec);
			}
			
			@Override
			public void makePersistentInTransaction(ObjectAdapter objectAdapter) {
				persistenceSession.makePersistentInTransaction(objectAdapter);
			}
			
			@Override
			public ManagedObjectState stateOf(Object domainObject) {
				return persistenceSession.stateOf(domainObject);
			}
			
			@Override
			public Object fetchPersistentPojoInTransaction(RootOid rootOid) {
				return persistenceSession.fetchPersistentPojoInTransaction(rootOid);
			}
			
			@Override
			public ObjectAdapter adapterFor(Object pojo) {
				return persistenceSession.adapterFor(pojo);
			}

			@Override
			public ObjectAdapter lookupService(String serviceId) {
				return persistenceSession.lookupService(serviceId);
			}
		};
	}
	
}
