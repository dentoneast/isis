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

package org.apache.isis.core.metamodel.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

//import org.jmock.Expectations;
//import org.jmock.auto.Mock;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.metamodel.BeansForTesting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@EnableWeld
class ServicesInjectorDefaultTest {

    static class Mocks {
        
        @Produces
        SomeDomainObject mockDomainObject() {
            return Mockito.mock(SomeDomainObject.class);
        }
        
        @Produces
        RepositoryService mockRepositoryService() {
            return Mockito.mock(RepositoryServiceExtended.class);
        }
        
        @Produces
        Mixin mockMixin() {
            return Mockito.mock(Mixin.class);
        }
        
        @Produces
        Service1 mockService1() {
            return Mockito.mock(Service1.class);
        }
        
        @Produces
        Service2 mockService2() {
            return Mockito.mock(Service2.class);
        }
        
    }
    
    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            
            BeansForTesting.builder()
            .injector()
            .addAll(
                    Mocks.class,
                    Service1.class,
                    Service2.class
                    )
            .build()
            
            )
    .build();

    @Inject private ServiceInjector injector;
    @Inject private ServiceRegistry registry;

    // -- SCENARIO
    
    public static interface Service1 {
    }

    public static interface Service2 {
    }

    public static interface Mixin {
    }

    public static interface RepositoryServiceExtended extends RepositoryService, Mixin {
    }

    public static interface SomeDomainObject {
        public void setContainer(RepositoryService container);
        public void setMixin(Mixin mixin);
        public void setService1(Service1 service);
        public void setService2(Service2 service);
    }
    
    // -- TESTS

    @Test
    public void shouldInjectContainer(SomeDomainObject mockDomainObject) {

        injector.injectServicesInto(mockDomainObject);
        
        verify(mockDomainObject, times(1)).setContainer(any(RepositoryService.class));
        verify(mockDomainObject, times(1)).setMixin(any(Mixin.class));
        verify(mockDomainObject, times(1)).setService1(any(Service1.class));
        verify(mockDomainObject, times(1)).setService2(any(Service2.class));
        
    }
    
    @Test
    public void shouldStreamRegisteredServices() {
        List<Class<?>> registeredServices = registry.streamServiceTypes()
                .collect(Collectors.toList());
        Assertions.assertTrue(registeredServices.size()>=3);
    }

}
