/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.plugins.ioc.weld.scopes.conversation;

import static org.apache.isis.commons.internal.base._With.requiresNotEmpty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.core.plugins.ioc.ConversationContextHandle;
import org.apache.isis.core.plugins.ioc.ConversationContextService;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.MutableBoundRequest;

import lombok.val;

//@Singleton @Alternative @Priority(10)
@Deprecated //TODO [2033] not needed, since we are using seam SPI
class ConversationContextServiceWeld implements ConversationContextService {

    private final static String TRANSIENT_CID = null;
    
    @Inject BoundConversationContext conversationContext;

    /* Start a transient conversation */
    @Override
    public ConversationContextHandle startTransientConversation() {

    	if(isActive()) {
	        return null; // if already active, don't return a handle
	    }
    
    	//Provide a data store which will last the lifetime of the request
        //and one that will last the lifetime of the session
    	final Map<String, Object> requestDataStore = new ConcurrentHashMap<>();
    	final Map<String, Object> sessionDataStore = new ConcurrentHashMap<>();
    	
    	val mutableBoundRequest = new MutableBoundRequest(requestDataStore, sessionDataStore);
    	
        resumeOrStartConversation(mutableBoundRequest, TRANSIENT_CID);
        
        final Consumer<String> onResume = 
        		cid->resumeOrStartConversation(mutableBoundRequest, requiresNotEmpty(cid, "cid"));
        		
		final Runnable onClose = 
				()->endOrPassivateConversation(mutableBoundRequest);
		
	    return ConversationContextHandleWeld.of(onResume, onClose);
        
    }
    
    // -- HELPER

    private boolean isActive() {
    	return conversationContext.isActive();
    }
    

    /* Start a transient conversation (if cid is null) or resume a non-transient */
    /* conversation. Provide a data store which will last the lifetime of the request */
    /* and one that will last the lifetime of the session */
    private void resumeOrStartConversation(
    		MutableBoundRequest mutableBoundRequest,
            String cid) {

        /* Associate the stores with the context and activate the context */
        /* BoundRequest just wraps the two datastores */
        conversationContext.associate(mutableBoundRequest);

        // Pass the cid in
        conversationContext.activate(cid);

    }

    
    /* End the conversations, providing the same data store as was used to start */
    /* the request. Any transient conversations will be destroyed, any newly-promoted */
    /* conversations will be placed into the session */
    private void endOrPassivateConversation(MutableBoundRequest mutableBoundRequest) {

        try {

            /* Invalidate the conversation (all transient conversations will be scheduled for destruction) */
            conversationContext.invalidate();

            /* Deactivate the conversation, causing all transient conversations to be destroyed */
            conversationContext.deactivate();

        } finally {

            /* Ensure that whatever happens we dissociate to prevent memory leaks*/
            conversationContext.dissociate(mutableBoundRequest);

        }

    }

}
