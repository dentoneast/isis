package org.apache.isis.core.runtime.system.transaction;

import java.util.function.Supplier;

import org.apache.isis.applib.services.command.Command;

public interface IsisTransactionManager {

	void open();

	void close();

	IsisTransaction getCurrentTransaction();

	int getTransactionLevel();

	void executeWithinTransaction(Runnable task);

	void abortTransaction();

	void startTransaction();

	void startTransaction(Command existingCommandIfAny);

	void flushTransaction();

	<Q> Q executeWithinTransaction(Supplier<Q> task);

	void endTransaction();

}
