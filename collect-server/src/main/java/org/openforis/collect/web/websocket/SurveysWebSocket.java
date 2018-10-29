package org.openforis.collect.web.websocket;

import org.openforis.collect.model.SurveySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SurveysWebSocket {

	private static final String SURVEYS_UPDATE_MESSAGE_DESTINATION = "/surveys/update";

	public enum UpdateType {
		CREATED, UPDATED, DELETED, PUBLISHED, UNPUBLISHED
	}
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	public void sendSurveyUpdatedMessage(SurveySummary survey, UpdateType updateType) {
		this.simpMessagingTemplate.convertAndSend(SURVEYS_UPDATE_MESSAGE_DESTINATION, 
				new UpdateMessage(updateType, survey));
	}
	
	public static class UpdateMessage {
		
		private UpdateType updateType;
		private SurveySummary survey;
		
		public UpdateMessage(UpdateType updateType, SurveySummary survey) {
			super();
			this.updateType = updateType;
			this.survey = survey;
		}
		
		public UpdateType getUpdateType() {
			return updateType;
		}
		
		public SurveySummary getSurvey() {
			return survey;
		}
	}
	
}
