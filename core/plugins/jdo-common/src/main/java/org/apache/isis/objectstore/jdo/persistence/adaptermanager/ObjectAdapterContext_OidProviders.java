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
package org.apache.isis.objectstore.jdo.persistence.adaptermanager;

import java.util.UUID;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.persistence.adapter.OidFactory.OidProvider;
import org.apache.isis.core.runtime.persistence.adapter.OidFactory.OidProvider2;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.context.managers.ContextManager;
import org.apache.isis.core.runtime.system.context.managers.ManagedObjectResolver;

import lombok.val;

class ObjectAdapterContext_OidProviders {

    
    static class GuardAgainstRootOid implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getPojo() instanceof RootOid;
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            throw new IllegalArgumentException("Cannot create a RootOid for pojo, "
                    + "when pojo is instance of RootOid. You might want to ask "
                    + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
        }
    }
    
    
    static class OidForServices implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().isService();
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            final String identifier = SystemConstants.SERVICE_IDENTIFIER;
            return Oid.Factory.persistentOf(managedObject.getSpecification().getSpecId(), identifier);
        }

    }
    
//TODO [2033] is now handled through OidForManagedContexts
//    
//    static class OidForPersistent implements OidProvider {
//
//        private final IsisJdoMetamodelPlugin isisJdoMetamodelPlugin = IsisJdoMetamodelPlugin.get();
//
//        @Override
//        public boolean isHandling(ManagedObject managedObject) {
//            // equivalent to 'isInstanceOfPersistable = pojo instanceof Persistable'
//            final boolean isInstanceOfPersistable = isisJdoMetamodelPlugin
//            		.isPersistenceEnhanced(managedObject.getPojo().getClass());
//            return isInstanceOfPersistable;
//        }
//
//        @Override
//        public RootOid oidFor(ManagedObject managedObject) {
//        	val pojo = managedObject.getPojo();
//        	val spec = managedObject.getSpecification();
//        	
//            final PersistenceSession persistenceSession = IsisContext.getPersistenceSession().get();
//            final boolean isRecognized = persistenceSession.isRecognized(pojo);
//            if(isRecognized) {
//                final String identifier = persistenceSession.identifierFor(pojo);
//                return Oid.Factory.persistentOf(spec.getSpecId(), identifier);
//            } else {
//                final String identifier = UUID.randomUUID().toString();
//                return Oid.Factory.transientOf(spec.getSpecId(), identifier);    
//            }
//        }
//        
//    }

    static class OidForManagedContexts implements OidProvider2 {

    	final _Lazy<ContextManager> contextManager = _Lazy.of(()->
    		IsisContext.getServiceRegistry().lookupServiceElseFail(ContextManager.class));

		@Override
		public ManagedObjectResolver resolverFor(ManagedObject managedObject) {
        	val spec = managedObject.getSpecification();
        	val managedObjectResolver = contextManager.get().resolverForIfAny(spec);
        	
        	return managedObjectResolver;
		}
        
    }

    static class OidForValues implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
        	val spec = managedObject.getSpecification();
            return spec.containsFacet(ValueFacet.class);
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            return Oid.Factory.value();
        }

    }
    
    //TODO [2033] should no longer be required
    static class OidForViewModels implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
        	val spec = managedObject.getSpecification();
            return spec.containsFacet(ViewModelFacet.class);
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
        	val pojo = managedObject.getPojo();
        	val spec = managedObject.getSpecification();
        	
            final ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            final String identifier = recreatableObjectFacet.memento(pojo);
            return Oid.Factory.viewmodelOf(spec.getSpecId(), identifier);
        }

    }
    
    static class OidForOthers implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return true; // try to handle anything
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
        	val spec = managedObject.getSpecification();
            final String identifier = UUID.randomUUID().toString();
            return Oid.Factory.transientOf(spec.getSpecId(), identifier);
        }

    }
    

}
