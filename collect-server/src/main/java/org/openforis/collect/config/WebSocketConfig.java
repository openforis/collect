package org.openforis.collect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	private static final String SOCK_JS_VERSION = "1.5.0";

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		super.configureMessageBroker(registry);
		registry.setPreservePublishOrder(true);
	}

	/**
	 * Register Stomp endpoints: the url to open the WebSocket connection.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {

		// Register the "/ws" endpoint, enabling the SockJS protocol.
		// SockJS is used (both client and server side) to allow alternative
		// messaging options if WebSocket is not available.
		registry.addEndpoint("/ws")
//				.setHandshakeHandler(new DefaultHandshakeHandler(new JettyRequestUpgradeStrategy()))
				.setAllowedOrigins("*")
				.withSockJS()
				.setClientLibraryUrl("../assets/js/sockjs/" + SOCK_JS_VERSION + "/sockjs.js")
				;
	}
}
