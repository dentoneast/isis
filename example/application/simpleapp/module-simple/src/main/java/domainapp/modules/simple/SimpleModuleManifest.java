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
package domainapp.modules.simple;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.config.AppConfig;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.security.authentication.bypass.AuthenticatorBypass;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.security.authorization.bypass.AuthorizorBypass;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

/**
 * Used by <code>isis-maven-plugin</code> (build-time validation of the module) and also by module-level integration tests.
 */
@Singleton
public class SimpleModuleManifest extends AppManifestAbstract2 implements AppConfig {

    public static final Builder BUILDER = Builder.forModule(new SimpleModule())
            .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.schema.autoCreateAll","true")
            .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.identifier.case","MixedCase")
            ;

    public SimpleModuleManifest() {
        super(BUILDER);
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

}
