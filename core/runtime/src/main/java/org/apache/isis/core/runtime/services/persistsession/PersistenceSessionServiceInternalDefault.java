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
package org.apache.isis.core.runtime.services.persistsession;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.isis.commons.internal.base._With.acceptIfPresent;
import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

@Singleton
public class PersistenceSessionServiceInternalDefault implements PersistenceSessionServiceInternal {

    @Override
    public ObjectAdapterProvider getObjectAdapterProvider() {
        return getPersistenceSession();
    }

    
    @Override
    public void executeWithinTransaction(Runnable task) {
        getTransactionManager().executeWithinTransaction(task);
    }
    
    @Override
    public <T> T executeWithinTransaction(Supplier<T> task) {
        return getTransactionManager().executeWithinTransaction(task);
    }

    protected PersistenceSession getPersistenceSession() {
        return ofNullable(getIsisSessionFactory().getCurrentSession())
                .map(IsisSession::getPersistenceSession)
                .orElseThrow(()->new NonRecoverableException("No IsisSession on current thread."));
    }

    private IsisSessionFactory getIsisSessionFactory() {
        return requireNonNull(isisSessionFactory, "IsisSessionFactory was not injected.");
    }

    public IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    @Inject SpecificationLoader specificationLoader;
    @Inject IsisSessionFactory isisSessionFactory;

}
