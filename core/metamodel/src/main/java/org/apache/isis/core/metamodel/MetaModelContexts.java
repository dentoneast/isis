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
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
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
public final class MetaModelContexts {

    static class MetaModelContextUsingCDI implements MetaModelContext {
        
        @Override
        public final IsisConfiguration getConfiguration() {
            return _Config.getConfiguration();
        }
        
        @Override
        public final ObjectAdapterProvider adapterProvider() {
            return _CDI.getSingleton(ObjectAdapterProvider.class);
        }
        
        @Override
        public final ServiceInjector getServiceInjector() {
            return _CDI.getSingleton(ServiceInjector.class);
        }

        @Override
        public final ServiceRegistry getServiceRegistry() {
            return _CDI.getSingleton(ServiceRegistry.class);
        }
        
        @Override
        public final SpecificationLoader getSpecificationLoader() {
            return _CDI.getSingleton(SpecificationLoader.class);
        }

        @Override
        public final AuthenticationSessionProvider getAuthenticationSessionProvider() {
            return _CDI.getSingleton(AuthenticationSessionProvider.class);
        }
        
        @Override
        public final TranslationService getTranslationService() {
            return _CDI.getSingleton(TranslationService.class);
        }
        
        @Override
        public final AuthenticationSession getAuthenticationSession() {
            return getAuthenticationSessionProvider().getAuthenticationSession();
        }
        
        @Override
        public final AuthorizationManager getAuthorizationManager() {
            return _CDI.getSingleton(AuthorizationManager.class); 
        }
        
        @Override
        public final AuthenticationManager getAuthenticationManager() {
            return _CDI.getSingleton(AuthenticationManager.class); 
        }
        
        @Override
        public final TitleService getTitleService() {
            return getServiceRegistry().lookupServiceElseFail(TitleService.class);
        }
        
        @Override
        public final ObjectSpecification getSpecification(final Class<?> type) {
            return type != null ? getSpecificationLoader().loadSpecification(type) : null;
        }
        
    }
    
    final static MetaModelContext usingCDI = new MetaModelContextUsingCDI();
    
}
