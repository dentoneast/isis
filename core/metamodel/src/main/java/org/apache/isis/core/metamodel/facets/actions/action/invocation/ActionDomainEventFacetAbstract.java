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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import java.util.Map;

import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ActionInteractionContext;
import org.apache.isis.core.metamodel.interactions.ActionValidityContext;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public abstract class ActionDomainEventFacetAbstract
        extends SingleClassValueFacetAbstract implements ActionDomainEventFacet {

    private Class<? extends ActionDomainEvent<?>> eventType;
    private final TranslationService translationService;
    private final String translationContext;

    static Class<? extends Facet> type() {
        return ActionDomainEventFacet.class;
    }

    private final DomainEventHelper domainEventHelper;

    public ActionDomainEventFacetAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final FacetHolder holder,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader) {
        super(type(), holder, eventType, specificationLoader);
        this.eventType = eventType;

        this.translationService = servicesInjector.lookupService(TranslationService.class);
        // sadness: same as in TranslationFactory
        this.translationContext = ((IdentifiedHolder)holder).getIdentifier().toClassAndNameIdentityString();

        domainEventHelper = new DomainEventHelper(servicesInjector);
    }

    @Override
    public Class<?> value() {
        return eventType;
    }

    protected Class eventType() {
        return eventType;
    }

    public Class<? extends ActionDomainEvent<?>> getEventType() {
        return eventType;
    }
    public void setEventType(final Class<? extends ActionDomainEvent<?>> eventType) {
        this.eventType = eventType;
    }



    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.HIDE,
                        eventType(), null,
                        actionFrom(ic), getIdentified(),
                        ic.getTarget(), ic.getMixedIn(), argumentAdaptersFrom(ic),
                        null,
                        null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(UsabilityContext<? extends UsabilityEvent> ic) {

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.DISABLE,
                        eventType(), null,
                        actionFrom(ic), getIdentified(),
                        ic.getTarget(), ic.getMixedIn(), argumentAdaptersFrom(ic),
                        null,
                        null);
        if (event != null && event.isDisabled()) {
            final TranslatableString reasonTranslatable = event.getDisabledReasonTranslatable();
            if(reasonTranslatable != null) {
                return reasonTranslatable.translate(translationService, translationContext);
            }
            return event.getDisabledReason();

        }
        return null;
    }

    private static ObjectAction actionFrom(final InteractionContext<?> ic) {
        if(!(ic instanceof ActionInteractionContext)) {
            throw new IllegalStateException(
                    "Expecting ic to be of type ActionInteractionContext, instead was: " + ic);
        }
        return ((ActionInteractionContext) ic).getObjectAction();
    }

    private static ObjectAdapter[] argumentAdaptersFrom(final InteractionContext<? extends InteractionEvent> ic) {
        final Map<Integer, ObjectAdapter> contributeeAsMap = ic.getContributeeAsMap();
        return contributeeAsMap.isEmpty() ? null : new ObjectAdapter[]{contributeeAsMap.get(0)};
    }

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> ic) {

        final ActionValidityContext aic = (ActionValidityContext) ic;
        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.VALIDATE,
                        eventType(), null,
                        actionFrom(ic), getIdentified(),
                        ic.getTarget(), ic.getMixedIn(), aic.getArgs(),
                        null,
                        null);
        if (event != null && event.isInvalid()) {
            final TranslatableString reasonTranslatable = event.getInvalidityReasonTranslatable();
            if(reasonTranslatable != null) {
                return reasonTranslatable.translate(translationService, translationContext);
            }
            return event.getInvalidityReason();
        }

        return null;
    }

}
