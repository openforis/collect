package org.openforis.collect.web.ws;

import java.util.Timer;
import java.util.TimerTask;

import org.openforis.collect.config.CollectConfiguration;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppWS {

	@Autowired
	private WebSocketMessageSender messageSender;
	@Autowired
	private SimpUserRegistry userRegistry;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;

	public enum MessageType {
		SURVEYS_UPDATED, SURVEY_UPDATED, SURVEY_PUBLISHED, SURVEY_UNPUBLISHED, SURVEY_DELETED, RECORD_LOCKED,
		RECORD_UNLOCKED, RECORD_UPDATED, RECORD_UPDATE_ERROR
	}

	public void sendMessage(MessageType type) {
		sendMessage(type, 0);
	}

	public void sendMessage(MessageType type, int delay) {
		sendMessage(new Message(type.name()), delay);
	}

	public void sendMessage(Message message) {
		sendMessage(message, 0);
	}

	public void sendMessage(final Message message, int delay) {
		if (delay > 0) {
			new Timer().schedule(new TimerTask() {
				public void run() {
					messageSender.send(message);
				}
			}, delay);
		} else {
			messageSender.send(message);
		}
	}

	public void sendMessageToUser(String username, Message message) {
		if (CollectConfiguration.isDevelopmentMode()) {
			// the dev frontend is not logged in, so its WebSocket session has no user
			messageSender.send(message);
		} else {
			messageSender.sendToUser(username, message);
		}
	}

	/**
	 * Sends a message about a record (e.g. a value update) to the connected users who can view the record, so that
	 * other users editing or viewing it get the changes too.
	 */
	public void sendRecordMessage(CollectRecord collectRecord, Message message) {
		if (CollectConfiguration.isDevelopmentMode()) {
			messageSender.send(message);
			return;
		}
		for (SimpUser connectedUser : userRegistry.getUsers()) {
			if (canViewRecord(connectedUser.getName(), collectRecord)) {
				messageSender.sendToUser(connectedUser.getName(), message);
			}
		}
	}

	private boolean canViewRecord(String username, CollectRecord collectRecord) {
		User user = userManager.loadByUserName(username);
		if (user == null) {
			return false;
		}
		if (user.hasEffectiveRole(UserRole.ADMIN)) {
			return true;
		}
		Integer surveyUserGroupId = ((CollectSurvey) collectRecord.getSurvey()).getUserGroupId();
		UserInGroup userInGroup = surveyUserGroupId == null ? null
				: userGroupManager.findUserInGroupOrDescendants(surveyUserGroupId, user.getId());
		if (userInGroup == null) {
			return false;
		}
		// limited users can see only their own records
		boolean limited = user.hasRole(UserRole.ENTRY_LIMITED) || userInGroup.getRole() == UserGroupRole.DATA_CLEANER_LIMITED;
		return !limited || collectRecord.getOwner() != null && user.getId().equals(collectRecord.getOwner().getId());
	}

	public static class SurveyMessage extends Message {

		private int surveyId;

		public SurveyMessage(MessageType messageType, int surveyId) {
			super(messageType.name());
			this.surveyId = surveyId;
		}

		public int getSurveyId() {
			return surveyId;
		}
	}

	private static abstract class RecordMessage extends Message {

		private Integer recordId;

		public RecordMessage(MessageType type, int recordId) {
			super(type.name());
			this.recordId = recordId;
		}

		public Integer getRecordId() {
			return recordId;
		}

	}

	public static class RecordLockedMessage extends RecordMessage {

		private String lockedBy;

		public RecordLockedMessage(int recordId, String lockedBy) {
			super(MessageType.RECORD_LOCKED, recordId);
			this.lockedBy = lockedBy;
		}

		public String getLockedBy() {
			return lockedBy;
		}
	}

	public static class RecordUnlockedMessage extends RecordMessage {

		public RecordUnlockedMessage(int recordId) {
			super(MessageType.RECORD_UNLOCKED, recordId);
		}
	}

	public static class RecordEventMessage extends Message {
		private Object event;

		public RecordEventMessage(Object event) {
			super(MessageType.RECORD_UPDATED.name());
			this.event = event;
		}

		public Object getEvent() {
			return event;
		}
	}

	public static class RecordUpdateErrorMessage extends Message {

		private String message;
		private String stackTrace;

		public RecordUpdateErrorMessage(String message, String stackTrace) {
			super(MessageType.RECORD_UPDATE_ERROR.name());
			this.message = message;
			this.stackTrace = stackTrace;
		}

		public String getMessage() {
			return message;
		}

		public String getStackTrace() {
			return stackTrace;
		}
	}
}
