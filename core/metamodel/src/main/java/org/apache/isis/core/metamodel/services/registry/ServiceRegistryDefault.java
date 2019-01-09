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

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.internal.collections._Multimaps.SetMultimap;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.services.ServiceUtil;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.0.0-M2
 */
@Singleton @Slf4j
public final class ServiceRegistryDefault implements ServiceRegistry {
    
    /**
     * This is mutable internally, but only ever exposed (in {@link #streamRegisteredServices()}).
     */
    private final Set<Bean<?>> registeredServiceBeans = _Sets.newHashSet();
    private final Set<Object> registeredServiceInstances = _Sets.newHashSet();

    /**
     * If no key, not yet searched for type; otherwise the corresponding value is a {@link List} of all
     * services that are assignable to the type.  It's possible that this is an empty list.
     */
    private final SetMultimap<Class<?>, Object> servicesAssignableToType = _Multimaps.newSetMultimap();
    private final _Lazy<Map<Class<?>, Object>> serviceByConcreteType = _Lazy.of(this::initServiceByConcreteType);

    
    @Override
    public <T> Optional<T> lookupService(Class<T> serviceClass) {
        return _CDI.getInstance(serviceClass, _Lists.of(QUALIFIER_ANY))
                .map(Instance::get);
    }
    
    @Override
    public Stream<Object> streamServices() {
        
        if(registeredServiceInstances.isEmpty()) {
         
            registeredServiceBeans.stream()
            .forEach(bean->{
                
                val type = bean.getBeanClass();
                val logScope = type.getName().startsWith("org.apache.isis.");
                
                if(logScope) {
                    log.info("processing bean {}", bean);
                } 
                
                Optional<?> managedObject = 
                        _CDI.getManagedBean(type, bean.getQualifiers());
                
                if(managedObject.isPresent()) {
                    registeredServiceInstances.add(managedObject.get());
                    
                    if(logScope) {
                        log.info("registering as a service {}", managedObject.get());
                    }
                    
                } else {
                    
                    if(logScope) {
                        log.warn("failed to register bean as a service {}", bean);
                    }
                }
            });
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
    public boolean isServiceType(Class<?> cls) {
        if(cls.isAnnotationPresent(Singleton.class) ||
                cls.isAnnotationPresent(DomainService.class)) {
            return true;
        }
        return false;
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
        validate(streamServiceBeans());
    }

    // -- HELPER ...
    
    private final static AnnotationLiteral<Any> QUALIFIER_ANY = 
            new AnnotationLiteral<Any>() {
        private static final long serialVersionUID = 1L;};
        
    private Stream<Bean<?>> streamServiceBeans() {
        
        if(registeredServiceBeans.isEmpty()) {
            
            BeanManager beanManager = _CDI.getBeanManager();
            
            Set<Bean<?>> beans = beanManager.getBeans(Object.class, QUALIFIER_ANY);
            for (Bean<?> bean : beans) {
                
                val type = bean.getBeanClass();
                val logScope = type.getName().startsWith("org.apache.isis.");
                
//                if(logScope) {
//                    log.info("processing bean {}", bean);
//                } 
                
                if(!isServiceType(type)) {
                    continue;
                }
                
                if(logScope) {
                    log.info("registering as a service {}", bean);
                }
                
            }
            
        }
        
        return registeredServiceBeans.stream();
    }        
    // -- VALIDATE
        
    private static void validate(final Stream<Bean<?>> serviceBeans) {

        final ListMultimap<String, Bean<?>> servicesById = _Multimaps.newListMultimap();
        serviceBeans.forEach(serviceBean->{
            String id = ServiceUtil.idOfBean(serviceBean);
            servicesById.putElement(id, serviceBean);
        });

        final String errorMsg = servicesById.entrySet().stream()
                .filter(entry->entry.getValue().size()>1) // filter for duplicates
                .map(entry->{
                    String serviceId = entry.getKey();
                    List<Bean<?>> duplicateServiceEntries = entry.getValue();
                    return String.format("serviceId '%s' is declared by domain services %s",
                            serviceId, classNamesFor(duplicateServiceEntries));
                })
                .collect(Collectors.joining(", "));

        if(_Strings.isNotEmpty(errorMsg)) {
            throw new IllegalStateException("Service ids must be unique! "+errorMsg);
        }
    }
    
    private static String classNamesFor(Collection<Bean<?>> serviceBeans) {
        return stream(serviceBeans)
                .map(Bean::getBeanClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

        
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
