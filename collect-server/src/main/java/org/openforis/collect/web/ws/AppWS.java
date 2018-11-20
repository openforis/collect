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
		SURVEYS_UPDATED
	}
	
	public void sendMessage(MessageType type) {
		sendMessage(type, 0);
	}
	
	public void sendMessage(MessageType type, int delay) {
		new Timer().schedule(new TimerTask() {
			public void run() {
				messageSender.send(new Message(type.name()));
			}
		}, delay);
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

}
