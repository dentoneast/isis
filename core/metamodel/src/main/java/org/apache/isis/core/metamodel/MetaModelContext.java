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
package org.apache.isis.core.metamodel;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface MetaModelContext {

    // -- INTERFACE
    
    IsisConfiguration getConfiguration();
    
    ObjectAdapterProvider adapterProvider();

    ServiceInjector getServiceInjector();

    ServiceRegistry getServiceRegistry();

    SpecificationLoader getSpecificationLoader();

    AuthenticationSessionProvider getAuthenticationSessionProvider();

    TranslationService getTranslationService();

    AuthenticationSession getAuthenticationSession();

    AuthorizationManager getAuthorizationManager();

    AuthenticationManager getAuthenticationManager();

    TitleService getTitleService();

    ObjectSpecification getSpecification(Class<?> type);

    // -- PRESET INSTANCES
    
    static MetaModelContext current() {
        return _Context.computeIfAbsent(MetaModelContext.class, __->MetaModelContexts.usingCDI); // default
    }
    
    static void preset(MetaModelContext metaModelContext) {
        _Context.putSingleton(MetaModelContext.class, metaModelContext);
    }
    
    // -- DELEGATION - FOR THOSE THAT IMPLEMENT THROUGH DELEGATION
    
    public static interface Delegating extends MetaModelContext {
        
        public MetaModelContext getMetaModelContext();
        
        public default IsisConfiguration getConfiguration() {
            return getMetaModelContext().getConfiguration();
        }
        
        public default ObjectAdapterProvider adapterProvider() {
            return getMetaModelContext().adapterProvider();
        }

        public default ServiceInjector getServiceInjector() {
            return getMetaModelContext().getServiceInjector();
        }

        public default ServiceRegistry getServiceRegistry() {
            return getMetaModelContext().getServiceRegistry();
        }

        public default SpecificationLoader getSpecificationLoader() {
            return getMetaModelContext().getSpecificationLoader();
        }

        public default AuthenticationSessionProvider getAuthenticationSessionProvider() {
            return getMetaModelContext().getAuthenticationSessionProvider();
        }

        public default TranslationService getTranslationService() {
            return getMetaModelContext().getTranslationService();
        }

        public default AuthenticationSession getAuthenticationSession() {
            return getMetaModelContext().getAuthenticationSession();
        }

        public default AuthorizationManager getAuthorizationManager() {
            return getMetaModelContext().getAuthorizationManager();
        }

        public default AuthenticationManager getAuthenticationManager() {
            return getMetaModelContext().getAuthenticationManager();
        }

        public default TitleService getTitleService() {
            return getMetaModelContext().getTitleService();
        }

        public default ObjectSpecification getSpecification(Class<?> type) {
            return getMetaModelContext().getSpecification(type);
        }
        
    }

   
    
}
