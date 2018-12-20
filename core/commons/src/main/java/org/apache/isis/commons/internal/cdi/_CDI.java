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
package org.apache.isis.commons.internal.cdi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;
import javax.inject.Qualifier;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Functions.CheckedRunnable;
import org.apache.isis.core.plugins.ioc.IocPlugin;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;
import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.requires;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal CDI support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0.0-M2
 */
public final class _CDI {

    /**
     * Bootstrap CDI if not already present.
     * @param onDiscover - Packages of the specified (stream of) classes will be scanned and found classes 
     * will be added to the set of bean classes for the synthetic bean archive. 
     */
    public static void init(Supplier<Stream<Class<?>>> onDiscover) {
        
        if(cdi().isPresent()) {
            return;
        }
        
        requires(onDiscover, "onDiscover");
        
        // plug in the provider
        final CDIProvider standaloneCDIProvider = IocPlugin.get().getCDIProvider(onDiscover.get());
        CDI.setCDIProvider(standaloneCDIProvider);

        // verify
        if(!cdi().isPresent()) {
            throw _Exceptions.unrecoverable("Could not resolve an instance of CDI.");
        }
        
        // proper CDI lifecycle support utilizing the fact that WELD provides a WeldContainer that 
        // implements AutoCloseable, which we can put on the _Context, such that when _Context.clear()
        // is called, gets properly closed
        final CheckedRunnable onClose = () -> ((AutoCloseable)CDI.current()).close();
        _Context.putSingleton(_CDI_Lifecycle.class, _CDI_Lifecycle.of(onClose));
        
    }
    
    /**
     * Get the CDI BeanManager for the current context.
     * @return non-null
     * @throws RuntimeException - if no BeanManager could be resolved
     */
    public static BeanManager getBeanManager() {
        return cdi().map(CDI::getBeanManager)
                .orElseThrow(()->_Exceptions.unrecoverable("Could not resolve a BeanManager."));
    }
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<T> getManagedBean(final Class<T> subType, Collection<Annotation> qualifiers) {
        return getInstance(subType, qualifiers)
                .map(instance->tryGet(instance::get));
    }

    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<T> getManagedBean(final Class<T> subType) {
        return getInstance(subType)
                .map(instance->tryGet(instance::get));
    }
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<Instance<T>> getInstance(final Class<T> subType, Collection<Annotation> qualifiers) {
        if(isEmpty(qualifiers)) {
            return getInstance(subType);
        }
        
        final Annotation[] _qualifiers = qualifiers.toArray(new Annotation[] {});
        
        return cdi()
                .map(cdi->tryGet(()->cdi.select(subType, _qualifiers)));
    }
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<Instance<T>> getInstance(final Class<T> subType) {
        return cdi()
                .map(cdi->tryGet(()->cdi.select(subType)));
    }
    
    
    /**
     * Filters the input array into a collection, such that only annotations are retained, 
     * that are valid qualifiers for CDI.
     * @param annotations
     * @return non-null
     */
    public static List<Annotation> filterQualifiers(final Annotation[] annotations) {
        return stream(annotations)
        .filter(_CDI::isQualifier)
        .collect(Collectors.toList());
    }
    
    /**
     * @param annotation
     * @return whether or not the annotation is a valid qualifier for CDI
     */
    public static boolean isQualifier(Annotation annotation) {
        if(annotation==null) {
            return false;
        }
        return annotation.annotationType().getAnnotationsByType(Qualifier.class).length>0;
    }
    
    // -- GENERIC SINGLETON RESOLVING
    
    /**
     * @return framework's managed singleton
     * @throws NoSuchElementException - if the singleton is not resolvable
     */
    public static <T> T getSingleton(Class<T> type) {
        // first lookup CDI, then lookup _Context; the latter to support unit testing 
        return _CDI.getManagedBean(type)
                .orElseGet(()->getMockedSingleton(type));
    }
        
    // -- UNIT TESTING SUPPORT
    
//    public static void putMockedSingletons(Collection<?> mockedServices) {
//        stream(mockedServices)
//        .forEach(_CDI::putMockedSingleton);
//    }
//    
//    public static <T> T putMockedSingleton(T mockedService) {
//        return putMockedSingleton(_Casts.uncheckedCast(mockedService.getClass()), mockedService);
//    }
//    
//    public static <T> T putMockedSingleton(Class<T> type, T mockedService) {
//        _Context.putSingleton(type, mockedService);
//        return mockedService;
//    }
    
    private static <T> T getMockedSingleton(Class<T> type) {
        requires(type, "type");        
        return _Context.getOrThrow(type, ()-> 
            new NoSuchElementException(String.format("Could not resolve an instance of type '%s'", type.getName())));
    }
    
    // -- HELPER
    
    private _CDI() {}
    
    /**
     * Get the CDI instance that provides access to the current container. 
     * @return an optional
     */
    private static Optional<CDI<Object>> cdi() {
        try {
            CDI<Object> cdi = CDI.current();
            return Optional.ofNullable(cdi);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private static <T> T tryGet(final Supplier<T> supplier) {
        try { 
            return supplier.get();  
        } catch (Exception e) {
            return null;
        }
    }




}
