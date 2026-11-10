package org.openforis.collect.web.controller;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.BiPredicate;

import org.mockito.ArgumentCaptor;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.springframework.security.access.AccessDeniedException;

/**
 * Mocked {@link AccessControl} for controller tests: it grants every access check, unless denying is enabled.
 */
class AccessControlMock {

	interface Endpoint {
		void call() throws Exception;
	}

	private boolean denying = false;

	final AccessControl accessControl = mock(AccessControl.class, invocation -> {
		if (denying) {
			throw new AccessDeniedException("denied");
		}
		return RETURNS_DEFAULTS.answer(invocation);
	});

	/**
	 * The endpoint must fail when access is denied. Then it is called with access granted, so that the checks it
	 * performs can be verified: the business logic after them is not under test and may fail on mocked collaborators.
	 */
	void assertChecked(Endpoint endpoint) {
		denying = true;
		try {
			assertThrows(AccessDeniedException.class, endpoint::call);
		} finally {
			denying = false;
		}
		clearInvocations(accessControl);
		try {
			endpoint.call();
		} catch (AccessDeniedException e) {
			throw e;
		} catch (Exception e) {
			// business logic failing on mocked collaborators
		}
	}

	/**
	 * Permission verified by the last endpoint called with {@link AccessControl#checkSurveyPermission}
	 */
	@SuppressWarnings("unchecked")
	BiPredicate<User, UserGroupRole> verifiedSurveyPermission() {
		ArgumentCaptor<BiPredicate<User, UserGroupRole>> captor = ArgumentCaptor.forClass(BiPredicate.class);
		verify(accessControl).checkSurveyPermission(any(), captor.capture());
		return captor.getValue();
	}
}
