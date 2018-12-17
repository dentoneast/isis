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

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.standard.Authenticator;

public class Pac4jAuthenticator implements Authenticator {

    @Override
    public void init() {
        // TODO Auto-generated method stub

        System.out.println("!!! Pac4jAuthenticator.init");
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        
        System.out.println("!!! Pac4jAuthenticator.shutdown");

    }

    @Override
    public boolean canAuthenticate(Class<? extends AuthenticationRequest> authenticationRequestClass) {
        // TODO Auto-generated method stub
        
        System.out.println("!!! Pac4jAuthenticator.canAuthenticate authenticationRequestClass="+authenticationRequestClass);
        
        
        _Exceptions.dumpStackTrace(System.out, 0, 1000);
        
        return true;
    }

    @Override
    public AuthenticationSession authenticate(AuthenticationRequest request, String code) {
        
        System.out.println("!!! Pac4jAuthenticator.authenticate request=" + request + ", code="+code);
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void logout(AuthenticationSession session) {
        // TODO Auto-generated method stub
        
        System.out.println("!!! Pac4jAuthenticator.logout session="+session);

    }

}
