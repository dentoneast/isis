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

package org.apache.isis.core.metamodel.services.registry;

import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Singleton;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.services.ServiceUtil;

import lombok.val;

/**
 * @since 2.0.0-M2
 */
@Singleton
public final class ServiceRegistryDefault implements ServiceRegistry {

    private final Set<BeanAdapter> registeredBeans = _Sets.newHashSet();
    private final Set<Object> serviceCache = _Sets.newHashSet();

    //    @Override 
    //    public Stream<Object> streamServices() {
    //        
    //        if(registeredServiceInstances.isEmpty()) {
    //         
    //            registeredBeans.stream()
    //            .forEach(beanAdapter->{
    //                
    //            	val scope = bean.getScope().getSimpleName();
    //                val type = bean.getBeanClass();
    //                if("RequestScoped".equals(scope)) {
    //                    log.info("skipping registering {}-scoped service {}", scope, type);
    //                    return;
    //                }
    //                
    //                Optional<?> managedObject = 
    //                        _CDI.getManagedBean(type, bean.getQualifiers());
    //                
    //                if(managedObject.isPresent()) {
    //                    registeredServiceInstances.add(managedObject.get());
    //                    
    //                    log.info("registering as a {}-scoped service {}", scope, managedObject.get());
    //                    
    //                } else {
    //                    
    //                    log.warn("failed to register bean {}-scoped as a service {}", scope, bean);
    //                    
    //                }
    //            });
    //        }
    //        
    //        return registeredServiceInstances.stream();
    //    }


    @Override
    public boolean isDomainServiceType(Class<?> cls) {
        if(cls.isAnnotationPresent(DomainService.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Stream<BeanAdapter> streamRegisteredBeans() {
        if(registeredBeans.isEmpty()) {
            _CDI.streamAllBeans()
            .map(this::beanToAdapterIfToBeAcceptedForRegistration)
            .filter(_NullSafe::isPresent)
            .forEach(registeredBeans::add);
        }
        return registeredBeans.stream();
    }  

    @Override  
    @Deprecated //FIXME [2033] this is bad, we should not even need to do this; root problem are ObjectAdapters requiring pojos
    public Stream<Object> streamServices() {
        if(serviceCache.isEmpty()) {
            streamRegisteredBeans()
            .filter(BeanAdapter::isDomainService)
            .map(BeanAdapter::getInstance)
            .filter(Instance::isResolvable)
            .map(Instance::get)
            .forEach(serviceCache::add);
        }
        return serviceCache.stream();
    }

    @Override
    public boolean isRegisteredBean(Class<?> cls) {
        //FIXME [2033] this is poorly implemented, should not require service objects.
        return streamServices()
        .anyMatch(obj->obj.getClass().equals(cls));
    }
    
    @Override
    public void validateServices() {
        ServiceRegistryDefault_validate.validateUniqueDomainServiceId(
                streamRegisteredBeans()
                .filter(BeanAdapter::isDomainService)
                );
    }

    // -- HELPER - FILTER

    private BeanAdapter beanToAdapterIfToBeAcceptedForRegistration(Bean<?> bean) {

        val scope = bean.getScope().getSimpleName(); // also works for produced beans
        val lifecycleContext = LifecycleContext.valueOf(scope);

        // getBeanClass() does not work for produced beans as intended here! 
        // (we do get the producer's class instead)
        val type = bean.getBeanClass(); 
        val isDomainService = isDomainServiceType(type);

        val id = ServiceUtil.idOfBean(bean);
        val beanAdapter = BeanAdapter.of(id, lifecycleContext, bean, isDomainService);
        return beanAdapter;
    }

}
