package org.openforis.collect.command;

import org.openforis.collect.command.handler.AddNodeCommandHandler;
import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.command.handler.CreateRecordHandler;
import org.openforis.collect.command.handler.DeleteNodeCommandHandler;
import org.openforis.collect.command.handler.DeleteRecordHandler;
import org.openforis.collect.command.handler.UpdateAttributeCommandHandler;
import org.openforis.collect.manager.CachedRecordProvider;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
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
	@Autowired
	private MessageSource messageSource;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() {
		transactional(CreateRecordCommand.class, new CreateRecordHandler(recordManager, surveyManager, userManager, messageSource));
		transactional(DeleteRecordCommand.class, new DeleteRecordHandler(recordManager, surveyManager));

		RecordProvider recordProvider = new CachedRecordProvider(recordManager);

		AddNodeCommandHandler addNodeCommandHandler = new AddNodeCommandHandler(surveyManager, recordProvider,
				recordManager, messageSource);
		transactional(AddAttributeCommand.class, addNodeCommandHandler);
		transactional(AddEntityCommand.class, addNodeCommandHandler);

		UpdateAttributeCommandHandler updateAttributeCommandHandler = new UpdateAttributeCommandHandler<UpdateBooleanAttributeCommand>(
				surveyManager, recordProvider, recordManager, messageSource);
		transactional(UpdateBooleanAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateCodeAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateDateAttributeCommand.class, updateAttributeCommandHandler);
		transactional(UpdateTextAttributeCommand.class, updateAttributeCommandHandler);

		DeleteNodeCommandHandler deleteNodeCommandHandler = new DeleteNodeCommandHandler(surveyManager, recordProvider,
				recordManager, messageSource);
		transactional(DeleteAttributeCommand.class, deleteNodeCommandHandler);
		transactional(DeleteEntityCommand.class, deleteNodeCommandHandler);
	}

	<C extends Command> void transactional(Class<C> commandType, CommandHandler<C> handler) {
		register(commandType, new SpringTransactionalCommandHandler<C>(transactionManager, handler));
	}
}
