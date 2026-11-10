package org.openforis.collect.web.controller;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.openforis.collect.manager.LogoManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class LogoControllerTest {

	@Test
	public void onlyAdminCanUploadLogo() throws Exception {
		SessionManager sessionManager = mock(SessionManager.class);
		LogoManager logoManager = mock(LogoManager.class);
		LogoController controller = new LogoController(new AccessControl(sessionManager, null, null));
		ReflectionTestUtils.setField(controller, "logoManager", logoManager);
		UploadItem uploadItem = new UploadItem();
		CommonsMultipartFile file = mock(CommonsMultipartFile.class);
		when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 1 }));
		uploadItem.setFileData(file);

		when(sessionManager.getLoggedUser()).thenReturn(user(1, UserRole.DESIGN));
		assertThrows(AccessDeniedException.class, () -> controller.uploadLogo(uploadItem, null, "header"));
		verifyNoInteractions(logoManager);

		when(sessionManager.getLoggedUser()).thenReturn(user(1, UserRole.ADMIN));
		controller.uploadLogo(uploadItem, null, "header");
		verify(logoManager).save(any());
	}
}
