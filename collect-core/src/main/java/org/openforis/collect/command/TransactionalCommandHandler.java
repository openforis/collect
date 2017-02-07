package org.openforis.collect.command;

import org.openforis.collect.command.handler.CommandHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionalCommandHandler<R, C extends Command<R>> implements CommandHandler<R, C> {

	private PlatformTransactionManager transactionManager;
	private final CommandHandler<R, C> next;

	public TransactionalCommandHandler(PlatformTransactionManager transactionManager,
			CommandHandler<R, C> next) {
		this.transactionManager = transactionManager;
		this.next = next;
	}

	@Override
	public R execute(C command) {
		TransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		try {
			R result = next.execute(command);
			transactionManager.commit(status);
			return result;
		} catch(Exception e) {
			transactionManager.rollback(status);
			throw new RuntimeException(e);
		}
	}

}
