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

package org.apache.isis.core.runtime.services.xactn;

import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

@Singleton
public class TransactionServiceDefault implements TransactionService {

    @PostConstruct
	public void init() {
    	isisTransactionManager = IsisContext.getTransactionManager().get();
    	
    	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+isisTransactionManager);
    	
	}
	
    @Override
    public void flushTransaction() {
    	isisTransactionManager.flushTransaction();
    }

    @Override
    public void nextTransaction() {
        nextTransaction((Command)null);
    }

    @Override
    public void nextTransaction(final Command commandIfAny) {
        nextTransaction(TransactionService.Policy.UNLESS_MARKED_FOR_ABORT, commandIfAny);
    }

    @Override
    public void nextTransaction(TransactionService.Policy policy) {
        nextTransaction(policy, null);
    }

    @Override
    public void nextTransaction(TransactionService.Policy policy, final Command commandIfAny) {
        final TransactionState transactionState = getTransactionState();
        switch (transactionState) {
        case NONE:
            break;
        case IN_PROGRESS:
        	isisTransactionManager.endTransaction();
            break;
        case MUST_ABORT:
            switch (policy) {
            case UNLESS_MARKED_FOR_ABORT:
                throw new IsisException("Transaction is marked to abort");
            case ALWAYS:
            	isisTransactionManager.abortTransaction();
                final Transaction currentTransaction = currentTransaction();
                if(currentTransaction instanceof IsisTransaction) {
                    ((IsisTransaction)currentTransaction).getThenClearAbortCause();
                }
                break;
            }
            break;
        case COMMITTED:
            break;
        case ABORTED:
            break;
        }

        isisTransactionManager.startTransaction(commandIfAny);
    }

    @Override
    public Transaction currentTransaction() {
        return isisTransactionManager.getCurrentTransaction();
    }

    @Override
    public CountDownLatch currentTransactionLatch() {
    	IsisTransaction transaction = isisTransactionManager.getCurrentTransaction();
        return transaction==null ? new CountDownLatch(0) : transaction.countDownLatch();
    }

    @Override
    public TransactionState getTransactionState() {
        final IsisTransaction transaction = isisTransactionManager.getCurrentTransaction();
        if (transaction == null) {
            return TransactionState.NONE;
        }
        IsisTransaction.State state = transaction.getState();
        return state.getTransactionState();
    }

    IsisTransactionManager isisTransactionManager;


}
