package org.openforis.collect.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiPredicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.manager.RecordGenerator.NewRecordParameters;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.RecordController.BackupDataExportParameters;
import org.openforis.collect.web.controller.RecordController.CSVExportParametersForm;
import org.openforis.collect.web.controller.RecordController.RecordDeleteParameters;
import org.openforis.collect.web.controller.RecordController.RecordSummarySearchParameters;
import org.openforis.commons.web.Response;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RecordControllerAccessTest {

	private static final int SURVEY_ID = 1;
	private static final int RECORD_ID = 2;

	private final AccessControlMock accessControlMock = new AccessControlMock();
	private final AccessControl accessControl = accessControlMock.accessControl;
	private final CollectSurvey survey = mock(CollectSurvey.class, RETURNS_DEEP_STUBS);
	private final User loggedUser = user(3, UserRole.ENTRY_LIMITED);

	@Mock(answer = RETURNS_DEEP_STUBS)
	private SurveyManager surveyManager;
	@Mock
	private RecordManager recordManager;
	@Mock(answer = RETURNS_DEEP_STUBS)
	private RecordSessionManager sessionManager;
	@Mock
	private UserManager userManager;
	@Mock(answer = RETURNS_DEEP_STUBS)
	private UserGroupManager userGroupManager;
	@Mock
	private CollectJobManager jobManager;
	@InjectMocks
	private RecordController controller;

	@Before
	public void init() {
		ReflectionTestUtils.setField(controller, "accessControl", accessControl);
		when(surveyManager.getById(SURVEY_ID)).thenReturn(survey);
		when(surveyManager.getOrLoadSurveyById(SURVEY_ID)).thenReturn(survey);
		when(surveyManager.loadSurvey(SURVEY_ID)).thenReturn(survey);
		when(sessionManager.getLoggedUser()).thenReturn(loggedUser);
	}

	@Test
	public void recordEndpointsCheckRecordAccess() {
		accessControlMock.assertChecked(() -> controller.loadData(SURVEY_ID, RECORD_ID, 1));
		verify(accessControl).checkCanViewRecord(survey, RECORD_ID);
		accessControlMock.assertChecked(() -> controller.loadRecord(SURVEY_ID, RECORD_ID, null, false));
		verify(accessControl).checkCanViewRecord(survey, RECORD_ID);
		accessControlMock.assertChecked(
				() -> controller.exportRecordToCsv(SURVEY_ID, RECORD_ID, 1, new MockHttpServletResponse()));
		verify(accessControl).checkCanViewRecord(survey, RECORD_ID);
		accessControlMock.assertChecked(
				() -> controller.exportRecordToCollectFormat(SURVEY_ID, RECORD_ID, new MockHttpServletResponse()));
		verify(accessControl).checkCanViewRecord(survey, RECORD_ID);
		accessControlMock.assertChecked(() -> controller.promoteRecord(SURVEY_ID, RECORD_ID));
		verify(accessControl).checkCanEditRecord(survey, RECORD_ID);
		accessControlMock.assertChecked(
				() -> controller.updateOwner(SURVEY_ID, RECORD_ID, Collections.singletonMap("ownerId", "4")));
		verify(accessControl).checkCanViewRecord(survey, RECORD_ID);
		assertCanChangeRecordOwnerRequired(accessControlMock.verifiedSurveyPermission());
	}

	@Test
	public void demoteRequiresPermissionToDemoteRecordInItsStep() {
		CollectRecordSummary summary = new CollectRecordSummary();
		summary.setStep(Step.CLEANSING);
		when(accessControl.checkCanViewRecord(survey, RECORD_ID)).thenReturn(summary);
		accessControlMock.assertChecked(() -> controller.demoteRecord(SURVEY_ID, RECORD_ID));
		BiPredicate<User, UserGroupRole> permission = accessControlMock.verifiedSurveyPermission();
		assertTrue(permission.test(user(4, UserRole.CLEANSING), UserGroupRole.OPERATOR));
		assertFalse(permission.test(user(4, UserRole.ENTRY), UserGroupRole.OPERATOR));
	}

	@Test
	public void surveyDataEndpointsCheckSurveyAccess() {
		accessControlMock.assertChecked(() -> controller.getCount(SURVEY_ID, null, null));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(
				() -> controller.startCsvDataExportJob(SURVEY_ID, new CSVExportParametersForm()));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(
				() -> controller.startBackupDataExportJob(SURVEY_ID, new BackupDataExportParameters()));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(() -> controller.generateStats(SURVEY_ID));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(() -> controller.startValidationResportJob(SURVEY_ID));
		verify(accessControl).checkCanAccessSurvey(survey);
	}

	@Test
	public void recordSummariesAreFilteredByLoggedUser() {
		RecordSummarySearchParameters params = new RecordSummarySearchParameters();
		params.setUserId(99);
		params.setUsername("admin");
		accessControlMock.assertChecked(() -> controller.loadRecordSummaries(SURVEY_ID, params));
		verify(accessControl).checkCanAccessSurvey(survey);
		ArgumentCaptor<RecordFilter> filter = ArgumentCaptor.forClass(RecordFilter.class);
		verify(recordManager).loadSummaries(filter.capture(), any());
		// entry limited user: only his own records
		assertEquals(Arrays.asList(loggedUser.getId()), filter.getValue().getOwnerIds());
		verifyNoInteractions(userManager);
	}

	@Test
	public void recordCreationRequiresPermissionToCreateRecords() {
		NewRecordParameters params = new NewRecordParameters();
		accessControlMock.assertChecked(() -> controller.newRecord(SURVEY_ID, params));
		assertCanCreateRecordsRequired(accessControlMock.verifiedSurveyPermission());
		accessControlMock.assertChecked(() -> controller.startRandomRecordsGenerationJob(SURVEY_ID, "old", "new",
				10d, "grid.csv", true));
		assertCanCreateRecordsRequired(accessControlMock.verifiedSurveyPermission());

		params.setUserId(99);
		params.setUsername("admin");
		accessControlMock.assertChecked(() -> controller.createRandomRecord(SURVEY_ID, params));
		assertCanCreateRecordsRequired(accessControlMock.verifiedSurveyPermission());
		// random records are owned by the logged user
		assertEquals(loggedUser.getId(), params.getUserId());
		assertNull(params.getUsername());
	}

	@Test
	public void previewRecordCreationRequiresSurveyAccessOnly() {
		NewRecordParameters params = new NewRecordParameters();
		params.setPreview(true);
		accessControlMock.assertChecked(() -> controller.newRecord(SURVEY_ID, params));
		verify(accessControl).checkCanAccessSurvey(survey);
		verify(accessControl, never()).checkSurveyPermission(any(), any());
	}

	@Test
	public void recordImportAndBulkMoveRequireSupervisingGroupRole() {
		MockMultipartFile file = new MockMultipartFile("file", "data.collect-data", null, new byte[0]);
		accessControlMock.assertChecked(() -> controller.startRecordImportSummaryJob(SURVEY_ID, file, "plot"));
		assertImportRecordsRequired(accessControlMock.verifiedSurveyPermission());
		accessControlMock.assertChecked(() -> controller.startCsvDataImportJob(SURVEY_ID, file, "plot", "update",
				"ENTRY", null, false, false, null));
		assertImportRecordsRequired(accessControlMock.verifiedSurveyPermission());
		accessControlMock.assertChecked(() -> controller.moveRecords(SURVEY_ID, "ENTRY", true));
		assertCanChangeRecordOwnerRequired(accessControlMock.verifiedSurveyPermission());
	}

	@Test
	public void recordsNotInSurveyAreNotDeleted() throws Exception {
		RecordDeleteParameters params = new RecordDeleteParameters();
		params.setRecordIds(new Integer[] { RECORD_ID, 5 });
		when(recordManager.loadSummaries(any(RecordFilter.class)))
				.thenReturn(Arrays.asList(new CollectRecordSummary()));
		Response response = controller.deleteRecord(SURVEY_ID, params);
		assertTrue(response.isStatusError());
		verify(recordManager, never()).delete(anyInt());
	}

	private static void assertCanCreateRecordsRequired(BiPredicate<User, UserGroupRole> permission) {
		assertTrue(permission.test(user(4, UserRole.ENTRY), UserGroupRole.OPERATOR));
		assertFalse(permission.test(user(4, UserRole.ENTRY_LIMITED), UserGroupRole.OPERATOR));
	}

	private static void assertImportRecordsRequired(BiPredicate<User, UserGroupRole> permission) {
		assertTrue(permission.test(user(4, UserRole.ENTRY), UserGroupRole.SUPERVISOR));
		assertFalse(permission.test(user(4, UserRole.ENTRY), UserGroupRole.OPERATOR));
	}

	private static void assertCanChangeRecordOwnerRequired(BiPredicate<User, UserGroupRole> permission) {
		assertTrue(permission.test(user(4, UserRole.CLEANSING), UserGroupRole.SUPERVISOR));
		assertFalse(permission.test(user(4, UserRole.ENTRY), UserGroupRole.SUPERVISOR));
	}
}
