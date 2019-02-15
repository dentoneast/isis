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
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.val;

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
    
    public <T> Instance<T> lookupServices(Class<T> serviceClass);
    
    /**
     * Returns a domain service implementing the requested type.
     * <p>
     * If this lookup is ambiguous, the service annotated with highest priority is returned.
     * see {@link Priority}   
     */
    default public <T> Optional<T> lookupService(final Class<T> serviceClass) {
    	
    	val instance = lookupServices(serviceClass);
    	if(instance.isUnsatisfied()) {
    		return Optional.empty();
    	}
    	if(instance.isResolvable()) {
    		return Optional.of(instance.get());
    	}
    	// dealing with ambiguity, get the one, with highest priority annotated
    	
    	val prioComparator = InstanceByPriorityComparator.instance();
    	val toMaxPrioReduction =
    			//TODO [2033] not tested yet, whether the 'direction' is correct < vs >
    			_Reduction.<T>of((max, next)-> prioComparator.leftIsHigherThanRight(next, max) ? next : max);
		
    	instance.forEach(toMaxPrioReduction);
    	
    	return toMaxPrioReduction.getResult();
    }

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

    // -- PRIORITY ANNOTATION HANDLING
	
    static class InstanceByPriorityComparator implements Comparator<Object> {
    	
    	private final static InstanceByPriorityComparator INSTANCE =
    			new InstanceByPriorityComparator();
    	
    	public static InstanceByPriorityComparator instance() {
    		return INSTANCE;
    	}

		@Override
		public int compare(Object o1, Object o2) {
			
			if(o1==null) {
				if(o2==null) {
					return 0;
				} else {
					return -1; // o1 < o2
				}
			} 
			if(o2==null) {
				return 1; // o1 > o2
			}
			
			val prioAnnot1 = _Reflect.getAnnotation(o1.getClass(), Priority.class);
    		val prioAnnot2 = _Reflect.getAnnotation(o2.getClass(), Priority.class);
    		val prio1 = prioAnnot1!=null ? prioAnnot1.value() : 0;
    		val prio2 = prioAnnot2!=null ? prioAnnot2.value() : 0;
    		return Integer.compare(prio1, prio2);
		}
		
		public boolean leftIsHigherThanRight(Object left, Object right) {
			return compare(left, right) > 0;
		}
    	
    }



    
}
