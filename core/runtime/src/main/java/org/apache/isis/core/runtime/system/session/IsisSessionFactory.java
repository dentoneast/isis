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

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

/**
 * Is the factory of {@link IsisSession}s, also holding a reference to the current session using
 * a thread-local.
 *
 * <p>
 *     The class can in considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 * <p>
 *     The class is only instantiated once; it is also registered with {@link ServiceInjector}, meaning that
 *     it can be {@link Inject}'d into other domain services.
 * </p>
 */
public interface IsisSessionFactory {

    public void destroyServicesAndShutdown();

    public IsisSession openSession(final AuthenticationSession authenticationSession);
    public void closeSession();

    public IsisSession getCurrentSession();


    /**
     * @return whether the calling thread is within the context of an open IsisSession
     */
    public boolean isInSession();

    /**
     * @return whether the calling thread is within the context of an open IsisTransaction
     */
    public boolean isInTransaction();

    /**
     * As per {@link #doInSession(Runnable, AuthenticationSession)}, using a default {@link InitialisationSession}.
     * @param runnable
     */
    public default void doInSession(final Runnable runnable) {
        doInSession(runnable, new InitialisationSession());
    }

    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param runnable The piece of code to run.
     * @param authenticationSession
     */
    public default void doInSession(final Runnable runnable, final AuthenticationSession authenticationSession) {
    	final Callable<Void> callable = ()->{runnable.run(); return null;};
        doInSession(callable, authenticationSession);
    }

    /**
     * As per {@link #doInSession(Callable), AuthenticationSession}, using a default {@link InitialisationSession}.
     */
    public default <R> R doInSession(final Callable<R> callable) {
        return doInSession(callable, new InitialisationSession());
    }

    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param callable The piece of code to run.
     * @param authenticationSession - the user to run under
     */
    public <R> R doInSession(final Callable<R> callable, final AuthenticationSession authenticationSession);

    // -- component accessors

    /**
     * The {@link ApplicationScopedComponent application-scoped} {@link ServiceInjector}.
     * @deprecated use IsisContext instead
     */
    public ServiceInjector getServiceInjector();

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link SpecificationLoader}.
     * @deprecated use IsisContext instead
     */
    public SpecificationLoader getSpecificationLoader();

    /**
     * The {@link AuthenticationManager} that will be used to authenticate and
     * create {@link AuthenticationSession}s
     * {@link IsisSession#getAuthenticationSession() within} the
     * {@link IsisSession}.
     * @deprecated use IsisContext instead
     */
    public AuthenticationManager getAuthenticationManager();

    /**
     * The {@link AuthorizationManager} that will be used to authorize access to
     * domain objects.
     * @deprecated use IsisContext instead
     */
    public AuthorizationManager getAuthorizationManager();

    /**
     * The {@link org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory} that will be used to create
     * {@link PersistenceSession} {@link IsisSession#getPersistenceSession()
     * within} the {@link IsisSession}.
     * @deprecated use IsisContext instead
     */
    public PersistenceSessionFactory getPersistenceSessionFactory();


}
