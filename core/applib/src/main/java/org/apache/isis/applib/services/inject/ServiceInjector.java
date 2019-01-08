/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.inject;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The repository of services, also able to inject into any object.
 *
 * <p>
 *    Implementation is (and must be) a thread-safe.
 * </p>
 *
 */
public interface ServiceInjector {

    <T> T injectServicesInto(final T domainObject);

    // -- DEPRECATIONS
    
    @Deprecated // use ServiceRegistry instead
    Stream<Object> streamServices();

    @Deprecated // use ServiceRegistry instead
    <T> Stream<T> streamServices(Class<T> serviceClass);
    
    @Deprecated // use ServiceRegistry instead
    <T> Optional<T> lookupService(final Class<T> serviceClass);
    
    @Deprecated // use ServiceRegistry instead
    <T> T lookupServiceElseFail(final Class<T> serviceClass);
    
//FIXME [2033]
//    /**
//     * This is mutable internally, but only ever exposed (in {@link #streamRegisteredServices()}).
//     */
//    private final List<Object> services;
//
//    /**
//     * If no key, not yet searched for type; otherwise the corresponding value is a {@link List} of all
//     * services that are assignable to the type.  It's possible that this is an empty list.
//     */
//    private final ListMultimap<Class<?>, Object> servicesAssignableToType = _Multimaps.newListMultimap();
//    private final _Lazy<Map<Class<?>, Object>> serviceByConcreteType = _Lazy.of(this::initServiceByConcreteType);
//    private final Map<Class<?>, Method[]> methodsByClassCache = _Maps.newHashMap();
//    private final Map<Class<?>, Field[]> fieldsByClassCache = _Maps.newHashMap();
//
//    private final InjectorMethodEvaluator injectorMethodEvaluator;
//    private final boolean autowireSetters;
//    private final boolean autowireInject;
//
//    // -- BUILDER
//    
//    public static ServicesInjectorBuilder builder() {
//        final IsisConfiguration config = _Config.getConfiguration();
//        return new ServicesInjectorBuilder()
//                .addService(config)
//                .autowireSetters(config.getBoolean(KEY_SET_PREFIX, true))
//                .autowireInject(config.getBoolean(KEY_INJECT_PREFIX, true));
//    }
//    
//    public static ServicesInjectorBuilder builderForTesting() {
//        return builder()
//                .autowireSetters(true)
//                .autowireInject(false);
//    }
//    
//    // -- CONSTRUCTOR (NOT EXPOSED)
//
//    ServicesInjector(
//            final List<Object> services,
//            final InjectorMethodEvaluator injectorMethodEvaluator,
//            final boolean autowireSetters,
//            final boolean autowireInject
//            ) {
//        
//        this.services = new ArrayList<>(services);
//
//        this.injectorMethodEvaluator =
//                injectorMethodEvaluator != null
//                ? injectorMethodEvaluator
//                        : new InjectorMethodEvaluatorDefault();
//
//        this.autowireSetters = autowireSetters;
//        this.autowireInject = autowireInject;
//    }
//

//    
//    
//    public <T> void addFallbackIfRequired(final Class<T> serviceClass, final T serviceInstance) {
//        if(!contains(services, serviceClass)) {
//            // add to beginning;
//            // (when first introduced, this feature has been used for the
//            // FixtureScriptsDefault so that appears it top of prototyping menu; not
//            // more flexible than this currently just because of YAGNI).
//            services.add(0, serviceInstance);
//            
//            //[ahuber] currently seems the only entry-point that modifies the services list
//            //hence we also invalidate the lazy lookup
//            serviceByConcreteType.clear();
//        }
//    }
//
//    /**
//     * Validate domain service Ids are unique.
//     */
//    public void validateServices() {
//        validate(streamServices());
//    }
//
//    private static void validate(final Stream<Object> serviceInstances) {
//        
//        final ListMultimap<String, Object> servicesById = _Multimaps.newListMultimap();
//        serviceInstances.forEach(serviceInstance->{
//            String id = ServiceUtil.idOfPojo(serviceInstance);
//            servicesById.putElement(id, serviceInstance);
//        });
//
//        final String errorMsg = servicesById.entrySet().stream()
//        .filter(entry->entry.getValue().size()>1) // filter for duplicates
//        .map(entry->{
//            String serviceId = entry.getKey();
//            List<Object> duplicateServiceEntries = entry.getValue();
//            return String.format("serviceId '%s' is declared by domain services %s",
//                    serviceId, classNamesFor(duplicateServiceEntries));
//        })
//        .collect(Collectors.joining(", "));
//         
//        if(_Strings.isNotEmpty(errorMsg)) {
//            throw new IllegalStateException("Service ids must be unique! "+errorMsg);
//        }
//    }
//
//    private static String classNamesFor(Collection<Object> services) {
//        return stream(services)
//                .map(Object::getClass)
//                .map(Class::getName)
//                .collect(Collectors.joining(", "));
//    }
//
//    static boolean contains(final List<Object> services, final Class<?> serviceClass) {
//        return stream(services)
//                .anyMatch(isOfType(serviceClass));
//    }
//
//    
//    /**
//     * @return Stream of all currently registered service types.
//     */
//    public Stream<Class<?>> streamServiceTypes() {
//        return serviceByConcreteType.get().keySet().stream();
//    }
//    
//    @Override
//    public Stream<Object> streamServices() {
//        return services.stream();
//    }
//    
//    // -- INJECT SERVICES INTO
//
//    /**
//     * Provided by the <tt>ServicesInjector</tt> when used by framework.
//     *
//     * <p>
//     * Called in multiple places from metamodel and facets.
//     */
//    @Override
//    public <T> T injectServicesInto(final T object) {
//        injectServices(object, services);
//        return object;
//    }
//
//    /**
//     * As per {@link #injectServicesInto(Object)}, but for all objects in the
//     * list.
//     */
//    public void injectServicesInto(final List<Object> objects) {
//        for (final Object object : objects) {
//            injectInto(object); // if implements ServiceInjectorAware
//            injectServicesInto(object); // via @javax.inject.Inject or setXxx(...)
//        }
//    }
//
//    // -- INJECT INTO
//
//    /**
//     * That is, injecting this injector...
//     */
//    public void injectInto(final Object candidate) {
//        if (ServicesInjectorAware.class.isAssignableFrom(candidate.getClass())) {
//            final ServicesInjectorAware cast = ServicesInjectorAware.class.cast(candidate);
//            cast.setServicesInjector(this);
//        }
//    }
//    
//    // -- SERVICE LOOKUP
//    
//    /**
//     * Returns all domain services implementing the requested type, in the order
//     * that they were registered in <tt>isis.properties</tt>.
//     *
//     * <p>
//     * Typically there will only ever be one domain service implementing a given type,
//     * (eg {@link PublishingService}), but for some services there can be more than one
//     * (eg {@link ExceptionRecognizer}).
//     *
//     * @see #lookupService(Class)
//     */
//    @Programmatic
//    @Override
//    public <T> Stream<T> streamServices(final Class<T> serviceClass) {
//        return servicesAssignableToType
//                .computeIfAbsent(serviceClass, this::locateMatchingServices)
//                .stream()
//                .map(x->uncheckedCast(x));
//    }
//
//    // -- HELPERS
//
//    private void injectServices(final Object object, final List<Object> services) {
//
//        final Class<?> cls = object.getClass();
//
//        autowireViaFields(object, services, cls);
//
//        if(autowireSetters) {
//            autowireViaPrefixedMethods(object, services, cls, "set");
//        }
//        if(autowireInject) {
//            autowireViaPrefixedMethods(object, services, cls, "inject");
//        }
//    }
//
//    private void autowireViaFields(final Object object, final List<Object> services, final Class<?> cls) {
//
//        _NullSafe.stream(fieldsByClassCache.computeIfAbsent(cls, __->cls.getDeclaredFields()))
//        .filter(isAnnotatedForInjection())
//        .forEach(field->autowire(object, field, services));
//
//        // recurse up the object's class hierarchy
//        final Class<?> superclass = cls.getSuperclass();
//        if(superclass != null) {
//            autowireViaFields(object, services, superclass);
//        }
//    }
//
//    private void autowire(
//            final Object object,
//            final Field field,
//            final List<Object> services) {
//
//        final Class<?> typeToBeInjected = field.getType();
//        // don't think that type can ever be null,
//        // but Javadoc for java.lang.reflect.Field doesn't say
//        if(typeToBeInjected == null) {
//            return;
//        }
//
//        // inject matching services into a field of type Collection<T> if a generic type T is present
//        final Class<?> elementType = _Collections.inferElementTypeIfAny(field);
//        if(elementType!=null) {
//            @SuppressWarnings("unchecked")
//            final Class<? extends Collection<Object>> collectionTypeToBeInjected =
//            (Class<? extends Collection<Object>>) typeToBeInjected;
//
//            final Collection<Object> collectionOfServices = _NullSafe.stream(services)
//                    .filter(_NullSafe::isPresent)
//                    .filter(isOfType(elementType))
//                    .collect(_Collections.toUnmodifiableOfType(collectionTypeToBeInjected));
//
//            invokeInjectorField(field, object, collectionOfServices);
//        }
//
//        for (final Object service : services) {
//            final Class<?> serviceClass = service.getClass();
//            if(typeToBeInjected.isAssignableFrom(serviceClass)) {
//                invokeInjectorField(field, object, service);
//                return;
//            }
//        }
//        
//        // fallback and try CDI
//        _CDI.getManagedBean(typeToBeInjected, _CDI.filterQualifiers(field.getAnnotations()))
//        .ifPresent(bean->invokeInjectorField(field, object, bean));
//        
//    }
//
//    private void autowireViaPrefixedMethods(
//            final Object object,
//            final List<Object> services,
//            final Class<?> cls,
//            final String prefix) {
//
//        _NullSafe.stream(methodsByClassCache.computeIfAbsent(cls, __->cls.getMethods()))
//        .filter(nameStartsWith(prefix))
//        .forEach(prefixedMethod->autowire(object, prefixedMethod, services));
//    }
//
//    private void autowire(
//            final Object object,
//            final Method prefixedMethod,
//            final List<Object> services) {
//
//        for (final Object service : services) {
//            final Class<?> serviceClass = service.getClass();
//            final boolean isInjectorMethod = injectorMethodEvaluator.isInjectorMethodFor(prefixedMethod, serviceClass);
//            if(isInjectorMethod) {
//                prefixedMethod.setAccessible(true);
//                invokeInjectorMethod(prefixedMethod, object, service);
//                return;
//            }
//        }
//    }
//
//    private static void invokeMethod(final Method method, final Object target, final Object[] parameters) {
//        try {
//            method.invoke(target, parameters);
//        } catch (final SecurityException | IllegalAccessException e) {
//            throw new MetaModelException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass().getName()));
//        } catch (final IllegalArgumentException e1) {
//            throw new MetaModelException(e1);
//        } catch (final InvocationTargetException e) {
//            final Throwable targetException = e.getTargetException();
//            if (targetException instanceof RuntimeException) {
//                throw (RuntimeException) targetException;
//            } else {
//                throw new MetaModelException(targetException);
//            }
//        }
//    }
//
//    private static void invokeInjectorField(final Field field, final Object target, final Object parameter) {
//        try {
//            field.setAccessible(true);
//            field.set(target, parameter);
//        } catch (final IllegalArgumentException e) {
//            throw new MetaModelException(e);
//        } catch (final IllegalAccessException e) {
//            throw new MetaModelException(String.format("Cannot access the %s field in %s", field.getName(), target.getClass().getName()));
//        }
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("injected {} into {}", parameter, new ToString(target));
//        }
//    }
//
//    private static void invokeInjectorMethod(final Method method, final Object target, final Object parameter) {
//        final Object[] parameters = new Object[] { parameter };
//        invokeMethod(method, target, parameters);
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("injected {} into {}", parameter, new ToString(target));
//        }
//    }
//    
//    // -- REFLECTIVE PREDICATES
//
//    private static final Predicate<Object> isOfType(final Class<?> cls) {
//        return obj->cls.isAssignableFrom(obj.getClass());
//    }
//
//    private static final Predicate<Method> nameStartsWith(final String prefix) {
//        return method->method.getName().startsWith(prefix);
//    }
//
//    private static final Predicate<Field> isAnnotatedForInjection() {
//        return field->field.getAnnotation(javax.inject.Inject.class) != null;
//    }
//


}
