package org.openforis.collect.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openforis.collect.command.AddEntityCommand;
import org.openforis.collect.command.Command;
import org.openforis.collect.command.CommandDispatcher;
import org.openforis.collect.event.EntityCreatedEvent;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.ws.AppWS;
import org.openforis.collect.web.ws.AppWS.RecordEventMessage;
import org.openforis.collect.web.ws.AppWS.RecordUpdateErrorMessage;
import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.openforis.collect.utils.ExceptionHandler;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.test.util.ReflectionTestUtils;

public class CommandControllerTest {

	private static final String RECORD_VIEWERS = "users who can view the record";

	private final List<String> recipients = new ArrayList<String>();
	private final List<Message> messages = new ArrayList<Message>();

	@Test
	public void testRecordEventsSentToRecordViewersAndErrorsToCommandUser() {
		CommandController controller = createController(false);

		controller.addEntity(createCommand());

		assertEquals(2, messages.size());
		assertEquals(RECORD_VIEWERS, recipients.get(0));
		assertTrue(messages.get(0) instanceof RecordEventMessage);
		assertEquals("entry1", recipients.get(1));
		assertTrue(messages.get(1) instanceof RecordUpdateErrorMessage);
	}

	@Test
	public void testPreviewRecordEventsSentOnlyToCommandUser() {
		CommandController controller = createController(true);

		controller.addEntity(createCommand());

		assertEquals("entry1", recipients.get(0));
		assertTrue(messages.get(0) instanceof RecordEventMessage);
	}

	private CommandController createController(boolean preview) {
		CollectSurvey survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		EntityDefinition rootEntityDefn = survey.getSchema().createEntityDefinition();
		rootEntityDefn.setName("plot");
		survey.getSchema().addRootEntityDefinition(rootEntityDefn);
		CollectRecord collectRecord = new CollectRecord(survey, null, rootEntityDefn, false);
		collectRecord.setPreview(preview);

		CommandController controller = new CommandController();
		ReflectionTestUtils.setField(controller, "sessionManager", new SessionManager() {
			@Override
			public String getLoggedUsername() {
				return "entry1";
			}
		});
		ReflectionTestUtils.setField(controller, "surveyManager", new SurveyManager() {
			@Override
			public CollectSurvey getOrLoadSurveyById(int id) {
				return survey;
			}
		});
		ReflectionTestUtils.setField(controller, "sessionRecordProvider", new SessionRecordProvider(null) {
			@Override
			public CollectRecord provide(CollectSurvey survey, Integer recordId, Step recordStep) {
				return collectRecord;
			}
		});
		ReflectionTestUtils.setField(controller, "commandDispatcher", new CommandDispatcher() {
			@Override
			public <C extends Command> void submit(C command, EventListener eventListener,
					ExceptionHandler exceptionHandler) {
				eventListener.onEvent(new EntityCreatedEvent());
				exceptionHandler.onException(new IllegalStateException("command failed"));
			}

			@Override
			public <C extends Command> List<RecordEvent> submitSync(C command) {
				throw new UnsupportedOperationException();
			}
		});
		ReflectionTestUtils.setField(controller, "appWS", new AppWS(null, null, null, null) {
			@Override
			public void sendMessageToUser(String username, Message message) {
				recipients.add(username);
				messages.add(message);
			}

			@Override
			public void sendRecordMessage(CollectRecord collectRecord, Message message) {
				recipients.add(RECORD_VIEWERS);
				messages.add(message);
			}

			@Override
			public void sendMessage(Message message) {
				fail("record events must not be broadcast");
			}
		});
		return controller;
	}

	private AddEntityCommand createCommand() {
		AddEntityCommand command = new AddEntityCommand();
		command.setSurveyId(1);
		command.setRecordId(1);
		command.setRecordStep(RecordStep.ENTRY);
		return command;
	}
}
