package org.apache.isis.core.plugins.ioc.weld.services.request;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.MutableBoundRequest;

import org.apache.isis.core.plugins.ioc.ConversationContextService;

@Singleton //FIXME [2033] just a copy and paste stub yet
public class ConversationContextServiceWeld implements ConversationContextService {

    @Inject BoundConversationContext conversationContext;


    /* Start a transient conversation */
    /* Provide a data store which will last the lifetime of the request */
    /* and one that will last the lifetime of the session */
    public void startTransientConversation(Map<String, Object> requestDataStore,
            Map<String, Object> sessionDataStore) {

        resumeOrStartConversation(requestDataStore, sessionDataStore, null);
    }


    /* Start a transient conversation (if cid is null) or resume a non-transient */
    /* conversation. Provide a data store which will last the lifetime of the request */
    /* and one that will last the lifetime of the session */
    public void resumeOrStartConversation(
            Map<String, Object> requestDataStore,
            Map<String, Object> sessionDataStore,
            String cid) {

        /* Associate the stores with the context and activate the context */
        /* BoundRequest just wraps the two datastores */
        conversationContext.associate(new MutableBoundRequest(requestDataStore, sessionDataStore));

        // Pass the cid in
        conversationContext.activate(cid);

    }


    /* End the conversations, providing the same data store as was used to start */
    /* the request. Any transient conversations will be destroyed, any newly-promoted */
    /* conversations will be placed into the session */
    public void endOrPassivateConversation(
            Map<String, Object> requestDataStore,
            Map<String, Object> sessionDataStore) {

        try {

            /* Invalidate the conversation (all transient conversations will be scheduled for destruction) */
            conversationContext.invalidate();

            /* Deactivate the conversation, causing all transient conversations to be destroyed */
            conversationContext.deactivate();

        } finally {

            /* Ensure that whatever happens we dissociate to prevent memory leaks*/
            conversationContext.dissociate(new MutableBoundRequest(requestDataStore, sessionDataStore));

        }

    }

}
