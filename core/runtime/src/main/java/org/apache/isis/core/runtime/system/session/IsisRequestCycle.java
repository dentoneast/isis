package org.apache.isis.core.runtime.system.session;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.val;

/**
 * @since 2.0.0-M3
 */
public class IsisRequestCycle implements AutoCloseable {

	private final IsisSessionFactory isisSessionFactory = IsisContext.getSessionFactory();
	private final IsisTransactionManager isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
	
	public static IsisRequestCycle open() {
		return new IsisRequestCycle();
	}
	
	private IsisRequestCycle() {
		
    	// no-op if no session available.
        if(!isisSessionFactory.isInSession()) {
            return;
        }

        // no-op if no transaction manager available.
        if(isisTransactionManager==null) {
            return;
        }
        
        isisTransactionManager.startTransaction();
	}
	
	@Override
	public void close() {
		
		final boolean inTransaction = isisSessionFactory.isInTransaction();
        if(inTransaction) {
            // user/logout will have invalidated the current transaction and also persistence session.
            try {
                isisTransactionManager.endTransaction();
            } catch (Exception ex) {
                // ignore.  Any exceptions will have been mapped into a suitable response already.
            }
        }
		
	}
	
	// -- SUPPORTING WEB REQUEST CYCLE FOR ISIS ...

	public static void onBeginRequest(AuthenticationSession authenticationSession) {
		
		val isisSessionFactory = IsisContext.getSessionFactory();
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
		
		// TODO Auto-generated method stub
		isisSessionFactory.openSession(authenticationSession);
		isisTransactionManager.startTransaction();
	}

	public static void onRequestHandlerExecuted() {
		
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
		if (isisTransactionManager==null) {
			return;
		}
		
        try {
            // will commit (or abort) the transaction;
            // an abort will cause the exception to be thrown.
        	isisTransactionManager.endTransaction();
        	
        } catch(Exception ex) {

        	// will redirect to error page after this,
            // so make sure there is a new transaction ready to go.
            if(isisTransactionManager.getCurrentTransaction().getState().isComplete()) {
            	isisTransactionManager.startTransaction();
            }
            
            throw ex;
        }
		
	}

	public static void onEndRequest() {
		
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
		if (isisTransactionManager==null) {
			return;
		}
		
        try {
        	isisTransactionManager.endTransaction();
        } finally {
        	val isisSessionFactory = IsisContext.getSessionFactory();
        	isisSessionFactory.closeSession();
        }
        
	}
	

}
