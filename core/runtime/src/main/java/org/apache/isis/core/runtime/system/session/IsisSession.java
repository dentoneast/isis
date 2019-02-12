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

package org.apache.isis.core.runtime.system.session;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.isis.commons.internal.base._Tuples;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.managed.ManagedObjectContextBase;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;

/**
 * Analogous to (and in essence a wrapper for) a JDO <code>PersistenceManager</code>;
 * holds the current set of components for a specific execution context (such as on a thread).
 *
 * <p>
 * The <code>IsisContext</code> class is responsible for locating the current execution context.
 *
 * @see IsisSessionFactory
 */
public class IsisSession extends ManagedObjectContextBase {

    @Deprecated //TODO [2033] avoid extensions to ManagedObjectContext 
    @Getter private final PersistenceSession persistenceSession;
    
	public IsisSession(
			final AuthenticationSession authenticationSession,
			final PersistenceSessionFactory persistenceSessionFactory) {
		
		super(IsisContext.getConfiguration(),
				IsisContext.getServiceInjector(),
				IsisContext.getServiceRegistry(),
				IsisContext.getSpecificationLoader(),
				authenticationSession);
		
		this.persistenceSession =
                persistenceSessionFactory.createPersistenceSession(authenticationSession);
        
        
	}
	
	// -- CURRENT
	
	public static IsisSession currentIfAny() {
		return _Context.threadLocalGetIfAny(IsisSession.class);
	}
	
	public static Optional<IsisSession> current() {
		return Optional.ofNullable(currentIfAny());
	}
	
	// -- OPEN
    
    
    void open() {
    	_Context.threadLocalPut(IsisSession.class, this);
        persistenceSession.open();
    }

    // -- CLOSE
    
    /**
     * Closes session.
     */
    void close() {
        if(persistenceSession != null) {
            persistenceSession.close();
        }
        _Context.threadLocalCleanup();
    }

    // -- transaction

    /**
     * Convenience method that returns the {@link IsisTransaction} of the
     * session, if any.
     */
    public IsisTransaction getCurrentTransaction() {
        return getTransactionManager().getCurrentTransaction();
    }



    // -- toString
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("authenticationSession", getAuthenticationSession());
        asString.append("persistenceSession", getPersistenceSession());
        asString.append("transaction", getCurrentTransaction());
        return asString.toString();
    }


    // -- Dependencies (from constructor)

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

	


}
