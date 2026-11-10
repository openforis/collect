package org.openforis.collect.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.function.BiPredicate;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.SpeciesController.TaxonQuery;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.session.SessionState;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

/**
 * Access checks of the survey and data controllers having only a few endpoints
 */
public class ControllersAccessTest {

	private static final int SURVEY_ID = 1;
	private static final String SURVEY_NAME = "test";
	private static final int RECORD_ID = 2;

	private final AccessControlMock accessControlMock = new AccessControlMock();
	private final AccessControl accessControl = accessControlMock.accessControl;
	private final CollectSurvey survey = mock(CollectSurvey.class, RETURNS_DEEP_STUBS);
	private final SurveyManager surveyManager = mock(SurveyManager.class);

	@Before
	public void init() {
		when(surveyManager.getById(SURVEY_ID)).thenReturn(survey);
		when(surveyManager.getOrLoadSurveyById(SURVEY_ID)).thenReturn(survey);
		when(surveyManager.loadSurvey(SURVEY_ID)).thenReturn(survey);
		when(surveyManager.get(SURVEY_NAME)).thenReturn(survey);
	}

	@Test
	public void backupsRequireAdmin() {
		BackupRestoreController controller = inject(new BackupRestoreController());
		setField(controller, "recordManager", mock(RecordManager.class));
		accessControlMock.assertChecked(() -> controller.getLatestBackupInfo(SURVEY_ID));
		verify(accessControl).checkAdmin();
		accessControlMock.assertChecked(() -> controller.startBackup(SURVEY_ID));
		verify(accessControl).checkAdmin();
		accessControlMock.assertChecked(
				() -> controller.downloadLatestBackup(SURVEY_ID, new MockHttpServletResponse()));
		verify(accessControl).checkAdmin();
		accessControlMock.assertChecked(() -> controller.downloadBackupExportResult(new MockHttpServletResponse()));
		verify(accessControl).checkAdmin();

		DataBackupController dataBackupController = new DataBackupController();
		setField(dataBackupController, "accessControl", accessControl);
		accessControlMock.assertChecked(
				() -> dataBackupController.downloadLastBackup(new MockHttpServletResponse(), SURVEY_NAME));
		verify(accessControl).checkAdmin();
	}

	@Test
	public void surveyDataRequiresSurveyAccess() {
		GeoDataController geoDataController = inject(new GeoDataController());
		accessControlMock.assertChecked(() -> geoDataController.loadCoordinateValues(SURVEY_ID, 3, "EPSG:4326", 0, 10));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(() -> geoDataController.loadGeometryValues(SURVEY_ID, 3, "EPSG:4326", 0, 10));
		verify(accessControl).checkCanAccessSurvey(survey);
		accessControlMock.assertChecked(
				() -> geoDataController.createCoordinateValuesKML(SURVEY_ID, 1, 3, new MockHttpServletResponse()));
		verify(accessControl).checkCanAccessSurvey(survey);

		SessionController sessionController = inject(new SessionController());
		accessControlMock.assertChecked(() -> sessionController.setActiveSurvey(SURVEY_ID));
		verify(accessControl).checkCanAccessSurvey(survey);

		SpeciesController speciesController = inject(new SpeciesController());
		accessControlMock.assertChecked(() -> speciesController.findTaxon(SURVEY_ID, "species", new TaxonQuery()));
		verify(accessControl).checkCanAccessSurvey(survey);
	}

	@Test
	public void recordFilesRequireRecordAccess() {
		RecordFileController controller = inject(new RecordFileController());
		SessionRecordProvider recordProvider = mock(SessionRecordProvider.class);
		when(recordProvider.provide(any(), any(), any())).thenReturn(mock(CollectRecord.class));
		setField(controller, "recordProvider", recordProvider);
		accessControlMock.assertChecked(() -> controller.downloadFile(null, new MockHttpServletResponse(), SURVEY_ID,
				RECORD_ID, Step.ENTRY, "/plot/photo"));
		verify(accessControl).checkCanViewRecord(survey, RECORD_ID);
		// preview record (kept in session)
		accessControlMock.assertChecked(() -> controller.downloadThumbnail(null, new MockHttpServletResponse(),
				SURVEY_ID, 0, Step.ENTRY, "/plot/photo"));
		verify(accessControl).checkCanAccessSurvey(survey);
	}

	@Test
	public void reportingRepositoryRequiresCleansingRoleAndNotLimitedGroupRole() {
		SaikuController controller = inject(new SaikuController());
		accessControlMock.assertChecked(() -> controller.generateRepository(SURVEY_NAME, "en"));
		BiPredicate<User, UserGroupRole> permission = accessControlMock.verifiedSurveyPermission();
		assertTrue(permission.test(user(3, UserRole.CLEANSING), UserGroupRole.OPERATOR));
		assertFalse(permission.test(user(3, UserRole.CLEANSING), UserGroupRole.DATA_CLEANER_LIMITED));
		assertFalse(permission.test(user(3, UserRole.ENTRY), UserGroupRole.OWNER));
	}

	@Test
	public void samplingPointsCanBeUpdatedOnlyInEditableSurvey() {
		SamplingPointsController controller = inject(new SamplingPointsController());
		SamplingDesignManager samplingDesignManager = mock(SamplingDesignManager.class);
		setField(controller, "samplingDesignManager", samplingDesignManager);
		SamplingDesignItem item = new SamplingDesignItem();
		accessControlMock.assertChecked(() -> controller.updateSamplingPointItem(SURVEY_ID, item));
		verify(accessControl).checkCanEditSurvey(survey);
		assertEquals(Integer.valueOf(SURVEY_ID), item.getSurveyId());
		verify(samplingDesignManager).save(item);

		SamplingDesignItem otherSurveyItem = new SamplingDesignItem();
		otherSurveyItem.setSurveyId(SURVEY_ID + 1);
		when(samplingDesignManager.loadById(5L)).thenReturn(otherSurveyItem);
		SamplingDesignItem changedItem = new SamplingDesignItem();
		changedItem.setId(5L);
		assertThrows(AccessDeniedException.class, () -> controller.updateSamplingPointItem(SURVEY_ID, changedItem));
		verify(samplingDesignManager, never()).save(changedItem);
	}

	@Test
	public void simpleSurveyCreationRequiresDesignRoleAndUserGroupAccess() {
		SimpleSurveyCreationController controller = inject(new SimpleSurveyCreationController());
		SimpleSurveyCreationParameters parameters = new SimpleSurveyCreationParameters();
		parameters.setUserGroupId(10);
		accessControlMock.assertChecked(() -> controller.createSimpleSurvey(parameters, null));
		verify(accessControl).checkRole(UserRole.DESIGN);
		verify(accessControl).checkCanAccessUserGroup(10);
	}

	@Test
	public void recordsValidationRequiresDesignRoleAndSurveyAccess() throws Exception {
		ValidationController controller = inject(new ValidationController());
		setField(controller, "recordManager", mock(RecordManager.class));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession().setAttribute(SessionState.SESSION_ATTRIBUTE_NAME,
				mock(SessionState.class, RETURNS_DEEP_STUBS));

		controller.validateAllRecords(request, new MockHttpServletResponse(), SURVEY_NAME, "plot");
		controller.validationReport(request, new MockHttpServletResponse(), SURVEY_NAME, 3, "en", null, null);
		verify(accessControl).checkRole(UserRole.DESIGN);
		verify(accessControl, times(2)).checkCanAccessSurvey(survey);

		// access denied errors are not wrapped, so they become HTTP 403
		doThrow(new AccessDeniedException("denied")).when(accessControl).checkCanAccessSurvey(survey);
		assertThrows(AccessDeniedException.class,
				() -> controller.validateAllRecords(request, new MockHttpServletResponse(), SURVEY_NAME, "plot"));
		assertThrows(AccessDeniedException.class,
				() -> controller.validationReport(request, new MockHttpServletResponse(), SURVEY_NAME, 3, "en", null, null));
	}

	private <C> C inject(C controller) {
		setField(controller, "accessControl", accessControl);
		setField(controller, "surveyManager", surveyManager);
		return controller;
	}
}
