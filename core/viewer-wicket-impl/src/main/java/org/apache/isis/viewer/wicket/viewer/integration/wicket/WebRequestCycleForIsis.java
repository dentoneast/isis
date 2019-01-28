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
package org.apache.isis.viewer.wicket.viewer.integration.wicket;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Throwables;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.plugins.ioc.ConversationContextHandle;
import org.apache.isis.core.plugins.ioc.ConversationContextService;
import org.apache.isis.core.plugins.ioc.RequestContextHandle;
import org.apache.isis.core.plugins.ioc.RequestContextService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.MessageBroker;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.isis.viewer.wicket.ui.pages.mmverror.MmvErrorPage;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Isis-specific implementation of the Wicket's {@link RequestCycle},
 * automatically opening a {@link IsisSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 */
@Slf4j
public class WebRequestCycleForIsis implements IRequestCycleListener {

    private PageClassRegistry pageClassRegistry;
    
    private final static _Probe probe = _Probe.unlimited().label("WebRequestCycleForIsis");
    
    private _Lazy<RequestContextService> requestContextService = _Lazy.of(()->
        _CDI.getSingleton(RequestContextService.class));
    
    public final static MetaDataKey<RequestContextHandle> REQUEST_CONTEXT_HANDLE_KEY 
        = new MetaDataKey<RequestContextHandle>() {
            private static final long serialVersionUID = 1L; };
            
    @Override
    public synchronized void onBeginRequest(RequestCycle requestCycle) {
        
        probe.println("onBeginRequest in");
        
        // this handle needs to be closed when the request-scope's life-cycle ends
        // so we store it onto the requestCycle as meta-data entry, to be 
        // retrieved later at 'onEndRequest'
        val requestContextHandle = requestContextService.get().startRequest();
        if(requestContextHandle!=null) {
            requestCycle.setMetaData(REQUEST_CONTEXT_HANDLE_KEY, requestContextHandle);
            probe.println("request context created");
        } else {
        	probe.println("no request context created");
		}

        if (!Session.exists()) {
            
            probe.println("onBeginRequest out - session was not opened (because no Session)");
            return;
        }

        final AuthenticatedWebSessionForIsis wicketSession = AuthenticatedWebSessionForIsis.get();
        final AuthenticationSession authenticationSession = wicketSession.getAuthenticationSession();
        if (authenticationSession == null) {
            probe.println("onBeginRequest out - session was not opened (because no authenticationSession)");
            return;
        }

        getIsisSessionFactory().openSession(authenticationSession);
        getTransactionManager().startTransaction();
        
        probe.println("onBeginRequest out - session was opened");
    }

    @Override
    public void onRequestHandlerResolved(final RequestCycle cycle, final IRequestHandler handler) {

        probe.println("onRequestHandlerResolved in");
        
        if(handler instanceof RenderPageRequestHandler) {
            ConcurrencyChecking.disable();

            val metaModelDeficiencies = IsisContext.getMetaModelDeficienciesIfAny();

            if(metaModelDeficiencies != null) {
                RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                final IRequestablePage nextPage = requestHandler.getPage();
                if(nextPage instanceof ErrorPage || nextPage instanceof MmvErrorPage) {
                    // do nothing
                    return;
                }
                throw new MetaModelInvalidException(metaModelDeficiencies);
            }
        }
        
        probe.println("onRequestHandlerResolved out");

    }


    /**
     * Is called prior to {@link #onEndRequest(RequestCycle)}, and offers the opportunity to
     * throw an exception.
     */
    @Override
    public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
        log.debug("onRequestHandlerExecuted: handler: {}", handler);
        
        probe.println("onRequestHandlerExecuted");

        if(handler instanceof RenderPageRequestHandler) {
            ConcurrencyChecking.reset(ConcurrencyChecking.CHECK);
        }

        if (getIsisSessionFactory().isInSession()) {
            try {
                // will commit (or abort) the transaction;
                // an abort will cause the exception to be thrown.
                getTransactionManager().endTransaction();
            } catch(Exception ex) {
                // will redirect to error page after this,
                // so make sure there is a new transaction ready to go.
                if(getTransactionManager().getCurrentTransaction().getState().isComplete()) {
                    getTransactionManager().startTransaction();
                }
                if(handler instanceof RenderPageRequestHandler) {
                    RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                    if(requestHandler.getPage() instanceof ErrorPage) {
                        // do nothing
                        return;
                    }
                }

                // shouldn't return null given that we're in a session ...
                PageProvider errorPageProvider = errorPageProviderFor(ex);
                throw new RestartResponseException(errorPageProvider, RedirectPolicy.ALWAYS_REDIRECT);
            }
        }
    }

    /**
     * It is not possible to throw exceptions here, hence use of {@link #onRequestHandlerExecuted(RequestCycle, IRequestHandler)}.
     */
    @Override
    public synchronized void onEndRequest(RequestCycle requestCycle) {
        
        probe.println("onEndRequest");
        
        if (getIsisSessionFactory().isInSession()) {
            try {
                // belt and braces
                getTransactionManager().endTransaction();
            } finally {
                getIsisSessionFactory().closeSession();
            }
        }

        // detach the current @RequestScope, if any
        val handle = requestCycle.getMetaData(REQUEST_CONTEXT_HANDLE_KEY);
        requestCycle.setMetaData(REQUEST_CONTEXT_HANDLE_KEY, null);
        //RequestContextService.closeHandle(handle); //FIXME [2033] too early, as of commented out ... memory leak
        
        
    }


    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {
        
        probe.println("onException");

    	val metaModelDeficiencies = IsisContext.getMetaModelDeficienciesIfAny();
        if(metaModelDeficiencies != null) {
            final Set<String> validationErrors = metaModelDeficiencies.getValidationErrors();
            final MmvErrorPage mmvErrorPage = new MmvErrorPage(validationErrors);
            return new RenderPageRequestHandler(new PageProvider(mmvErrorPage), RedirectPolicy.ALWAYS_REDIRECT);
        }

        try {

            // adapted from http://markmail.org/message/un7phzjbtmrrperc
            if(ex instanceof ListenerInvocationNotAllowedException) {
                final ListenerInvocationNotAllowedException linaex = (ListenerInvocationNotAllowedException) ex;
                if(linaex.getComponent() != null && PromptFormAbstract.ID_CANCEL_BUTTON.equals(linaex.getComponent().getId())) {
                    // no message.
                    // this seems to occur when press ESC twice in rapid succession on a modal dialog.
                } else {
                    addMessage(null);

                }
                return respondGracefully(cycle);
            }


            // handle recognised exceptions gracefully also
            final Stream<ExceptionRecognizer> exceptionRecognizers =
                    getServicesInjector().streamServices(ExceptionRecognizer.class);
            String recognizedMessageIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
            if(recognizedMessageIfAny != null) {
                return respondGracefully(cycle);
            }

            final List<Throwable> causalChain = Throwables.getCausalChain(ex);
            final Optional<Throwable> hiddenIfAny = causalChain.stream()
                    .filter(ObjectMember.HiddenException.isInstanceOf()).findFirst();
            if(hiddenIfAny.isPresent()) {
                addMessage("hidden");
                return respondGracefully(cycle);
            }
            final Optional<Throwable> disabledIfAny = causalChain.stream()
                    .filter(ObjectMember.DisabledException.isInstanceOf()).findFirst();
            if(disabledIfAny.isPresent()) {
                addTranslatedMessage(disabledIfAny.get().getMessage());
                return respondGracefully(cycle);
            }

        } catch(Exception ignoreFailedAttemptToGracefullyHandle) {
            // if any of this graceful responding fails, then fall back to original handling
        }

        PageProvider errorPageProvider = errorPageProviderFor(ex);
        // avoid infinite redirect loops
        RedirectPolicy redirectPolicy = ex instanceof PageExpiredException
                ? RedirectPolicy.NEVER_REDIRECT
                        : RedirectPolicy.ALWAYS_REDIRECT;
        return errorPageProvider != null
                ? new RenderPageRequestHandler(errorPageProvider, redirectPolicy)
                        : null;
    }

    private IRequestHandler respondGracefully(final RequestCycle cycle) {
        final IRequestablePage page = PageRequestHandlerTracker.getFirstHandler(cycle).getPage();
        final PageProvider pageProvider = new PageProvider(page);
        return new RenderPageRequestHandler(pageProvider);
    }

    private void addMessage(final String message) {
        final String translatedMessage = translate(message);
        addTranslatedMessage(translatedMessage);
    }

    private void addTranslatedMessage(final String translatedSuffixIfAny) {
        final String translatedPrefix = translate("Action no longer available");
        final String message = translatedSuffixIfAny != null
                ? String.format("%s (%s)", translatedPrefix, translatedSuffixIfAny)
                        : translatedPrefix;
                getMessageBroker().addMessage(message);
    }

    private String translate(final String text) {
        if(text == null) {
            return null;
        }
        return getTranslationService().translate(WebRequestCycleForIsis.class.getName(), text);
    }

    protected PageProvider errorPageProviderFor(Exception ex) {
        IRequestablePage errorPage = errorPageFor(ex);
        return errorPage != null? new PageProvider(errorPage): null;
    }

    // special case handling for PageExpiredException, otherwise infinite loop
    private final static ExceptionRecognizerForType pageExpiredExceptionRecognizer =
            new ExceptionRecognizerForType(PageExpiredException.class, new Function<String,String>(){
                @Override
                public String apply(String input) {
                    return "Requested page is no longer available.";
                }
            });

    protected IRequestablePage errorPageFor(Exception ex) {
        List<ExceptionRecognizer> exceptionRecognizers = _Lists.newArrayList();
        exceptionRecognizers.add(pageExpiredExceptionRecognizer);

        if(inIsisSession()) {
            getServicesInjector().streamServices(ExceptionRecognizer.class)
            .forEach(exceptionRecognizers::add);
        } else {
            val metaModelDeficiencies = IsisContext.getMetaModelDeficienciesIfAny();
            if(metaModelDeficiencies != null) {
                Set<String> validationErrors = metaModelDeficiencies.getValidationErrors();
                return new MmvErrorPage(validationErrors);
            }
            // not sure whether this can ever happen now...
            log.warn("Unable to obtain exceptionRecognizers (no session), "
                    + "will be treated as unrecognized exception", ex);
        }
        String recognizedMessageIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        ExceptionModel exceptionModel = ExceptionModel.create(recognizedMessageIfAny, ex);

        return isSignedIn() ? new ErrorPage(exceptionModel) : newSignInPage(exceptionModel);
    }

    /**
     * Tries to instantiate the configured {@link PageType#SIGN_IN signin page} with the given exception model
     *
     * @param exceptionModel A model bringing the information about the occurred problem
     * @return An instance of the configured signin page
     */
    private IRequestablePage newSignInPage(final ExceptionModel exceptionModel) {
        Class<? extends Page> signInPageClass = null;
        if (pageClassRegistry != null) {
            signInPageClass = pageClassRegistry.getPageClass(PageType.SIGN_IN);
        }
        if (signInPageClass == null) {
            signInPageClass = WicketSignInPage.class;
        }
        final PageParameters parameters = new PageParameters();
        Page signInPage;
        try {
            Constructor<? extends Page> constructor = signInPageClass.getConstructor(PageParameters.class, ExceptionModel.class);
            signInPage = constructor.newInstance(parameters, exceptionModel);
        } catch (Exception ex) {
            try {
                IPageFactory pageFactory = Application.get().getPageFactory();
                signInPage = pageFactory.newPage(signInPageClass, parameters);
            } catch (Exception x) {
                throw new WicketRuntimeException("Cannot instantiate the configured sign in page", x);
            }
        }
        return signInPage;
    }

    /**
     * TODO: this is very hacky...
     *
     * <p>
     * Matters should improve once ISIS-299 gets implemented...
     */
    protected boolean isSignedIn() {
        if(!inIsisSession()) {
            return false;
        }
        if(getAuthenticationSession() == null) {
            return false;
        }
        return getWicketAuthenticationSession().isSignedIn();
    }


    public void setPageClassRegistry(PageClassRegistry pageClassRegistry) {
        this.pageClassRegistry = pageClassRegistry;
    }

    // -- Dependencies (from isis' context)
    
    
    
    protected ServiceInjector getServicesInjector() {
        return getIsisSessionFactory().getServiceInjector();
    }

    protected IsisTransactionManager getTransactionManager() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession().getTransactionManager();
    }

    protected boolean inIsisSession() {
        return getIsisSessionFactory().isInSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }


    TranslationService getTranslationService() {
        return getServicesInjector().lookupService(TranslationService.class).orElse(null);
    }

    // -- Dependencies (from wicket)

    protected AuthenticatedWebSession getWicketAuthenticationSession() {
        return AuthenticatedWebSession.get();
    }



}
