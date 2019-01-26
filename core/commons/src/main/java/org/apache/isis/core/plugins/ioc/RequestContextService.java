package org.apache.isis.core.plugins.ioc;

public interface RequestContextService {

	RequestContextHandle startRequest();
	boolean isActive();
	
    static void closeHandle(RequestContextHandle requestContextHandle) {
        if(requestContextHandle!=null) {
            requestContextHandle.close();
        }
    }

}
