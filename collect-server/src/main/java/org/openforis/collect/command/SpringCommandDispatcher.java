package org.openforis.collect.command;

import org.openforis.collect.command.handler.AddNodeCommandHandler;
import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.command.handler.CreateRecordHandler;
import org.openforis.collect.command.handler.DeleteNodeCommandHandler;
import org.openforis.collect.command.handler.DeleteRecordHandler;
import org.openforis.collect.command.handler.RecordCommandHandler;
import org.openforis.collect.command.handler.UpdateAttributeCommandHandler;
import org.openforis.collect.manager.CachedRecordProvider;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.concurrency.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public class SpringCommandDispatcher extends RegistryCommandDispatcher {

	@Autowired
	private JobManager jobManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PlatformTransactionManager transactionManager;
	@Autowired
	private MessageSource messageSource;
	
	private CachedRecordProvider recordProvider;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() {
		recordProvider = new CachedRecordProvider(recordManager);

		transactional(CreateRecordCommand.class, setDependencies(new CreateRecordHandler()));
		transactional(DeleteRecordCommand.class, setDependencies(new DeleteRecordHandler()));

		AddNodeCommandHandler addNodeCommandHandler = setDependencies(new AddNodeCommandHandler());
		transactional(AddAttributeCommand.class, addNodeCommandHandler);
		transactional(AddEntityCommand.class, addNodeCommandHandler);

		UpdateAttributeCommandHandler updateAttributeCommandHandler = setDependencies(new UpdateAttributeCommandHandler<UpdateAttributeCommand>());
		transactional(UpdateBooleanAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateCodeAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateDateAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateTextAttributeCommand.class, updateAttributeCommandHandler);

		DeleteNodeCommandHandler deleteNodeCommandHandler = setDependencies(new DeleteNodeCommandHandler());
		transactional(DeleteAttributeCommand.class, deleteNodeCommandHandler);
		transactional(DeleteEntityCommand.class, deleteNodeCommandHandler);
	}
	
	private <H extends RecordCommandHandler<?>> H setDependencies(H handler) {
		handler.setJobManager(jobManager);
		handler.setSurveyManager(surveyManager);
		handler.setRecordManager(recordManager);
		handler.setRecordProvider(recordProvider);
		handler.setUserManager(userManager);
		handler.setMessageSource(messageSource);
		return handler;
	}

	<C extends Command> void transactional(Class<C> commandType, CommandHandler<C> handler) {
		register(commandType, new SpringTransactionalCommandHandler<C>(transactionManager, handler));
	}
}
