package org.openforis.collect.command;

import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.command.handler.CreateRecordHandler;
import org.openforis.collect.command.handler.UpdateAttributeCommandHandler;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public class SpringCommandDispatcher extends RegistryCommandDispatcher {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() {
		transactional(CreateRecordCommand.class, new CreateRecordHandler(recordManager, surveyManager, userManager));
		
		UpdateAttributeCommandHandler updateAttributeCommandHandler = new UpdateAttributeCommandHandler<UpdateBooleanAttributeCommand>(recordManager, surveyManager);
		
		transactional(UpdateBooleanAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateCodeAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateDateAttributeCommand.class, updateAttributeCommandHandler);
	}

	<R, C extends Command<R>> void transactional(Class<C> commandType, CommandHandler<R, C> handler) {
		register(commandType, new TransactionalCommandHandler<R, C>(transactionManager, handler));
	}
}
