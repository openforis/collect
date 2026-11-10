package org.openforis.collect.web.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;

import java.util.function.BiPredicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.SurveyController.SurveyCloneParameters;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters;
import org.openforis.collect.web.controller.SurveyController.SurveyExportParameters;
import org.openforis.collect.web.controller.SurveyController.SurveyImportParameters;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SurveyControllerAccessTest {

	private static final int SURVEY_ID = 1;
	private static final String SURVEY_NAME = "test";
	private static final String SURVEY_URI = "http://www.openforis.org/collect/test";
	private static final int SURVEY_GROUP_ID = 10;
	private static final int OTHER_GROUP_ID = 20;

	private final AccessControlMock accessControlMock = new AccessControlMock();
	private final AccessControl accessControl = accessControlMock.accessControl;
	private final CollectSurvey survey = mock(CollectSurvey.class, RETURNS_DEEP_STUBS);
	private final User loggedUser = user(3, UserRole.DESIGN);

	@Mock(answer = RETURNS_DEEP_STUBS)
	private SurveyManager surveyManager;
	@Mock
	private SessionManager sessionManager;
	@InjectMocks
	private SurveyController controller;

	@Before
	public void init() {
		ReflectionTestUtils.setField(controller, "accessControl", accessControl);
		when(surveyManager.getOrLoadSurveyById(SURVEY_ID)).thenReturn(survey);
		when(surveyManager.getByUri(SURVEY_URI)).thenReturn(survey);
		SurveySummary summary = new SurveySummary(SURVEY_ID, SURVEY_NAME, SURVEY_URI);
		summary.setUserGroupId(SURVEY_GROUP_ID);
		when(surveyManager.loadSummaryByName(SURVEY_NAME)).thenReturn(summary);
		when(surveyManager.loadSummaryByUri(SURVEY_URI)).thenReturn(summary);
		when(sessionManager.getLoggedUser()).thenReturn(loggedUser);
	}

	@Test
	public void nonAdminCanListOnlyHisOwnSurveys() {
		accessControlMock.assertChecked(() -> controller.loadSurveys(99, OTHER_GROUP_ID, false, false, false, "en"));
		verify(accessControl).checkCanAccessUserGroup(OTHER_GROUP_ID);
		verify(surveyManager).getSurveySummaries(anyString(), eq(loggedUser.getId()), any());
	}

	@Test
	public void surveyLoadingRequiresSurveyAccess() {
		accessControlMock.assertChecked(() -> controller.loadSurvey(SURVEY_ID, false, "en"));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(() -> controller.loadSurveyFiles(SURVEY_ID));
		verify(accessControl).checkCanAccessSurvey(survey);
	}

	@Test
	public void surveyDesignOperationsRequireSurveyEditAccess() {
		accessControlMock.assertChecked(() -> controller.cloneIntoTemporarySurvey(SURVEY_ID));
		verify(accessControl).checkCanEditSurvey(survey);
		accessControlMock.assertChecked(() -> controller.publishSurvey(SURVEY_ID, false, "en"));
		verify(accessControl).checkCanEditSurvey(survey);
		accessControlMock.assertChecked(() -> controller.unpublishSurvey(SURVEY_ID, "en"));
		verify(accessControl).checkCanEditSurvey(survey);
		accessControlMock.assertChecked(() -> controller.closeSurvey(SURVEY_ID, "en"));
		verify(accessControl).checkCanEditSurvey(survey);
		accessControlMock.assertChecked(() -> controller.archiveSurvey(SURVEY_ID, "en"));
		verify(accessControl).checkCanEditSurvey(survey);
		accessControlMock.assertChecked(() -> controller.deleteSurvey(SURVEY_ID));
		verify(accessControl).checkCanEditSurvey(survey);
		SurveyExportParameters exportParameters = new SurveyExportParameters();
		exportParameters.setSurveyUri(SURVEY_URI);
		accessControlMock.assertChecked(() -> controller.startExport(exportParameters, null));
		verify(accessControl).checkCanEditSurvey(survey);
	}

	@Test
	public void surveyCreationRequiresDesignRoleAndUserGroupAccess() {
		SurveyCreationParameters params = new SurveyCreationParameters();
		params.setUserGroupId(OTHER_GROUP_ID);
		accessControlMock.assertChecked(
				() -> controller.createSurvey(params, new BeanPropertyBindingResult(params, "params")));
		verify(accessControl).checkRole(UserRole.DESIGN);
		verify(accessControl).checkCanAccessUserGroup(OTHER_GROUP_ID);

		SurveyCloneParameters cloneParams = new SurveyCloneParameters();
		cloneParams.setOriginalSurveyName(SURVEY_NAME);
		accessControlMock.assertChecked(() -> controller.cloneSurvey(cloneParams));
		verify(accessControl).checkRole(UserRole.DESIGN);
		verify(accessControl).checkCanAccessSurveyGroup(SURVEY_GROUP_ID);
	}

	@Test
	public void surveyImportIntoExistingSurveyRequiresAccessToIt() {
		MockMultipartFile surveyFile = new MockMultipartFile("file", "survey.collect", null, new byte[0]);
		accessControlMock.assertChecked(() -> controller.prepareSurveyImport(surveyFile));
		verify(accessControl).checkRole(UserRole.DESIGN);

		SurveyBackupInfo uploadedSurveyInfo = new SurveyBackupInfo();
		uploadedSurveyInfo.setSurveyUri(SURVEY_URI);
		ReflectionTestUtils.setField(controller, "uploadedSurveyInfo", uploadedSurveyInfo);
		SurveyImportParameters params = new SurveyImportParameters();
		params.setUserGroupId(OTHER_GROUP_ID);
		accessControlMock.assertChecked(
				() -> controller.startSurveyFileImport(params, new BeanPropertyBindingResult(params, "params")));
		verify(accessControl).checkRole(UserRole.DESIGN);
		verify(accessControl).checkCanAccessUserGroup(OTHER_GROUP_ID);
		verify(accessControl).checkCanAccessSurveyGroup(SURVEY_GROUP_ID);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void surveyUserGroupCanBeChangedOnlyBySurveyOwnersOrAdministrators() {
		accessControlMock.assertChecked(() -> controller.changeSurveyUserGroup(SURVEY_NAME, OTHER_GROUP_ID));
		verify(accessControl).checkRole(UserRole.DESIGN);
		verify(accessControl).checkCanAccessUserGroup(OTHER_GROUP_ID);
		ArgumentCaptor<BiPredicate<User, UserGroupRole>> permission = ArgumentCaptor.forClass(BiPredicate.class);
		verify(accessControl).checkSurveyGroupPermission(eq(SURVEY_GROUP_ID), permission.capture());
		assertTrue(permission.getValue().test(loggedUser, UserGroupRole.OWNER));
		assertTrue(permission.getValue().test(loggedUser, UserGroupRole.ADMINISTRATOR));
		assertFalse(permission.getValue().test(loggedUser, UserGroupRole.SUPERVISOR));
	}
}
