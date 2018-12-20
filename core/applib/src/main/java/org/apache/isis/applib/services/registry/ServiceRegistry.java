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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;

@ApplicationScoped
public interface ServiceRegistry {

    @Deprecated
    void registerServiceInstance(Object serviceInstance);
    
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
     * (eg {@link org.apache.isis.applib.services.repository.RepositoryService}), but for some services there can be
     * more than one (eg {@link ExceptionRecognizer}).
     */
    public default <T> Optional<T> lookupService(final Class<T> serviceClass) {
        return streamServices(serviceClass)
                .findFirst();
    }

    public default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return lookupService(serviceClass)
                .orElseThrow(()->
                    new NoSuchElementException("Could not locate service of type '" + serviceClass + "'"));
    }
    
    public default boolean isService(final Class<?> serviceClass) {
        return lookupService(serviceClass).isPresent();
    }
    
    /**
     * @param cls
     * @return whether the exact type is registered as service
     */
    boolean isRegisteredService(Class<?> cls);
    
    /**
     * @param cls
     * @return whether the exact object is registered as service
     */
    boolean isRegisteredServiceInstance(Object pojo);

    void validateServices();


    
}
