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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.SetMultimap;
import org.apache.isis.commons.internal.collections._Sets;

import lombok.val;

/**
 * @since 2.0.0-M2
 */
@Singleton
public final class ServiceRegistryDefault implements ServiceRegistry {
    
    /**
     * This is mutable internally, but only ever exposed (in {@link #streamRegisteredServices()}).
     */
    private final Set<Object> registeredServiceInstances = _Sets.newHashSet();

    /**
     * If no key, not yet searched for type; otherwise the corresponding value is a {@link List} of all
     * services that are assignable to the type.  It's possible that this is an empty list.
     */
    private final SetMultimap<Class<?>, Object> servicesAssignableToType = _Multimaps.newSetMultimap();
    private final _Lazy<Map<Class<?>, Object>> serviceByConcreteType = _Lazy.of(this::initServiceByConcreteType);


    @Override
    public void registerServiceInstance(Object serviceInstance) {
        //registeredServiceInstances.add(serviceInstance);
        //serviceByConcreteType.clear(); // invalidate
    }
    
    @Override
    public Stream<Object> streamServices() {
        if(registeredServiceInstances.isEmpty()) {
         
            BeanManager beanManager = _CDI.getBeanManager();
            
            Set<Bean<?>> beans = beanManager.getBeans(Object.class, QUALIFIER_ANY);
            for (Bean<?> bean : beans) {
                
                val type = bean.getBeanClass();
                
                if(bean.getBeanClass().getName().startsWith("org.apache.isis.")) {
                    System.out.println("!!! " + bean);    
                }
                
                Optional<?> managedObject = 
                        _CDI.getManagedBean(type, bean.getQualifiers());
                
                if(managedObject.isPresent()) {
                    registeredServiceInstances.add(managedObject.get());
                    
                    System.out.println("\t registering: " + managedObject.get());
                    
                } else {
                    if(bean.getBeanClass().getName().startsWith("org.apache.isis.")) {
                        System.out.println("\t !!! bean not present: " + type.getName());    
                    }
                }
                
            }
            
        }
        
        return registeredServiceInstances.stream();
    }

    @Override
    public <T> Stream<T> streamServices(final Class<T> serviceClass) {
        return servicesAssignableToType
                .computeIfAbsent(serviceClass, this::locateMatchingServices)
                .stream()
                .map(x->_Casts.uncheckedCast(x));
    }

    @Override
    public boolean isRegisteredService(final Class<?> cls) {
        return serviceByConcreteType.get().containsKey(cls);
    }

    @Override
    public boolean isRegisteredServiceInstance(final Object pojo) {
        if(pojo==null) {
            return false;
        }
        final Class<?> key = pojo.getClass();
        final Object serviceInstance = serviceByConcreteType.get().get(key);
        return Objects.equals(pojo, serviceInstance);
    }
    
    
    @Override
    public void validateServices() {
        // TODO Auto-generated method stub
        
    }
    
    // -- HELPER ...
    
    private final static AnnotationLiteral<Any> QUALIFIER_ANY = 
            new AnnotationLiteral<Any>() {
        private static final long serialVersionUID = 1L;};
    
    
//    private final static AnnotationLiteral<Singleton> SINGLETON =
//            new AnnotationLiteral<Singleton>() {
//        private static final long serialVersionUID = 1L;};
//    
//    private final static AnnotationLiteral<ApplicationScoped> APPLICATIONSCOPED = 
//            new AnnotationLiteral<ApplicationScoped>() {
//        private static final long serialVersionUID = 1L;};
        
    // -- LOOKUP SERVICE(S)

    private <T> Set<Object> locateMatchingServices(final Class<T> serviceClass) {
        final Set<Object> matchingServices = streamServices()
                .filter(isOfType(serviceClass))
                .collect(Collectors.toSet());
        return matchingServices;
    }
    
    // -- LAZY INIT

    private Map<Class<?>, Object> initServiceByConcreteType(){
        final Map<Class<?>, Object> map = _Maps.newHashMap();
        for (Object service : registeredServiceInstances) {
            final Class<?> concreteType = service.getClass();
            map.put(concreteType, service);
        }
        return map;
    }
    
    // -- REFLECTIVE PREDICATES

    private static final Predicate<Object> isOfType(final Class<?> cls) {
        return obj->cls.isAssignableFrom(obj.getClass());
    }


    
}
