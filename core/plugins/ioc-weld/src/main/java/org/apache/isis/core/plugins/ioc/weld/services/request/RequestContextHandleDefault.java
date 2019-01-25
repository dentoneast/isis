package org.apache.isis.core.plugins.ioc.weld.services.request;

import org.apache.isis.core.plugins.ioc.RequestContextHandle;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
class RequestContextHandleDefault implements RequestContextHandle {

	private final Runnable onClose;
	
	@Override
	public void close() {
		onClose.run();
	}
	

}
