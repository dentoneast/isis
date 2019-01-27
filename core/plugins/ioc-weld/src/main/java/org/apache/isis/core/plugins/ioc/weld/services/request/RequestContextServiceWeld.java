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
package org.apache.isis.core.plugins.ioc.weld.services.request;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.weld.context.bound.BoundRequestContext;

import org.apache.isis.core.plugins.ioc.RequestContextHandle;
import org.apache.isis.core.plugins.ioc.RequestContextService;

@Singleton @Alternative @Priority(10)
public class RequestContextServiceWeld implements RequestContextService {

	/* Inject the BoundRequestContext. */
	/* Alternatively, you could look this up from the BeanManager */
	@Inject BoundRequestContext requestContext;

	/* Start the request, providing a data store which will last the lifetime of the request */
	@Override
	public RequestContextHandle startRequest() {
	    
	    if(isActive()) {
	        return null; // if already active, don't return a handle
	    }
		
		final Map<String, Object> requestDataStore = new ConcurrentHashMap<>();

		// Associate the store with the context and activate the context
		requestContext.associate(requestDataStore);
		requestContext.activate();
		
		return RequestContextHandleWeld.of(()->endRequest(requestDataStore));
	}

	private boolean isActive() {
        return requestContext.isActive();
    }
	
	/* End the request, providing the same data store as was used to start the request */
	private void endRequest(Map<String, Object> requestDataStore) {

		try {
			/* Invalidate the request (all bean instances will be scheduled for destruction) */
			requestContext.invalidate();
			/* Deactivate the request, causing all bean instances to be destroyed (as the context is invalid) */
			requestContext.deactivate();

		} finally {
			/* Ensure that whatever happens we dissociate to prevent any memory leaks */
			requestContext.dissociate(requestDataStore);
		}

	}


    

}
