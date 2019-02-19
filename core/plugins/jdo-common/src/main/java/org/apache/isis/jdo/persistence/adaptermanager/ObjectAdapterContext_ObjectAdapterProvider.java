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
package org.apache.isis.jdo.persistence.adaptermanager;

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.adapter.OidFactory;
import org.apache.isis.core.runtime.persistence.adapter.OidFactory.OidProvider2;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.val;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapterProvider implementation
 * </p> 
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_ObjectAdapterProvider implements ObjectAdapterProvider {
    
	private final static _Probe probe = _Probe.unlimited().label("ObjectAdapterContext_ObjectAdapterProvider");
    
    private final ObjectAdapterContext objectAdapterContext;
//    private final PersistenceSession persistenceSession;
    private final ServiceInjector serviceInjector;
    private final SpecificationLoader specificationLoader; 
    private final OidFactory oidFactory; 
    private final OidProvider2 oidProviderForManagedContexts = 
    		new ObjectAdapterContext_OidProviders.OidForManagedContexts();
    
    ObjectAdapterContext_ObjectAdapterProvider(
            ObjectAdapterContext objectAdapterContext,
            MetaModelContext metaModelContext,
            PersistenceSession persistenceSession) {
        
        this.objectAdapterContext = objectAdapterContext;
//        this.persistenceSession = persistenceSession;
        this.serviceInjector = persistenceSession.getServiceInjector();
        this.specificationLoader = metaModelContext.getSpecificationLoader();
        
        this.oidFactory = OidFactory.builder(pojo->specificationLoader.loadSpecification(pojo.getClass()))
                .add(new ObjectAdapterContext_OidProviders.GuardAgainstRootOid())
                .add(new ObjectAdapterContext_OidProviders.OidForServices())
                .add(new ObjectAdapterContext_OidProviders.OidForValues())
                .add(new ObjectAdapterContext_OidProviders.OidForViewModels())
//                .add(new ObjectAdapterContext_OidProviders.OidForPersistent())
                .add(new ObjectAdapterContext_OidProviders.OidForOthers())
                .build();
    }

    @Override
    public ObjectAdapter adapterFor(Object pojo) {

        if(pojo == null) {
            return null;
        }
        
        val spec = specificationLoader.loadSpecification(pojo.getClass());
        val managedObject = SimpleManagedObject.of(spec, pojo);
        
        val resolver = oidProviderForManagedContexts.resolverFor(managedObject);
        if(resolver!=null) {
        	return oidProviderForManagedContexts.adapterFor(managedObject, resolver);
        }
        
        probe.println("[2033] we should not need to fall through here any longer");
        
        final RootOid rootOid = oidFactory.oidFor(pojo);
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories().createRootAdapter(pojo, rootOid);
        return objectAdapterContext.injectServices(newAdapter);
    }
    
    @Override
    public ObjectAdapter adapterForCollection(Object pojo, RootOid parentOid, OneToManyAssociation collection) {

        requires(parentOid, "parentOid");
        requires(collection, "collection");

        // the List, Set etc. instance gets wrapped in its own adapter
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories()
                .createCollectionAdapter(pojo, parentOid, collection);

        return objectAdapterContext.injectServices(newAdapter);
    }
    
    @Override
    public ManagedObject disposableAdapterForViewModel(final Object viewModelPojo) {
        return ManagedObject.of(()->specificationLoader.loadSpecification(viewModelPojo.getClass()), viewModelPojo);
    }

    @Override
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, String mementoString) {
        return objectAdapterContext.adapterForViewModel(viewModelPojo, mementoString);
    }
    
    // -- DOMAIN OBJECT CREATION SUPPORT
    
    @Override
    public ObjectAdapter newTransientInstance(ObjectSpecification objectSpec) {
        return objectAdapterContext.objectCreationMixin.newInstance(objectSpec);
    }
    
    @Override
    public ObjectAdapter recreateViewModelInstance(ObjectSpecification objectSpec, final String memento) {
        return objectAdapterContext.objectCreationMixin.recreateInstance(objectSpec, memento);
    }

    
   
}