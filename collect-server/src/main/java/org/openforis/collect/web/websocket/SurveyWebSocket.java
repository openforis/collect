package org.openforis.collect.web.websocket;

import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.websocket.WebSocketMessageSender.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SurveyWebSocket {

	@Autowired
	private WebSocketMessageSender messageSender;
	
	public enum SurveyMessageType {
		SURVEY_CREATED, SURVEY_UPDATED, SURVEY_DELETED, SURVEY_PUBLISHED, SURVEY_UNPUBLISHED
	}
	
	public void sendMessage(SurveyMessageType type, SurveySummary survey) {
		messageSender.send(new UpdateMessage(type, survey));
	}
	
	public static class UpdateMessage extends Message {
		
		private SurveySummary survey;
		
		public UpdateMessage(SurveyMessageType updateType, SurveySummary survey) {
			super(updateType.name());
			this.survey = survey;
		}
		
		public SurveySummary getSurvey() {
			return survey;
		}
	}

}
