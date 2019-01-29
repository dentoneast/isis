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

package org.apache.isis.config.builder;

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.fixturescripts.DiscoverableFixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscoveryPlugin;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.0.0-M2
 */
@Slf4j
class ModulePackageHelper {
    
    public static Set<Class<?>> runTypeDiscovery(final AppManifest appManifest) {
        
        final Set<Class<?>> moduleAndFrameworkTypesForScanning = 
                findAndRegisterTypes(appManifest);
        
        return moduleAndFrameworkTypesForScanning;
        
    }
    
    // -- HELPER

    private static Stream<Class<?>> modulesFrom(final AppManifest appManifest) {
        
        final List<Class<?>> modules = appManifest.getModules();
        
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException(
                    "If an appManifest is provided then it must return a non-empty set of modules");
        }

        return modules.stream();
    }
    
    private static Stream<String> modulePackageNamesFrom(final AppManifest appManifest) {
        return modulesFrom(appManifest)
                .map(Class::getPackage)
                .map(Package::getName);
    }
    
    private static Set<Class<?>> findAndRegisterTypes(final AppManifest appManifest) {
        
        requires(appManifest, "appManifest");
        
        log.info(String.format(
                "Discover the application's domain and register all types using manifest '%s' ...",
                appManifest.getClass().getName()) );
        
        
        val typesForScanning = new HashSet<Class<?>>();
        AppManifest.Registry.FRAMEWORK_PROVIDED_TYPES_FOR_SCANNING.stream()
        .map(name -> {
			try {
				return _Context.loadClass(name);
			} catch (ClassNotFoundException e) {
				throw _Exceptions.unrecoverable(e);
			}
		})
        .forEach(typesForScanning::add);
        
        modulesFrom(appManifest)
        .forEach(typesForScanning::add);
        
        typesForScanning.add(appManifest.getClass());
        
        //FIXME [2033] at this point we should have all we need, let CDI take over
        // and let then CDI Bean intercepter make entries into the registry 
        
        final AppManifest.Registry registry = AppManifest.Registry.instance();

        final Set<String> moduleAndFrameworkPackages = new HashSet<>();
        moduleAndFrameworkPackages.addAll(AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICE_PACKAGES);
        modulePackageNamesFrom(appManifest)
            .forEach(moduleAndFrameworkPackages::add);
        moduleAndFrameworkPackages.add(appManifest.getClass().getPackage().getName());
        
        final ClassDiscovery discovery = ClassDiscoveryPlugin.get().discover(moduleAndFrameworkPackages);

        final Set<Class<?>> domainServiceTypes = _Sets.newLinkedHashSet();
        domainServiceTypes.addAll(discovery.getTypesAnnotatedWith(DomainService.class));
        domainServiceTypes.addAll(discovery.getTypesAnnotatedWith(DomainServiceLayout.class));
        
        final Set<Class<?>> persistenceCapableTypes = PersistenceCapableTypeFinder.find(discovery);

        final Set<Class<? extends FixtureScript>> fixtureScriptTypes = discovery.getSubTypesOf(FixtureScript.class)
                .stream()
                .filter(aClass -> {
                    // the fixtureScript types are introspected just to provide a drop-down when running fixture scripts
                    // in prototyping mode (though they may be introspected lazily if actually run).
                    // we therefore try to limit the set of fixture types eagerly introspected at startup
                    //
                    // specifically, we ignore as a fixture script if annotated with @Programmatic
                    // (though directly implementing DiscoverableFixtureScript takes precedence and will NOT ignore)
                    return DiscoverableFixtureScript.class.isAssignableFrom(aClass) ||
                            _Reflect.getAnnotation(aClass, Programmatic.class) == null;
                })
                .collect(Collectors.toSet());

        final Set<Class<?>> domainObjectTypes = _Sets.newLinkedHashSet();
        domainObjectTypes.addAll(discovery.getTypesAnnotatedWith(DomainObject.class));
        domainObjectTypes.addAll(discovery.getTypesAnnotatedWith(DomainObjectLayout.class));

        final Set<Class<?>> mixinTypes = _Sets.newHashSet();
        mixinTypes.addAll(discovery.getTypesAnnotatedWith(Mixin.class));
        domainObjectTypes.stream()
        .filter(input -> {
            final DomainObject annotation = input.getAnnotation(DomainObject.class);
            return annotation != null && annotation.nature() == Nature.MIXIN;
        })
        .forEach(mixinTypes::add);

        final Set<Class<?>> viewModelTypes = _Sets.newLinkedHashSet();
        viewModelTypes.addAll(discovery.getTypesAnnotatedWith(ViewModel.class));
        viewModelTypes.addAll(discovery.getTypesAnnotatedWith(ViewModelLayout.class));

        final Set<Class<?>> xmlElementTypes = _Sets.newLinkedHashSet();
        xmlElementTypes.addAll(discovery.getTypesAnnotatedWith(XmlElement.class));

        // add in any explicitly registered services...
        domainServiceTypes.addAll(appManifest.getAdditionalServices());

        // Reflections seems to have a bug whereby it will return some classes outside the
        // set of packages that we want (think this is to do with the fact that it matches based on
        // the prefix and gets it wrong); so we double check and filter out types outside our
        // required set of packages.

        // for a tiny bit of efficiency, we append a '.' to each package name here, outside the loops
        final List<String> packagesWithDotSuffix =
                moduleAndFrameworkPackages.stream()
                .map(s -> s != null ? s + "." : null)
                .collect(Collectors.toList());

        registry.setDomainServiceTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, domainServiceTypes));
        registry.setPersistenceCapableTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, persistenceCapableTypes));
        registry.setFixtureScriptTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, fixtureScriptTypes));
        registry.setMixinTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, mixinTypes));
        registry.setDomainObjectTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, domainObjectTypes));
        registry.setViewModelTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, viewModelTypes));
        registry.setXmlElementTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, xmlElementTypes));
        
        typesForScanning.addAll(domainServiceTypes);
        typesForScanning.addAll(viewModelTypes);
        typesForScanning.addAll(domainObjectTypes);
        typesForScanning.addAll(persistenceCapableTypes);
        
        return typesForScanning;
    }
    
    static <T> Set<Class<? extends T>> withinPackageAndNotAnonymous(
            final Collection<String> packageNames,
            final Set<Class<? extends T>> classes) {
        
        final Set<Class<? extends T>> classesWithin = _Sets.newLinkedHashSet();
        for (Class<? extends T> clz : classes) {
            final String className = clz.getName();
            if(containedWithin(packageNames, className) && notAnonymous(clz)) {
                classesWithin.add(clz);
            }
        }
        return classesWithin;
    }
    
    static private boolean containedWithin(final Collection<String> packageNames, final String className) {
        for (String packageName : packageNames) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        //TODO [2039] we may need to re-think this policy, there should not be surprising use-cases
        log.warn("Skipping a service for registration because due to not being part of the packagess to include: " + className);
        return false;
    }

    private static <T> boolean notAnonymous(final Class<? extends T> clz) {
        try {
            return !clz.isAnonymousClass();
        } catch(NoClassDefFoundError error) {
            return false; // ignore, assume anonymous
        }
    }
    
    
}
