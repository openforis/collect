package org.openforis.collect.command;

import org.openforis.collect.command.handler.AddNodeCommandHandler;
import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.command.handler.CreateRecordHandler;
import org.openforis.collect.command.handler.CreateRecordPreviewHandler;
import org.openforis.collect.command.handler.DeleteNodeCommandHandler;
import org.openforis.collect.command.handler.DeleteRecordHandler;
import org.openforis.collect.command.handler.RecordCommandHandler;
import org.openforis.collect.command.handler.UpdateAttributeCommandHandler;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.web.manager.SessionRecordProvider;
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
	@Autowired
	private SessionRecordProvider sessionRecordProvider;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() {
		register(CreateRecordCommand.class, new CreateRecordHandler());
		register(CreateRecordPreviewCommand.class, new CreateRecordPreviewHandler());
		register(DeleteRecordCommand.class, new DeleteRecordHandler());

		AddNodeCommandHandler addNodeCommandHandler = new AddNodeCommandHandler();
		register(AddAttributeCommand.class, addNodeCommandHandler);
		register(AddEntityCommand.class, addNodeCommandHandler);

		UpdateAttributeCommandHandler updateAttributeCommandHandler = new UpdateAttributeCommandHandler<UpdateAttributeCommand<?>>();
		register(UpdateBooleanAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateCodeAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateCoordinateAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateDateAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateFileAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateIntegerAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateRealAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateTaxonAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateTextAttributeCommand.class, updateAttributeCommandHandler);
		register(UpdateTimeAttributeCommand.class, updateAttributeCommandHandler);

		DeleteNodeCommandHandler deleteNodeCommandHandler = new DeleteNodeCommandHandler();
		register(DeleteAttributeCommand.class, deleteNodeCommandHandler);
		register(DeleteEntityCommand.class, deleteNodeCommandHandler);
	}

	private <H extends CommandHandler<?>> H setDependencies(H handler) {
		if (handler instanceof RecordCommandHandler) {
			RecordCommandHandler<?> recordCommandHandler = (RecordCommandHandler<?>) handler;
			recordCommandHandler.setJobManager(jobManager);
			recordCommandHandler.setSurveyManager(surveyManager);
			recordCommandHandler.setRecordManager(recordManager);
			recordCommandHandler.setRecordProvider(sessionRecordProvider);
			recordCommandHandler.setUserManager(userManager);
			recordCommandHandler.setMessageSource(messageSource);
		}
		return handler;
	}

	@Override
	public <R, C extends Command> RegistryCommandDispatcher register(Class<C> commandType, CommandHandler<C> handler) {
		setDependencies(handler);
		return super.register(commandType, new SpringTransactionalCommandHandler<C>(transactionManager, handler));
	}

}
