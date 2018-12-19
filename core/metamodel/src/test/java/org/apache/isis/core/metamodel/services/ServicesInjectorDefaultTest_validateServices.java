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

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;

public class ServicesInjectorDefaultTest_validateServices {

    ServiceRegistry serviceRegistry;

    public static class DomainServiceWithSomeId {
        public String getId() { return "someId"; }
    }

    public static class DomainServiceWithDuplicateId {
        public String getId() { return "someId"; }
    }

    public static class DomainServiceWithDifferentId {
        public String getId() { return "otherId"; }
    }

    public static class ValidateServicesTestValidateServices extends ServicesInjectorDefaultTest_validateServices {

        List<Object> serviceList;

        @Before
        public void setUp() throws Exception {
            serviceList = _Lists.newArrayList();
        }

        @Test(expected=IllegalStateException.class)
        public void validate_DomainServicesWithDuplicateIds() {

            // given
            serviceList.add(new DomainServiceWithSomeId());
            serviceList.add(new DomainServiceWithDuplicateId());
            
            serviceRegistry = new ServiceRegistryDefault();
            serviceList.forEach(serviceRegistry::registerServiceInstance);
            
//            serviceRegistry.registerServiceInstance(serviceInstance);
//                    ServiceRegistryBuilder_forTesting.get()
//                    .addServices(serviceList)
//                    .build();

            // when
            serviceRegistry.validateServices();
        }

        public void validate_DomainServicesWithDifferentIds() {

            // given
            serviceList.add(new DomainServiceWithSomeId());
            serviceList.add(new DomainServiceWithDifferentId());
            
            serviceRegistry = new ServiceRegistryDefault();
            serviceList.forEach(serviceRegistry::registerServiceInstance);

//            serviceRegistry = ServiceRegistryBuilder_forTesting.get()
//                    .addServices(serviceList)
//                    .build();

            // when
            serviceRegistry.validateServices();
        }

    }
}
