/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.runtime.services.persistsession;

import static org.apache.isis.commons.internal.base._With.requires;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.managers.ContextManager;
import org.apache.isis.core.runtime.system.context.managers.Converters;
import org.apache.isis.core.runtime.system.context.managers.ManagedObjectResolver;

import lombok.val;

@Singleton //TODO [2033] cleanup comments
public class ObjectAdapterServiceDefault implements ObjectAdapterService {


	@Override
	public ObjectAdapter adapterFor(Object pojo) {

		if(pojo == null) {
			return null;
		}

		val spec = specificationLoader.loadSpecification(pojo.getClass());
		switch (spec.getManagedObjectSort()) {
		case VALUE:
			return PojoAdapter.ofValue((Serializable) pojo);
		case VIEW_MODEL:
		case DOMAIN_SERVICE:
		case ENTITY:
			val managedObject = SimpleManagedObject.of(spec, pojo);
			val resolver = resolverFor(managedObject);
			if(resolver!=null) {
				return adapterFor(managedObject, resolver);
			}			
			throw _Exceptions.unexpectedCodeReach();
//			val fallback = ps().adapterFor(pojo);
//			return fallback;
		default:
			return PojoAdapter.ofTransient(pojo, spec.getSpecId());
		}

	}

	@Override
	public ObjectAdapter adapterForCollection(
			Object collectionPojo, 
			RootOid parentOid, 
			OneToManyAssociation oneToMany) {
		
        requires(parentOid, "parentOid");
        requires(collectionPojo, "collectionPojo");
		
		val collectionOid = Oid.Factory.parentedOfOneToMany(parentOid, oneToMany);
		
        // the List, Set etc. instance gets wrapped in its own adapter
        val newAdapter = PojoAdapter.of(collectionPojo, collectionOid); 
        return newAdapter;
	}

	@Override
	public ObjectAdapter adapterForViewModel(Object viewModelPojo, String mementoStr) {
		
		val objectSpecification = specificationLoader.loadSpecification(viewModelPojo.getClass());
        val objectSpecId = objectSpecification.getSpecId();
        val newRootOid = Oid.Factory.viewmodelOf(objectSpecId, mementoStr);
        val newAdapter = PojoAdapter.of(viewModelPojo, newRootOid); 
        return newAdapter; 
	}

	@Override
	public ObjectAdapter newTransientInstance(ObjectSpecification spec) {
		val pojo = spec.instantiatePojo();
        val newAdapter = PojoAdapter.ofTransient(pojo, spec.getSpecId());
        newAdapter.injectServices(serviceInjector);
        return newAdapter;
	}

	@Override //TODO [2033] instead we could create a UOID then ask the resolver 
	public ObjectAdapter adapterForViewModelMementoString(ObjectSpecification spec, String mementoStr) {
		val viewModelFacet = spec.getFacet(ViewModelFacet.class);
        if(viewModelFacet == null) {
            throw new IllegalArgumentException("spec does not have ViewModelFacet; spec is " + spec.getFullIdentifier());
        }
        
        val viewModelPojo = viewModelFacet.createViewModelPojo(spec, mementoStr, ObjectSpecification::instantiatePojo);
        val newRootOid = Oid.Factory.viewmodelOf(spec.getSpecId(), mementoStr);
        val newAdapter = PojoAdapter.of(viewModelPojo, newRootOid); 
        return newAdapter; 
	}

	// -- HELPER

//	protected PersistenceSession ps() {
//		return ofNullable(getIsisSessionFactory().getCurrentSession())
//				.map(IsisSession::getPersistenceSession)
//				.orElseThrow(()->new NonRecoverableException("No IsisSession on current thread."));
//	}
//
//	private IsisSessionFactory getIsisSessionFactory() {
//		return requireNonNull(isisSessionFactory, "IsisSessionFactory was not injected.");
//	}
	
	private ManagedObjectResolver resolverFor(ManagedObject managedObject) {
		val spec = managedObject.getSpecification();
		val managedObjectResolver = contextManager.resolverForIfAny(spec);
		return managedObjectResolver;
	}
	
	private RootOid oidFor(ManagedObject managedObject, ManagedObjectResolver resolver) {
     	val objectUri = resolver.uriOf(managedObject);
     	val converter = Converters.fromUriConverter();
     	return converter.toRootOid(objectUri);
     }
	
	private ObjectAdapter adapterFor(ManagedObject managedObject, ManagedObjectResolver resolver) {
		return PojoAdapter.of(managedObject.getPojo(), oidFor(managedObject, resolver));
	}

	//@Inject IsisSessionFactory isisSessionFactory;
	@Inject SpecificationLoader specificationLoader;
	@Inject ServiceInjector serviceInjector;
	@Inject ContextManager contextManager;


}
