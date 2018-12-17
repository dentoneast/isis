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
package org.apache.isis.core.security.authspi;

import java.util.List;
import java.util.Set;

import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.core.security.authentication.standard.Authenticator;
import org.apache.isis.core.security.authorization.standard.Authorizor;

/**
 * @since 2.0.0-M2
 * 
 * For now this is just a stub, meant to replace Installers. 
 * 
 */
public interface SecurityAuthPlugin {

    // -- INTERFACE
    
    public List<Authenticator> getAuthenticators();
    public List<Authorizor> getAuthorizors();
    
    // -- LOOKUP
    
    /**
     * not cached
     * @return
     */
    public static Set<SecurityAuthPlugin> loadAll() {
        return _Plugin.loadAll(SecurityAuthPlugin.class);
    }
    
}
