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

import java.util.function.Consumer;

import org.apache.isis.core.plugins.ioc.ConversationContextHandle;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName="of")
class ConversationContextHandleWeld implements ConversationContextHandle {

	private Consumer<String> onResume;
	private Runnable onClose;
	
	@Override
	public void resume(final String cid) {
		
		if(onResume!=null) {
			onResume.accept(cid);
		}
		
		// release consumer
		onResume = null;
	}
	
	@Override
	public void close() {
		
		if(onClose!=null) {
			onClose.run();
		}
		
		// release runnable
		onClose = null;
	}
	
	

}
