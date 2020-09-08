package org.openforis.collect.command;

import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.event.EventListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransactionalCommandHandler<C extends Command> implements CommandHandler<C> {

	private PlatformTransactionManager transactionManager;
	private final CommandHandler<C> next;

	public SpringTransactionalCommandHandler(PlatformTransactionManager transactionManager,
			CommandHandler<C> next) {
		this.transactionManager = transactionManager;
		this.next = next;
	}

	@Override
	public void execute(C command, EventListener eventListener) {
		TransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		try {
			next.execute(command, eventListener);
			transactionManager.commit(status);
		} catch(Exception e) {
			transactionManager.rollback(status);
			throw new RuntimeException(e);
		}
	}

}
