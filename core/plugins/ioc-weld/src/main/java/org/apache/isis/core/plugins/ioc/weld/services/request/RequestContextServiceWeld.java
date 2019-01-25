package org.apache.isis.core.plugins.ioc.weld.services.request;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.core.plugins.ioc.RequestContextHandle;
import org.apache.isis.core.plugins.ioc.RequestContextService;
import org.jboss.weld.context.bound.BoundRequestContext;

@Singleton
public class RequestContextServiceWeld implements RequestContextService {

	/* Inject the BoundRequestContext. */
	/* Alternatively, you could look this up from the BeanManager */
	@Inject BoundRequestContext requestContext;

	/* Start the request, providing a data store which will last the lifetime of the request */
	@Override
	public RequestContextHandle startRequest() {
		
		final Map<String, Object> requestDataStore = new HashMap<>();

		// Associate the store with the context and activate the context
		requestContext.associate(requestDataStore);
		requestContext.activate();
		
		return RequestContextHandleDefault.of(()->endRequest(requestDataStore));
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
