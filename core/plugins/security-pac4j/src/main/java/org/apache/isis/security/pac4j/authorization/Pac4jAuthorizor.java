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
package org.apache.isis.security.pac4j.authorization;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.security.authorization.standard.Authorizor;

public class Pac4jAuthorizor implements Authorizor {

    @Override
    public void init() {
        // TODO Auto-generated method stub
        System.out.println("!!! Pac4jAuthorizor.init");
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        System.out.println("!!! Pac4jAuthorizor.shutdown");
    }

    @Override
    public boolean isVisibleInAnyRole(Identifier identifier) {
        System.out.println("!!! Pac4jAuthorizor.isVisibleInAnyRole identifier="+identifier);
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isUsableInAnyRole(Identifier identifier) {
        System.out.println("!!! Pac4jAuthorizor.isUsableInAnyRole identifier="+identifier);
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isVisibleInRole(String role, Identifier identifier) {
        System.out.println("!!! Pac4jAuthorizor.isVisibleInRole role="+role+", identifier="+identifier);
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isUsableInRole(String role, Identifier identifier) {
        System.out.println("!!! Pac4jAuthorizor.isUsableInRole role="+role+", identifier="+identifier);
        // TODO Auto-generated method stub
        return true;
    }

}
