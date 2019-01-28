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

import javax.servlet.http.HttpServletRequest;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.cdi._CDI;
import org.jboss.seam.conversation.spi.SeamConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;

import lombok.val;

public class SeamConversationContextSupport implements SeamConversationContext<HttpServletRequest> {

	@Override
	public SeamConversationContext<HttpServletRequest> associate(HttpServletRequest request) {
		httpConversationContext().associate(request);
		return this;
	}

	@Override
	public SeamConversationContext<HttpServletRequest> activate(String conversationId) {
		
		val cid = conversationId!=null && !_Strings.isEmpty(conversationId)
				? conversationId
						: null;
		
		httpConversationContext().activate(cid);
		return this;
	}

	@Override
	public SeamConversationContext<HttpServletRequest> invalidate() {
		httpConversationContext().invalidate();
		return this;
	}

	@Override
	public SeamConversationContext<HttpServletRequest> deactivate() {
		httpConversationContext().deactivate();
		return this;
	}

	@Override
	public SeamConversationContext<HttpServletRequest> dissociate(HttpServletRequest request) {
		httpConversationContext().dissociate(request);
		return this;
	}
	
	// -- STUPID HACK
	
	protected void doAssociate(HttpServletRequest nop) {
		
	}
	
	// -- HELPER
	
	private HttpConversationContext httpConversationContext() {
		return _CDI.getManagedBean(HttpConversationContext.class).get();
	}
	
	

	
}
