package org.openforis.collect.web.ws;

import java.util.Timer;
import java.util.TimerTask;

import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppWS {

	@Autowired
	private WebSocketMessageSender messageSender;
	
	public enum MessageType {
		SURVEYS_UPDATED,
		RECORD_LOCKED,
		RECORD_UNLOCKED,
		RECORD_UPDATED,
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

	private void sendMessage(final Message message, int delay) {
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
	
	public static class SurveyUpdateMessage extends Message {
		
		private SurveySummary survey;
		
		public SurveyUpdateMessage(MessageType updateType, SurveySummary survey) {
			super(updateType.name());
			this.survey = survey;
		}
		
		public SurveySummary getSurvey() {
			return survey;
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
}
