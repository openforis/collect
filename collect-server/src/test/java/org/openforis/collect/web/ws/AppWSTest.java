package org.openforis.collect.web.ws;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.config.CollectConfiguration;
import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.springframework.test.util.ReflectionTestUtils;

public class AppWSTest {

	private final List<String> sent = new ArrayList<String>();
	private AppWS appWS;

	@Before
	public void init() {
		appWS = new AppWS();
		ReflectionTestUtils.setField(appWS, "messageSender", new WebSocketMessageSender() {
			@Override
			public void send(Message message) {
				sent.add("all");
			}

			@Override
			public void sendToUser(String username, Message message) {
				sent.add(username);
			}
		});
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
		assertEquals(Arrays.asList("all"), sent);
	}
}
