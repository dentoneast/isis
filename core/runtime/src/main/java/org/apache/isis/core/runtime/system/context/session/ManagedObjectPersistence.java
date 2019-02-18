package org.apache.isis.core.runtime.system.context.session;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.persistence.FixturesInstalledState;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * TODO [2033] only temporary for refactoring, ultimately to be removed or refined
 * <p>
 * Adds a layer of abstraction on top of the yet too complex PersistenceSession.
 * 
 * @since 2.0.0-M3
 *
 */
public interface ManagedObjectPersistence {

	// -- METHODS NEEDED BY RO-VIEWER ...
	
	ManagedObjectState stateOf(Object domainObject);
	FixturesInstalledState getFixturesInstalledState();
	
	//
	
	ObjectAdapter adapterOfPojo(Object pojo); //TODO [2033] use a ObjectAdapter providing service instead

	// 
	
	ObjectAdapter newTransientInstance(ObjectSpecification domainTypeSpec);
	void makePersistentInTransaction(ObjectAdapter objectAdapter);
	Object fetchPersistentPojoInTransaction(RootOid rootOid);
	
	// -- MEMENTO SUPPORT
	
    ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data);
	
	// -- FACTORIES
	
	//TODO [2033] this delegates to PersistenceSession but should rather delegate to a ManagedObjectManager
	
	static ManagedObjectPersistence of(PersistenceSession persistenceSession) {
		return new ManagedObjectPersistence() {
			
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
			public ObjectAdapter adapterOfPojo(Object pojo) {
				return persistenceSession.adapterFor(pojo);
			}
			
			@Override
			public ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data) {
				return persistenceSession.adapterOfMemento(spec, oid, data);
			}

			@Override
			public FixturesInstalledState getFixturesInstalledState() {
				return persistenceSession.getFixturesInstalledState();
			}


		};
	}
	
	
}
