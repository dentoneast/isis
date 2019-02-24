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
import javax.enterprise.inject.spi.Bean;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Reduction;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.Value;
import lombok.val;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface ServiceRegistry {


    /**
     * Whether or not the given type is annotated @DomainService.
     * @param cls
     * @return
     */
    boolean isDomainServiceType(Class<?> cls);
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param type
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    default public <T> Instance<T> getInstance(
            final Class<T> type, Annotation[] qualifiers){
        
        val optionalInstance = qualifiers!=null
                ? _CDI.getInstance(type, _CDI.filterQualifiers(qualifiers))
                    : _CDI.getInstance(type);
                
        return optionalInstance.orElse(_CDI.InstanceFactory.empty());
    }
    
    default public <T> Instance<T> getInstance(final Class<T> type){
        return getInstance(type, _Constants.emptyAnnotations);
    }
    
    /**
     * Returns all bean adapters implementing the requested type.
     */
    default Stream<BeanAdapter> streamRegisteredBeansOfType(Class<?> requiredType) {
        return streamRegisteredBeans()
        .filter(beanAdapter->beanAdapter.isCandidateFor(requiredType));
    }
    
    public Stream<BeanAdapter> streamRegisteredBeans();
    
    /**
     * Returns a domain service implementing the requested type.
     * <p>
     * If this lookup is ambiguous, the service annotated with highest priority is returned.
     * see {@link Priority}   
     */
    default public <T> Optional<T> lookupService(final Class<T> serviceClass) {
    	
    	val instance = getInstance(serviceClass);
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
    
    Stream<Object> streamServices();
    
    /**
     * @param cls
     * @return whether the exact type is registered as service
     */
    @Deprecated
    boolean isRegisteredBean(Class<?> cls);
    
    /**
     * Verify domain service Ids are unique.
     * @throws IllegalStateException - if validation fails
     */
    void validateServices();
    
    // -- BEAN ADAPTER
    
    public static enum LifecycleContext {
        ApplicationScoped,
        Singleton,
        SessionScoped,
        RequestScoped,
        ConversationScoped,
        Dependent,
        ;

        public boolean isApplicationScoped() {
            return this == ApplicationScoped;
        }
        
        public boolean isSingleton() {
            return this == Singleton;
        }
        
        public boolean isRequestScoped() {
            return this == RequestScoped;
        }
    }
    
    @Value(staticConstructor="of")
    public static final class BeanAdapter {

        private final String id;
        private final LifecycleContext lifecycleContext;
        private final Bean<?> bean;
        private final boolean domainService;
        
        public Instance<?> getInstance() {
            val type = bean.getBeanClass();
            val instance = _CDI.getInstance(type, bean.getQualifiers())
                    .orElse(_CDI.InstanceFactory.empty());
            return instance;
        }
        
        public boolean isCandidateFor(Class<?> requiredType) {
            return bean.getTypes().stream()
            .filter(type -> type instanceof Class)
            .map(type->(Class<?>)type)
            .anyMatch(type->requiredType.isAssignableFrom(type));
        }
        
    }

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
