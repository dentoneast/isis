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

package org.apache.isis.core.runtime.system.session;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;

public class IsisInjectModule {

    @Inject private IsisConfiguration isisConfiguration;
    
    @Produces @ApplicationScoped
    protected IsisSessionFactory provideIsisSessionFactory() {
        
        final AppManifest appManifest = isisConfiguration.getAppManifest();
        
        final IsisComponentProvider componentProvider = IsisComponentProvider
                .builder(appManifest)
                .build();
        
        final IsisSessionFactoryBuilder builder =
                new IsisSessionFactoryBuilder(componentProvider);
        
        // as a side-effect, if the metamodel turns out to be invalid, then
        // this will push the MetaModelInvalidException into IsisContext.
        IsisSessionFactory sessionFactory = builder.buildSessionFactory();
        
        return sessionFactory;
    }
    
    public static class Nested {
        
        @Inject private IsisSessionFactory isisSessionFactory;
    
        @Produces @ApplicationScoped
        protected ServicesInjector getServicesInjector() {
            return isisSessionFactory.getServicesInjector();
        }
        
        @Produces @ApplicationScoped
        protected SpecificationLoader getSpecificationLoader() {
            return isisSessionFactory.getSpecificationLoader();
        }
        
    }
    
    
    
    
    


}
