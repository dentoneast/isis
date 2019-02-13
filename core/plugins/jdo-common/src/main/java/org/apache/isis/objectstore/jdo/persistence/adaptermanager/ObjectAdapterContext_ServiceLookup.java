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

import java.util.Map;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.base._Timing.StopWatch;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapters for registered services 
 * </p> 
 * @since 2.0.0-M2
 */
@SuppressWarnings("unused")
class ObjectAdapterContext_ServiceLookup {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_ServiceLookup.class);
    private final ObjectAdapterContext objectAdapterContext;
    private final ServiceInjector servicesInjector;
    
    ObjectAdapterContext_ServiceLookup(ObjectAdapterContext objectAdapterContext,
            ServiceInjector servicesInjector) {
        this.objectAdapterContext = objectAdapterContext;
        this.servicesInjector = servicesInjector;
    }

    ObjectAdapter lookupServiceAdapterFor(RootOid rootOid) {
        
        final ServicesByIdResource servicesByIdResource =
                _Context.computeIfAbsent(ServicesByIdResource.class, this::initLookupResource);
        
        final Object serviceInstance = servicesByIdResource.lookupServiceInstance(rootOid);
        if(serviceInstance==null) {
            return null;
        }
        return objectAdapterContext.getFactories().createRootAdapter(serviceInstance, rootOid);
        
    }
    
    // -- HELPER
    
    /**
     *  Application scoped resource to hold a map for looking up services by id.
     */
    private static class ServicesByIdResource implements AutoCloseable {
        private final Map<RootOid, Object> servicesById = _Maps.newHashMap();

        @Override
        public void close() {
            servicesById.clear();
        }

        public Object lookupServiceInstance(RootOid serviceRootOid) {
            return servicesById.get(serviceRootOid);
        }
    }
    
    private ServicesByIdResource initLookupResource() {
        
        objectAdapterContext.printContextInfo("INIT SERVICE ID LOOKUP RESOURCE");
        
        StopWatch watch = _Timing.now();
        
        final ServicesByIdResource lookupResource = new ServicesByIdResource();
        
        servicesInjector.streamServices()
        .map(objectAdapterContext.getObjectAdapterProvider()::adapterFor)
        .forEach(serviceAdapter->{
            Assert.assertFalse("expected to not be 'transient'", serviceAdapter.getOid().isTransient());
            lookupResource.servicesById.put((RootOid)serviceAdapter.getOid() , serviceAdapter.getPojo());
        });
        
        objectAdapterContext.printContextInfo("took (µs) "+watch.stop().getMicros());
        
        return lookupResource;
    }
    

}