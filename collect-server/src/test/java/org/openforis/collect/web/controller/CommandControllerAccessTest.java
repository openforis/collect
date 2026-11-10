package org.openforis.collect.web.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;

import java.util.function.BiPredicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openforis.collect.command.AddEntityCommand;
import org.openforis.collect.command.CreateRecordCommand;
import org.openforis.collect.command.CreateRecordPreviewCommand;
import org.openforis.collect.command.DeleteAttributeCommand;
import org.openforis.collect.command.DeleteRecordCommand;
import org.openforis.collect.command.RecordCommand;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CommandControllerAccessTest {

	private static final int SURVEY_ID = 1;
	private static final int RECORD_ID = 2;

	private final AccessControlMock accessControlMock = new AccessControlMock();
	private final AccessControl accessControl = accessControlMock.accessControl;
	private final CollectSurvey survey = mock(CollectSurvey.class, RETURNS_DEEP_STUBS);

	@Mock
	private SurveyManager surveyManager;
	@Mock
	private SessionManager sessionManager;
	@InjectMocks
	private CommandController controller;

	@Before
	public void init() {
		ReflectionTestUtils.setField(controller, "accessControl", accessControl);
		when(surveyManager.getOrLoadSurveyById(SURVEY_ID)).thenReturn(survey);
	}

	@Test
	public void previewRecordCommandsRequireSurveyAccessOnly() {
		accessControlMock.assertChecked(
				() -> controller.createRecordPreview(command(new CreateRecordPreviewCommand(), null)));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(() -> controller.addEntity(command(new AddEntityCommand(), null)));
		verify(accessControl).checkCanAccessSurvey(survey);
	}

	@Test
	public void recordCreationRequiresPermissionToCreateRecords() {
		accessControlMock.assertChecked(() -> controller.createRecord(command(new CreateRecordCommand(), null)));
		BiPredicate<User, UserGroupRole> permission = accessControlMock.verifiedSurveyPermission();
		assertTrue(permission.test(user(3, UserRole.ENTRY), UserGroupRole.OPERATOR));
		assertFalse(permission.test(user(3, UserRole.ENTRY), UserGroupRole.VIEWER));
	}

	@Test
	public void recordDeletionRequiresPermissionToDeleteTheRecord() {
		User owner = user(3, UserRole.ENTRY);
		CollectRecordSummary summary = new CollectRecordSummary();
		summary.setStep(Step.ENTRY);
		summary.setOwner(owner);
		when(accessControl.checkCanViewRecord(survey, RECORD_ID)).thenReturn(summary);
		accessControlMock.assertChecked(() -> controller.deleteRecord(command(new DeleteRecordCommand(), RECORD_ID)));
		BiPredicate<User, UserGroupRole> permission = accessControlMock.verifiedSurveyPermission();
		assertTrue(permission.test(owner, UserGroupRole.OPERATOR));
		assertFalse(permission.test(user(4, UserRole.ENTRY), UserGroupRole.OPERATOR));
	}

	@Test
	public void recordUpdatesRequireRecordEditAccess() {
		accessControlMock.assertChecked(() -> controller.addEntity(command(new AddEntityCommand(), RECORD_ID)));
		verify(accessControl).checkCanEditRecord(survey, RECORD_ID);
		accessControlMock.assertChecked(
				() -> controller.deleteAttributeFile(command(new DeleteAttributeCommand(), RECORD_ID)));
		verify(accessControl).checkCanEditRecord(survey, RECORD_ID);
		String updateFileCommand = String.format("{\"surveyId\": %d, \"recordId\": %d, \"attributeType\": \"FILE\"}",
				SURVEY_ID, RECORD_ID);
		accessControlMock.assertChecked(() -> controller.updateAttributeFile(updateFileCommand,
				new MockMultipartFile("file", new byte[0])));
		verify(accessControl).checkCanEditRecord(survey, RECORD_ID);
	}

	private static <C extends RecordCommand> C command(C command, Integer recordId) {
		command.setSurveyId(SURVEY_ID);
		command.setRecordId(recordId);
		return command;
	}
}
