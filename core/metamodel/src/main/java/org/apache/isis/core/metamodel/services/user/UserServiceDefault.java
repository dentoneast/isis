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

package org.apache.isis.core.metamodel.services.user;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class UserServiceDefault implements UserService {

    @Programmatic
    @Override
    public UserMemento getUser() {

        final UserAndRoleOverrides userAndRoleOverrides = currentOverridesIfAny();

        if (userAndRoleOverrides != null) {

            final String username = userAndRoleOverrides.user;

            final Stream<String> roles;
            if (userAndRoleOverrides.roles != null) {
                roles = userAndRoleOverrides.streamRoles();
            } else {
                // preserve the roles if were not overridden
                roles = streamPreviousRoles();
            }

            final List<RoleMemento> roleMementos = asRoleMementos(roles);
            return new UserMemento(username, roleMementos);

        } else {
            final AuthenticationSession session =
                    authenticationSessionProvider.getAuthenticationSession();
            return session.createUserMemento();
        }
    }

    private Stream<String> streamPreviousRoles() {
        final AuthenticationSession session =
                authenticationSessionProvider.getAuthenticationSession();
        
        return session.streamRoles();
    }

    public static class UserAndRoleOverrides {
        final String user;
        final Set<String> roles;


        UserAndRoleOverrides(final String user) {
            this(user, null);
        }

        UserAndRoleOverrides(final String user, final Set<String> roles) {
            this.user = user;
            this.roles = roles;
        }

        public String getUser() {
            return user;
        }

        public Stream<String> streamRoles() {
            return stream(roles);
        }
    }

    private final ThreadLocal<Stack<UserAndRoleOverrides>> overrides =
            new ThreadLocal<Stack<UserAndRoleOverrides>>() {
        @Override protected Stack<UserAndRoleOverrides> initialValue() {
            return new Stack<>();
        }
    };


    private void overrideUserAndRoles(final String user, final Iterable<String> rolesIfAny) {
        
        final Stream<String> roles = rolesIfAny != null 
                ? stream(rolesIfAny)
                        : inheritRoles();
        
        final SortedSet<String> rolesSorted = roles.collect(Collectors.toCollection(TreeSet::new));
        this.overrides.get().push(new UserAndRoleOverrides(user, rolesSorted));
    }

    private void resetOverrides() {
        this.overrides.get().pop();
    }

    /**
     * Not API; for use by the implementation of sudo/runAs (see {@link SudoService} etc.
     */
    @Programmatic
    public UserAndRoleOverrides currentOverridesIfAny() {
        final Stack<UserAndRoleOverrides> userAndRoleOverrides = overrides.get();
        return !userAndRoleOverrides.empty()
                ? userAndRoleOverrides.peek()
                        : null;
    }

    private Stream<String> inheritRoles() {
        final UserAndRoleOverrides currentOverridesIfAny = currentOverridesIfAny();
        return currentOverridesIfAny != null
                ? currentOverridesIfAny.streamRoles()
                        : authenticationSessionProvider.getAuthenticationSession().streamRoles();
    }

    private static List<RoleMemento> asRoleMementos(final Stream<String> roles) {
        final List<RoleMemento> mementos = stream(roles)
                .map(RoleMemento::new)
                .collect(Collectors.toList());
        
        return mementos;
    }


    @DomainService(
            nature = NatureOfService.DOMAIN,
            menuOrder = "" + Integer.MAX_VALUE
            )
    public static class SudoServiceSpi implements SudoService.Spi {

        @Override
        public void runAs(final String username, final Iterable<String> roles) {
            userServiceDefault.overrideUserAndRoles(username, roles);
        }

        @Override
        public void releaseRunAs() {
            userServiceDefault.resetOverrides();
        }

        @Inject
        UserServiceDefault userServiceDefault;
    }

    @javax.inject.Inject
    AuthenticationSessionProvider authenticationSessionProvider;

}
