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
package domainapp.application.manifest;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.config.AppConfig;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.core.security.authentication.bypass.AuthenticatorBypass;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.security.authorization.bypass.AuthorizorBypass;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import domainapp.application.DomainAppApplicationModule;
import domainapp.modules.spring.SpringModule;

/**
 * Bootstrap the application.
 */
public class DomainAppAppManifest extends AppManifestAbstract2 implements AppConfig {

    public static final Builder BUILDER = Builder
            .forModule(new DomainAppApplicationModule())
            .withConfigurationPropertiesFile(
                    DomainAppAppManifest.class, "isis-non-changing.properties")
            .withAuthMechanism("shiro")
            .withAdditionalModules(SpringModule.class)
            ;

    public DomainAppAppManifest() {
        super(BUILDER);
        
        ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
        		ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;
    }

	// Implementing AppConfig, to tell the framework how to bootstrap the configuration.
    @Override @Produces @Singleton
    public IsisConfiguration isisConfiguration() {
        return IsisConfiguration.buildFromAppManifest(this);
    }
    
	 /**
     * The standard authentication manager, configured with the 'bypass' authenticator 
     * (allows all requests through).
     * <p>
     * integration tests ignore appManifest for authentication and authorization.
     */
    @Produces @Singleton
    public AuthenticationManager authenticationManagerWithBypass() {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard();
        authenticationManager.addAuthenticator(new AuthenticatorBypass());
        return authenticationManager;
    }
    
    @Produces @Singleton
    public AuthorizationManager authorizationManagerWithBypass() {
        final AuthorizationManagerStandard authorizationManager = new AuthorizationManagerStandard() {
            {
                authorizor = new AuthorizorBypass();
            }  
        };
        return authorizationManager;
    }

    @Produces @Singleton
    public WebAppConfigBean webAppConfigBean() {
        return WebAppConfigBean.builder()
                .build();
    }
    
}
