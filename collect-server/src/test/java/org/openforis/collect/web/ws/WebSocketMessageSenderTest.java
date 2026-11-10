package org.openforis.collect.web.ws;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

public class WebSocketMessageSenderTest {

	private final List<org.springframework.messaging.Message<?>> sent = new ArrayList<>();
	private WebSocketMessageSender sender;

	@Before
	public void init() {
		sender = new WebSocketMessageSender();
		ReflectionTestUtils.setField(sender, "simpMessagingTemplate",
				new SimpMessagingTemplate((message, timeout) -> sent.add(message)));
	}

	@Test
	public void testBroadcast() {
		sender.send(new Message("SURVEYS_UPDATED"));
		assertEquals("/events", SimpMessageHeaderAccessor.getDestination(sent.get(0).getHeaders()));
	}

	@Test
	public void testSendToUser() {
		sender.sendToUser("entry1", new Message("RECORD_UPDATED"));
		assertEquals("/user/entry1/events", SimpMessageHeaderAccessor.getDestination(sent.get(0).getHeaders()));
	}
}
