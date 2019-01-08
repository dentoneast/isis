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

package org.apache.isis.applib.services.registry;

import java.lang.annotation.Annotation;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.commons.internal.cdi._CDI;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface ServiceRegistry {

    /**
     * Whether or not the given type is a application-scoped singleton, that
     * qualifies as a service to be managed by the framework. 
     * @param cls
     * @return
     */
    boolean isServiceType(Class<?> cls);
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param type
     * @param annotations
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    default public <T> Optional<Instance<T>> getInstance(
            final Class<T> type, Annotation[] annotations){
        return annotations!=null
                ? _CDI.getInstance(type, _CDI.filterQualifiers(annotations))
                    : _CDI.getInstance(type);
    }
    
    /**
     * Obtains a managed bean for the given required type and additional required qualifiers. 
     * @param type
     * @param annotations
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    default public <T> Optional<T> getManagedBean(
            final Class<T> type, Annotation[] annotations){
        return annotations!=null
                ? _CDI.getManagedBean(type, _CDI.filterQualifiers(annotations))
                    : _CDI.getManagedBean(type);
    }
    
    
    /**
     * @return Stream of all currently registered service instances.
     */
    Stream<Object> streamServices();

    /**
     * Returns all domain services implementing the requested type, in the order
     * that they were registered in <tt>isis.properties</tt>.
     *
     * <p>
     * Typically there will only ever be one domain service implementing a given type,
     * (eg {@link PublishingService}), but for some services there can be more than one
     * (eg {@link ExceptionRecognizer}).
     *
     * @see #lookupService(Class)
     */
    <T> Stream<T> streamServices(Class<T> serviceClass);

    public default Stream<Class<?>> streamServiceTypes() {
        return streamServices().map(Object::getClass);
    }
    
    /**
     * Returns the first registered domain service implementing the requested type.
     *
     * <p>
     * Typically there will only ever be one domain service implementing a given type,
     * (eg {@link org.apache.isis.applib.services.repository.RepositoryService}), 
     * but for some services there can be
     * more than one (eg {@link ExceptionRecognizer}).
     */
    public <T> Optional<T> lookupService(final Class<T> serviceClass);

    public default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return lookupService(serviceClass)
                .orElseThrow(()->
                    new NoSuchElementException("Could not locate service of type '" + serviceClass + "'"));
    }
    
    /**
     * @param cls
     * @return whether the exact type is registered as service
     */
    boolean isRegisteredService(Class<?> cls);
    
    /**
     * @param pojo
     * @return whether the pojo equals one of the registered service instances
     */
    boolean isRegisteredServiceInstance(Object pojo);

    /**
     * Verify domain service Ids are unique.
     * @throws IllegalStateException - if validation fails
     */
    void validateServices();




    
}
