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
package org.apache.isis.config;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.val;

public final class AppConfigLocator {
    
    private static final Logger LOG = LoggerFactory.getLogger(AppConfigLocator.class);
    
    private AppConfigLocator() { }
    
    public static AppConfig getAppConfig() {
        return _Context.computeIfAbsent(AppConfig.class, ()->lookupAppConfigAndVerifyCDI());
    }
    
    // -- HELPER
    
    // for sanity check
    private final static Set<String> criticalServices() {
    	
    	//TODO [2033] use classes from AppManifest.Registry instead
    	
    	return _Sets.newLinkedHashSet(_Lists.of(
    		"org.apache.isis.applib.services.registry.ServiceRegistry", 
    		"org.apache.isis.config.IsisConfiguration", 
    		"org.apache.isis.core.runtime.system.session.IsisSessionFactory", 
    		
    		"org.apache.isis.core.security.authentication.manager.AuthenticationManager", 
    		"org.apache.isis.core.security.authorization.manager.AuthorizationManager",
    		"org.apache.isis.core.security.authentication.AuthenticationSessionProvider",
    		
    		"org.apache.isis.core.metamodel.specloader.SpecificationLoader",
    		"org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory",
    		
    		"org.apache.isis.applib.services.eventbus.EventBusService",
    		"org.apache.isis.applib.services.factory.FactoryService",
    		
    		"org.apache.isis.applib.services.i18n.LocaleProvider",
    		"org.apache.isis.applib.services.i18n.TranslationsResolver",
    		"org.apache.isis.applib.services.i18n.TranslationService",
    		"org.apache.isis.applib.services.message.MessageService",
    		
    		"org.apache.isis.applib.services.repository.RepositoryService",
    		"org.apache.isis.applib.services.title.TitleService",
    		"org.apache.isis.applib.services.user.UserService",
    		"org.apache.isis.applib.services.xactn.TransactionService",
    		
    		"org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal",
    		"org.apache.isis.applib.services.wrapper.WrapperFactory",
    		"org.apache.isis.core.runtime.services.bookmarks.BookmarkServiceInternalDefault",
    		
    		"org.apache.isis.jdo.datanucleus.persistence.IsisLegacyJdoContextHandler",
    		
    		"org.apache.isis.applib.services.homepage.HomePageProviderService"
    		
    		
    		));
    }
    
    private final static _Probe probe = 
    		_Probe.unlimited().label("AppConfigLocator");
    private final static _Probe probeSanity = 
    		_Probe.unlimited().label("AppConfigLocator SANITY");
    
    private static AppConfig lookupAppConfigAndVerifyCDI() {
        final AppConfig appConfig = lookupAppConfig();
        
		_CDI.streamAllBeans().forEach(bean->{

			val type = bean.getBeanClass();
			val logScope = type.getName().startsWith("org.apache.isis.") ||
					type.getName().startsWith("domainapp.");
            if(logScope) {
            	probe.println("discovered bean %s", bean);
            } 
		});
		
		// ensure critical services are managed by CDI
		final Set<String> managedTypes = new HashSet<>();
		for(String serviceClassName : criticalServices()) {
			try {
				val type = _Context.loadClassAndInitialize(serviceClassName);
				
				if(!_CDI.getManagedBean(type).isPresent()) {
					throw new NoSuchElementException(String.format("No such bean known to CDI."));
				};
				
				probeSanity.println("%s ... managed by CDI", type.getSimpleName());
				managedTypes.add(serviceClassName);
				
			} catch (Exception e) {
				probeSanity.println("!!%s ... failed to resolve bean '%s' cause: %s",
						serviceClassName.substring(1+serviceClassName.lastIndexOf(".")),
						serviceClassName, 
						e.getMessage());
				
				_Exceptions.streamStacktraceLines(_Exceptions.getRootCause(e), 12)
				.forEach(line->probeSanity.println(1, line));
				
			}
		}
		
		
		val criticalServicesNotManaged = criticalServices();
		criticalServicesNotManaged.removeAll(managedTypes);
		if(criticalServicesNotManaged.size()>0) {
			
			val servicesLiteral = criticalServicesNotManaged.stream()
					.collect(Collectors.joining(", "));
			
			throw _Exceptions.unrecoverable(
					String.format("Some critical services are not managed by CDI: {%s}", servicesLiteral));
		}
		
        
        return appConfig;
    }
    
    private static AppConfig lookupAppConfig() {
        
        AppConfig appConfig;
        
        appConfig = lookupAppConfig_UsingCDI();
        if(appConfig!=null) {
            LOG.info(String.format("Located AppConfig '%s' via CDI.", appConfig.getClass().getName()));
            
            return appConfig;
        }
        
        appConfig = lookupAppConfig_UsingServiceLoader();
        if(appConfig!=null) {
        	
        	val appConfigImpl = appConfig;
        	val appConfigClass = appConfig.getClass();
        	
            LOG.info(String.format("Located AppConfig '%s' via ServiceLoader.", appConfigClass.getName()));
            
            Supplier<Stream<Class<?>>> onDiscover = appConfigImpl.isisConfiguration()::streamClassesToDiscover;
            
            onDiscover.get()
            .forEach(type->probe.println("on discover include '%s'", type));
            
            // as we are in a non-managed environment, we need to bootstrap CDI ourself
            _CDI.init(onDiscover);
            
            return appConfig;
        }
        
//        appConfig = lookupAppConfig_UsingConfigProperties();
//        if(appConfig!=null) {
//            LOG.info(String.format("Located AppConfig '%s' using config properties.", appConfig.getClass().getName()));
//            return appConfig;    
//        }
        
        throw new IsisException("Failed to locate the AppConfig");
    }
    
    private static AppConfig lookupAppConfig_UsingCDI() {
        return _CDI.getManagedBean(AppConfig.class).orElse(null);
    }
    
    private static AppConfig lookupAppConfig_UsingServiceLoader() {
        
        return _Plugin.getOrElse(AppConfig.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(AppConfig.class, ambiguousPlugins);
                },
                ()->null);
    }
    
//    // to support pre 2.0.0-M2 behavior    
//    private static AppConfig lookupAppConfig_UsingConfigProperties() {
//        
//        IsisConfigurationBuilder builder = IsisConfigurationBuilder.getDefault();
//        String appManifestClassName =  builder.peekAtString("isis.appManifest");
//        
//        final Class<AppManifest> appManifestClass;
//        try {
//            appManifestClass = _Casts.uncheckedCast(_Context.loadClassAndInitialize(appManifestClassName));
//        } catch (ClassNotFoundException e) {
//            throw new IsisException(
//                    String.format(
//                            "Failed to locate the AppManifest using config property 'isis.appManifest=%s'.",
//                            appManifestClassName), 
//                    e);
//        }
//        
//        final AppManifest appManifest;
//        try {
//            appManifest = appManifestClass.newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new IsisException(
//                    String.format("Failed to create instance of AppManifest '%s'.", appManifestClass), e);
//        }
//        
//        // Note: AppConfig is a FunctionalInterface
//        return ()->IsisConfiguration.buildFromAppManifest(appManifest);
//        
//    }
    
    
    

}
