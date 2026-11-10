package org.openforis.collect.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DataRestoreControllerAccessTest {

	private static final int SURVEY_GROUP_ID = 10;
	private static final String SURVEY_NAME = "test";
	private static final String SURVEY_URI = "http://www.openforis.org/collect/test";
	private static final String JOB_ID = "job";
	private static final String OVERWRITE_OLDER = "OVERWRITE_OLDER";

	private final DataRestoreJob job = mock(DataRestoreJob.class);
	private final Configuration configuration = new Configuration();

	@Mock
	private SessionManager sessionManager;
	@Mock
	private UserGroupManager userGroupManager;
	@Mock
	private SurveyManager surveyManager;
	@Mock
	private CollectJobManager jobManager;
	@Mock
	private ConfigurationManager configurationManager;
	@Mock
	private UserManager userManager;
	@InjectMocks
	private DataRestoreController controller;

	@Before
	public void init() {
		AccessControl accessControl = new AccessControl();
		ReflectionTestUtils.setField(accessControl, "sessionManager", sessionManager);
		ReflectionTestUtils.setField(accessControl, "userGroupManager", userGroupManager);
		ReflectionTestUtils.setField(controller, "accessControl", accessControl);
		CollectSurvey survey = mock(CollectSurvey.class);
		when(survey.getUserGroupId()).thenReturn(SURVEY_GROUP_ID);
		when(surveyManager.getByUri(SURVEY_URI)).thenReturn(survey);
		// only user 1 belongs to the survey user group
		when(userGroupManager.findUserInGroupOrDescendants(SURVEY_GROUP_ID, 1)).thenReturn(new UserInGroup());
		when(jobManager.createJob(DataRestoreJob.JOB_NAME, DataRestoreJob.class)).thenReturn(job);
		when(jobManager.getJob(JOB_ID)).thenReturn(job);
		when(job.getId()).thenReturn(UUID.randomUUID());
		when(job.getUser()).thenReturn(user(1, UserRole.ENTRY));
		when(configurationManager.getConfiguration()).thenReturn(configuration);
	}

	@Test
	public void entryUserCanRestoreRecordsOnlyIntoAccessibleSurveys() throws Exception {
		login(2, UserRole.ENTRY);
		assertTrue(controller.restoreData(backupFile(), SURVEY_NAME, true, false, OVERWRITE_OLDER).isStatusError());
		verify(jobManager, never()).start(any(), anyString());

		login(1, UserRole.ENTRY);
		assertTrue(controller.restoreData(backupFile(), SURVEY_NAME, true, false, OVERWRITE_OLDER).isStatusOk());
		verify(jobManager).start(job, SURVEY_URI);
	}

	@Test
	public void adminIsRequiredToRestoreNewSurveyOrToDeleteAllRecords() throws Exception {
		login(1, UserRole.CLEANSING);
		assertThrows(AccessDeniedException.class,
				() -> controller.restoreData(backupFile(), null, true, false, OVERWRITE_OLDER));
		assertThrows(AccessDeniedException.class,
				() -> controller.restoreData(backupFile(), SURVEY_NAME, true, true, OVERWRITE_OLDER));

		login(2, UserRole.ADMIN);
		assertTrue(controller.restoreData(backupFile(), SURVEY_NAME, true, true, OVERWRITE_OLDER).isStatusOk());
		verify(jobManager).start(job, SURVEY_URI);
	}

	@Test
	public void restoreJobCanBeFollowedOnlyByItsUserOrByAdmins() throws Exception {
		login(1, UserRole.ENTRY);
		assertTrue(controller.getRestoreDataRemotelyStatus(JOB_ID).isStatusOk());

		login(2, UserRole.ENTRY);
		assertThrows(AccessDeniedException.class, () -> controller.getRestoreDataRemotelyStatus(JOB_ID));
		assertThrows(AccessDeniedException.class, () -> controller.abortRestoreDataRemotelyJob(JOB_ID));
		verify(job, never()).abort();

		login(3, UserRole.ADMIN);
		controller.abortRestoreDataRemotelyJob(JOB_ID);
		verify(job).abort();
	}

	@Test
	public void remoteRestoreRequiresConfiguredRestoreKey() {
		UploadItem uploadItem = new UploadItem();
		assertEquals("Restore not allowed: invalid restore key",
				controller.restoreDataRemotely(uploadItem, SURVEY_NAME, "").getErrorMessage());
		configuration.put(ConfigurationItem.ALLOWED_RESTORE_KEY, "secret");
		assertTrue(controller.restoreDataRemotely(uploadItem, SURVEY_NAME, null).isStatusError());
		assertTrue(controller.restoreDataRemotely(uploadItem, SURVEY_NAME, "wrong").isStatusError());
		verifyNoInteractions(userManager);

		controller.restoreDataRemotely(uploadItem, SURVEY_NAME, "secret");
		verify(userManager).loadAdminUser();
	}

	private void login(int userId, UserRole role) {
		when(sessionManager.getLoggedUser()).thenReturn(user(userId, role));
	}

	private static MockMultipartFile backupFile() throws IOException {
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(content)) {
			zip.putNextEntry(new ZipEntry(SurveyBackupJob.INFO_FILE_NAME));
			SurveyBackupInfo info = new SurveyBackupInfo();
			info.setSurveyUri(SURVEY_URI);
			info.setSurveyName(SURVEY_NAME);
			info.store(zip);
		}
		return new MockMultipartFile("file", content.toByteArray());
	}
}
