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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.system.MessageRegistry;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
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
@Singleton @ApplicationScoped
public class IsisSessionFactory {

    //private final static Logger LOG = LoggerFactory.getLogger(IsisSessionFactory.class);

    @Inject private IsisConfiguration configuration;
    
    private ServiceInjector serviceInjector;
    private SpecificationLoader specificationLoader;
    private AuthenticationManager authenticationManager;
    private AuthorizationManager authorizationManager;
    private PersistenceSessionFactory persistenceSessionFactory;
    private ServiceInitializer serviceInitializer;

    @PostConstruct
    public void init() {
        
        System.out.println("!!!!!!!!!!!!! IsisSessionFactory INIT " + hashCode());

        if(_Context.getIfAny(IsisSessionFactory.class)!=null) {
            return;
        }
        
        //System.out.println("!!!!!!!!!!!!! IsisSessionFactory INIT " + hashCode());
        
        final IsisComponentProvider componentProvider = IsisComponentProvider
                .builder()
                .build();
        
        final IsisSessionFactoryBuilder builder =
                new IsisSessionFactoryBuilder(componentProvider);
        
        // as a side-effect, if the metamodel turns out to be invalid, then
        // this will push the MetaModelInvalidException into IsisContext.
        IsisSessionFactory sessionFactory = builder.buildSessionFactory(()->this);
    }
    
    void setServicesInjector(ServiceInjector servicesInjector) {
        this.serviceInjector = servicesInjector;
        this.specificationLoader = IsisContext.getSpecificationLoader();
        this.authenticationManager = IsisContext.getAuthenticationManager();
        this.authorizationManager = IsisContext.getAuthorizationManager();
        this.persistenceSessionFactory = servicesInjector.lookupServiceElseFail(PersistenceSessionFactory.class);
    }

    void constructServices() {

        // do postConstruct.  We store the initializer to do preDestroy on shutdown
        serviceInitializer = new ServiceInitializer(configuration, serviceInjector.streamServices().collect(Collectors.toList()));
        serviceInitializer.validate();

        openSession(new InitialisationSession());

        try {
            //
            // postConstructInSession
            //

            IsisTransactionManager transactionManager = getCurrentSessionTransactionManager();
            transactionManager.executeWithinTransaction(serviceInitializer::postConstruct);

            //
            // installFixturesIfRequired
            //
            final FixturesInstallerFromConfiguration fixtureInstaller =
                    new FixturesInstallerFromConfiguration(this);
            fixtureInstaller.installFixtures();

            //
            // translateServicesAndEnumConstants
            //

            final List<Object> services = serviceInjector.streamServices().collect(Collectors.toList());
            final TitleService titleService = serviceInjector.lookupServiceElseFail(TitleService.class);
            for (Object service : services) {
                final String unused = titleService.titleOf(service);
                _Blackhole.consume(unused);
            }

            // (previously we took a protective copy to avoid a concurrent modification exception,
            // but this is now done by SpecificationLoader itself)
            for (final ObjectSpecification objSpec : IsisContext.getSpecificationLoader().allSpecifications()) {
                final Class<?> correspondingClass = objSpec.getCorrespondingClass();
                if(correspondingClass.isEnum()) {
                    final Object[] enumConstants = correspondingClass.getEnumConstants();
                    for (Object enumConstant : enumConstants) {
                        final String unused = titleService.titleOf(enumConstant);
                        _Blackhole.consume(unused);
                    }
                }
            }

            // as used by the Wicket UI
            final TranslationService translationService = serviceInjector.lookupServiceElseFail(TranslationService.class);

            final String context = IsisSessionFactoryBuilder.class.getName();
            final MessageRegistry messageRegistry = new MessageRegistry();
            final List<String> messages = messageRegistry.listMessages();
            for (String message : messages) {
                translationService.translate(context, message);
            }

        } finally {
            closeSession();
        }
    }

    @PreDestroy
    public void destroyServicesAndShutdown() {
        destroyServices();
        shutdown();
    }

    private void destroyServices() {
        // may not be set if the metamodel validation failed during initialization
        if (serviceInitializer == null) {
            return;
        }

        // call @PreDestroy (in a session)
        openSession(new InitialisationSession());
        IsisTransactionManager transactionManager = getCurrentSessionTransactionManager();
        try {
            transactionManager.startTransaction();
            try {

                serviceInitializer.preDestroy();

            } catch (RuntimeException ex) {
                transactionManager.getCurrentTransaction().setAbortCause(
                        new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                transactionManager.endTransaction();
            }
        } finally {
            closeSession();
        }
    }

    private void shutdown() {
        persistenceSessionFactory.shutdown();
        authenticationManager.shutdown();
        specificationLoader.shutdown();
    }

    // -- openSession, closeSession, currentSession, inSession
    
    /**
     * Inheritable... allows to have concurrent computations utilizing the ForkJoinPool.
     * see {@link IsisContext#compute(java.util.function.Supplier)}
     */ 
    private final InheritableThreadLocal<IsisSession> currentSession = new InheritableThreadLocal<>();

    /**
     * Creates and {@link IsisSession#open() open}s the {@link IsisSession}.
     */
    public IsisSession openSession(final AuthenticationSession authenticationSession) {

        closeSession();

        final PersistenceSession persistenceSession =
                persistenceSessionFactory.createPersistenceSession(serviceInjector, authenticationSession);
        IsisSession session = new IsisSession(authenticationSession, persistenceSession);
        currentSession.set(session);
        session.open();
        return session;
    }

    public void closeSession() {
        final IsisSession existingSessionIfAny = getCurrentSession();
        if (existingSessionIfAny == null) {
            return;
        }
        existingSessionIfAny.close();
        currentSession.set(null);
    }

    public IsisSession getCurrentSession() {
        return currentSession.get();
    }

    private IsisTransactionManager getCurrentSessionTransactionManager() {
        final IsisSession currentSession = getCurrentSession();
        return currentSession.getPersistenceSession().getTransactionManager();
    }

    /**
     * @return whether the calling thread is within the context of an open IsisSession
     */
    public boolean isInSession() {
        return getCurrentSession() != null;
    }

    /**
     * @return whether the calling thread is within the context of an open IsisTransaction
     */
    public boolean isInTransaction() {
        if (isInSession()) {
            if (getCurrentSession().getCurrentTransaction() != null) {
                if (!getCurrentSession().getCurrentTransaction().getState().isComplete()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * As per {@link #doInSession(Runnable, AuthenticationSession)}, using a default {@link InitialisationSession}.
     * @param runnable
     */
    public void doInSession(final Runnable runnable) {
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
    public void doInSession(final Runnable runnable, final AuthenticationSession authenticationSession) {
        doInSession(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
        }, authenticationSession);
    }

    /**
     * As per {@link #doInSession(Callable), AuthenticationSession}, using a default {@link InitialisationSession}.
     */
    public <R> R doInSession(final Callable<R> callable) {
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
    public <R> R doInSession(final Callable<R> callable, final AuthenticationSession authenticationSession) {
        final IsisSessionFactory sessionFactory = this;
        boolean noSession = !sessionFactory.isInSession();
        try {
            if (noSession) {
                sessionFactory.openSession(authenticationSession);
            }

            return callable.call();
        } catch (Exception x) {
            throw new RuntimeException(
                    String.format("An error occurred while executing code in %s session", noSession ? "a temporary" : "a"),
                    x);
        } finally {
            if (noSession) {
                sessionFactory.closeSession();
            }
        }
    }

    // -- component accessors

    /**
     * The {@link ApplicationScopedComponent application-scoped} {@link ServiceInjector}.
     * @deprecated use IsisContext instead
     */
    public ServiceInjector getServicesInjector() {
        return serviceInjector;
    }

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link SpecificationLoader}.
     * @deprecated use IsisContext instead
     */
    @Produces @ApplicationScoped
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * The {@link AuthenticationManager} that will be used to authenticate and
     * create {@link AuthenticationSession}s
     * {@link IsisSession#getAuthenticationSession() within} the
     * {@link IsisSession}.
     * @deprecated use IsisContext instead
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * The {@link AuthorizationManager} that will be used to authorize access to
     * domain objects.
     * @deprecated use IsisContext instead
     */
    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    /**
     * The {@link org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory} that will be used to create
     * {@link PersistenceSession} {@link IsisSession#getPersistenceSession()
     * within} the {@link IsisSession}.
     * @deprecated use IsisContext instead
     */
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return persistenceSessionFactory;
    }


}
