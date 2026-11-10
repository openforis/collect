package org.openforis.collect.web.ws;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.config.CollectConfiguration;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

public class AppWSTest {

	private static final int SURVEY_USER_GROUP_ID = 10;

	private final List<String> sent = new ArrayList<String>();
	private final Set<SimpUser> connectedUsers = new HashSet<SimpUser>();
	private final UserManager userManager = mock(UserManager.class);
	private final UserGroupManager userGroupManager = mock(UserGroupManager.class);
	private AppWS appWS;

	@Before
	public void init() {
		WebSocketMessageSender messageSender = new WebSocketMessageSender() {
			@Override
			public void send(Message message) {
				sent.add("all");
			}

			@Override
			public void sendToUser(String username, Message message) {
				sent.add(username);
			}
		};
		SimpUserRegistry userRegistry = mock(SimpUserRegistry.class);
		when(userRegistry.getUsers()).thenReturn(connectedUsers);
		appWS = new AppWS(messageSender, userRegistry, userManager, userGroupManager);
	}

	@After
	public void resetDevelopmentMode() {
		CollectConfiguration.setDevelopmentMode(false);
	}

	@Test
	public void testMessageSentToUser() {
		appWS.sendMessageToUser("entry1", new Message("RECORD_UPDATED"));
		assertEquals(Arrays.asList("entry1"), sent);
	}

	@Test
	public void testMessageBroadcastInDevelopmentMode() {
		CollectConfiguration.setDevelopmentMode(true);
		appWS.sendMessageToUser("entry1", new Message("RECORD_UPDATED"));
		appWS.sendRecordMessage(createRecord(null), new Message("RECORD_UPDATED"));
		assertEquals(Arrays.asList("all", "all"), sent);
	}

	@Test
	public void testRecordMessageSentToConnectedUsersWhoCanViewTheRecord() {
		User owner = connectedUser(1, "owner", UserRole.ENTRY, UserGroupRole.OPERATOR);
		connectedUser(2, "colleague", UserRole.CLEANSING, UserGroupRole.OPERATOR);
		connectedUser(3, "limited", UserRole.ENTRY_LIMITED, UserGroupRole.OPERATOR);
		connectedUser(4, "limitedcleaner", UserRole.CLEANSING, UserGroupRole.DATA_CLEANER_LIMITED);
		connectedUser(5, "outsider", UserRole.ENTRY, null);
		connectedUser(6, "admin", UserRole.ADMIN, null);
		connectedUser(7, "deleted", null, null);

		appWS.sendRecordMessage(createRecord(owner), new Message("RECORD_UPDATED"));

		assertEquals(new HashSet<String>(Arrays.asList("owner", "colleague", "admin")), new HashSet<String>(sent));
	}

	@Test
	public void testLimitedUserGetsOwnRecordMessages() {
		User limited = connectedUser(3, "limited", UserRole.ENTRY_LIMITED, UserGroupRole.OPERATOR);

		appWS.sendRecordMessage(createRecord(limited), new Message("RECORD_UPDATED"));

		assertEquals(Arrays.asList("limited"), sent);
	}

	/**
	 * @param role null for a user connected but not existing anymore
	 * @param groupRole null if the user doesn't belong to the survey user group
	 */
	private User connectedUser(int id, String name, UserRole role, UserGroupRole groupRole) {
		SimpUser simpUser = mock(SimpUser.class);
		when(simpUser.getName()).thenReturn(name);
		connectedUsers.add(simpUser);
		if (role == null) {
			return null;
		}
		User user = new User(name);
		user.setId(id);
		user.addRole(role);
		when(userManager.loadByUserName(name)).thenReturn(user);
		if (groupRole != null) {
			UserInGroup userInGroup = new UserInGroup();
			userInGroup.setUserId(id);
			userInGroup.setRole(groupRole);
			when(userGroupManager.findUserInGroupOrDescendants(SURVEY_USER_GROUP_ID, id)).thenReturn(userInGroup);
		}
		return user;
	}

	private CollectRecord createRecord(User owner) {
		CollectSurvey survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		survey.setUserGroupId(SURVEY_USER_GROUP_ID);
		EntityDefinition rootEntityDefn = survey.getSchema().createEntityDefinition();
		rootEntityDefn.setName("plot");
		survey.getSchema().addRootEntityDefinition(rootEntityDefn);
		CollectRecord collectRecord = new CollectRecord(survey, null, rootEntityDefn, false);
		collectRecord.setOwner(owner);
		return collectRecord;
	}
}
