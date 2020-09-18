package org.openforis.collect.command;

import java.util.List;

import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventListenerToList;
import org.openforis.collect.event.RecordEvent;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransactionalCommandHandler<C extends Command> implements CommandHandler<C> {

	private PlatformTransactionManager transactionManager;
	private final CommandHandler<C> next;

	public SpringTransactionalCommandHandler(PlatformTransactionManager transactionManager, CommandHandler<C> next) {
		this.transactionManager = transactionManager;
		this.next = next;
	}

	@Override
	public void execute(final C command, final EventListener eventListener) {
		executeInTransaction(new Runnable() {
			public void run() {
				next.execute(command, eventListener);
			}
		});
	}

	@Override
	public List<RecordEvent> executeSync(C command) {
		EventListenerToList eventListener = new EventListenerToList();
		next.execute(command, eventListener);
		return eventListener.getList();
	}

	private void executeInTransaction(Runnable runnable) {
		TransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		try {
			runnable.run();
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw new RuntimeException(e);
		}
	}

}
