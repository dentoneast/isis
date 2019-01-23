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
package org.apache.isis.security.pac4j.authentication;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.standard.Authenticator;
import org.apache.isis.core.security.authentication.standard.SimpleSession;

public class Pac4jAuthenticator implements Authenticator {

	private final static _Probe probe = _Probe.unlimited().label("Pac4jAuthenticator");
	
	
    @Override
    public void init() {
        // TODO Auto-generated method stub

    	probe.println("init");
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        
        probe.println("shutdown");

    }

    @Override
    public boolean canAuthenticate(Class<? extends AuthenticationRequest> authenticationRequestClass) {
        // TODO Auto-generated method stub
        
    	probe.println("canAuthenticate authenticationRequestClass=%s", authenticationRequestClass);
        
        _Exceptions.dumpStackTrace(System.out, 0, 1000);
        
        return false;
    }

    @Override
    public AuthenticationSession authenticate(AuthenticationRequest request, String code) {
        
    	probe.println("authenticate request=%s, code=%s", request, code);
        
        String[] roles = {};
        return new SimpleSession(request.getName(), roles, code);
    }

    @Override
    public void logout(AuthenticationSession session) {
        // TODO Auto-generated method stub
        
    	probe.println("logout session=%s", session);

    }
    
    // -- HELPER
    


}
