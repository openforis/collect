package org.openforis.collect.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageSender {
	private static final String DESTINATION = "/events";

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	public void send(Message message) {
		this.simpMessagingTemplate.convertAndSend(DESTINATION, message);
	}
	
	public static abstract class Message {
		
		private String type;

		public Message(String type) {
			super();
			this.type = type;
		}
		
		public String getType() {
			return type;
		}
	}
}
