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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;

@Singleton
public class ServiceInjectorDefault implements ServiceInjector {
    
    private static final Logger LOG = LoggerFactory.getLogger(ServiceInjectorDefault.class);
    
    private static final String KEY_SET_PREFIX = "isis.services.injector.setPrefix";
    private static final String KEY_INJECT_PREFIX = "isis.services.injector.injectPrefix";
    
    @Inject IsisConfiguration configuration;
    @Inject ServiceRegistry serviceRegistry;
    @Inject InjectorMethodEvaluator injectorMethodEvaluator;
    
    private final Map<Class<?>, Method[]> methodsByClassCache = _Maps.newHashMap();
    private final Map<Class<?>, Field[]> fieldsByClassCache = _Maps.newHashMap();

    @Override
    public <T> T injectServicesInto(T domainObject) {
        final List<Object> services = serviceRegistry.streamServices().collect(Collectors.toList()); 
        injectServices(domainObject, services);
        return domainObject;
    }
    
    @PostConstruct
    public void init() {
        autowireSetters = configuration.getBoolean(KEY_SET_PREFIX, true);
        autowireInject = configuration.getBoolean(KEY_INJECT_PREFIX, true);
    }
    
    // -- HELPERS
    
    boolean autowireSetters;
    boolean autowireInject;    

    private void injectServices(final Object object, final List<Object> services) {

        final Class<?> cls = object.getClass();

        injectToFields(object, services, cls);

        if(autowireSetters) {
            injectViaPrefixedMethods(object, services, cls, "set");
        }
        if(autowireInject) {
            injectViaPrefixedMethods(object, services, cls, "inject");
        }
    }

    private void injectToFields(final Object object, final List<Object> services, final Class<?> cls) {

        _NullSafe.stream(fieldsByClassCache.computeIfAbsent(cls, __->cls.getDeclaredFields()))
        .filter(isAnnotatedForInjection())
        .forEach(field->autowire(object, field, services));

        // recurse up the object's class hierarchy
        final Class<?> superclass = cls.getSuperclass();
        if(superclass != null) {
            injectToFields(object, services, superclass);
        }
    }

    private void autowire(
            final Object object,
            final Field field,
            final List<Object> services) {

        final Class<?> typeToBeInjected = field.getType();
        // don't think that type can ever be null,
        // but Javadoc for java.lang.reflect.Field doesn't say
        if(typeToBeInjected == null) {
            return;
        }

        // inject matching services into a field of type Collection<T> if a generic type T is present
        final Class<?> elementType = _Collections.inferElementTypeIfAny(field);
        if(elementType!=null) {
            @SuppressWarnings("unchecked")
            final Class<? extends Collection<Object>> collectionTypeToBeInjected =
            (Class<? extends Collection<Object>>) typeToBeInjected;

            final Collection<Object> collectionOfServices = _NullSafe.stream(services)
                    .filter(_NullSafe::isPresent)
                    .filter(isOfType(elementType))
                    .collect(_Collections.toUnmodifiableOfType(collectionTypeToBeInjected));

            invokeInjectorField(field, object, collectionOfServices);
        }

        for (final Object service : services) {
            final Class<?> serviceClass = service.getClass();
            if(typeToBeInjected.isAssignableFrom(serviceClass)) {
                invokeInjectorField(field, object, service);
                return;
            }
        }

        // fallback and try CDI
        _CDI.getManagedBean(typeToBeInjected, _CDI.filterQualifiers(field.getAnnotations()))
        .ifPresent(bean->invokeInjectorField(field, object, bean));

    }

    private void injectViaPrefixedMethods(
            final Object object,
            final List<Object> services,
            final Class<?> cls,
            final String prefix) {

        _NullSafe.stream(methodsByClassCache.computeIfAbsent(cls, __->cls.getMethods()))
        .filter(nameStartsWith(prefix))
        .forEach(prefixedMethod->autowire(object, prefixedMethod, services));
    }

    private void autowire(
            final Object object,
            final Method prefixedMethod,
            final List<Object> services) {

        for (final Object service : services) {
            final Class<?> serviceClass = service.getClass();
            final boolean isInjectorMethod = injectorMethodEvaluator.isInjectorMethodFor(prefixedMethod, serviceClass);
            if(isInjectorMethod) {
                prefixedMethod.setAccessible(true);
                invokeInjectorMethod(prefixedMethod, object, service);
                return;
            }
        }
    }

    private static void invokeMethod(final Method method, final Object target, final Object[] parameters) {
        try {
            method.invoke(target, parameters);
        } catch (final SecurityException | IllegalAccessException e) {
            throw new MetaModelException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass().getName()));
        } catch (final IllegalArgumentException e1) {
            throw new MetaModelException(e1);
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                throw new MetaModelException(targetException);
            }
        }
    }

    private static void invokeInjectorField(final Field field, final Object target, final Object parameter) {
        try {
            field.setAccessible(true);
            field.set(target, parameter);
        } catch (final IllegalArgumentException e) {
            throw new MetaModelException(e);
        } catch (final IllegalAccessException e) {
            throw new MetaModelException(String.format("Cannot access the %s field in %s", field.getName(), target.getClass().getName()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected {} into {}", parameter, new ToString(target));
        }
    }

    private static void invokeInjectorMethod(final Method method, final Object target, final Object parameter) {
        final Object[] parameters = new Object[] { parameter };
        invokeMethod(method, target, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected {} into {}", parameter, new ToString(target));
        }
    }

    // -- REFLECTIVE PREDICATES

    private static final Predicate<Object> isOfType(final Class<?> cls) {
        return obj->cls.isAssignableFrom(obj.getClass());
    }

    private static final Predicate<Method> nameStartsWith(final String prefix) {
        return method->method.getName().startsWith(prefix);
    }

    private static final Predicate<Field> isAnnotatedForInjection() {
        return field->field.getAnnotation(javax.inject.Inject.class) != null;
    }

    // -- DELEGATIONS
    
    @Override
    public Stream<Object> streamServices() {
        return serviceRegistry.streamServices();
    }

    @Override
    public <T> Stream<T> streamServices(Class<T> serviceClass) {
        return serviceRegistry.streamServices(serviceClass);
    }

    @Override
    public <T> Optional<T> lookupService(Class<T> serviceClass) {
        return serviceRegistry.lookupService(serviceClass);
    }

    @Override
    public <T> T lookupServiceElseFail(Class<T> serviceClass) {
        return serviceRegistry.lookupServiceElseFail(serviceClass);
    }


}
