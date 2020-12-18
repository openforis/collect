package org.openforis.collect.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.Environment;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	private static Logger LOG = LogManager.getLogger(WebSocketConfig.class);

	private static final String ENDPOINT = "/ws";
	private static final String ALLOWED_ORIGINS = "*";
	private static final String SOCK_JS_VERSION = "1.5.0";
	private static final String SOCK_JS_CLIENT_LIBRARY_URL = "../assets/js/sockjs/" + SOCK_JS_VERSION + "/sockjs.js";

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
		RequestUpgradeStrategy requestUpgradeStrategy = createRequestUpgradeStrategy();
		if (requestUpgradeStrategy == null) {
			LOG.info("no upgrade strategy in use");

			registry.addEndpoint(ENDPOINT)
				.setAllowedOrigins(ALLOWED_ORIGINS)
				.withSockJS()
				.setClientLibraryUrl(SOCK_JS_CLIENT_LIBRARY_URL);
		} else {
			LOG.info("using " + requestUpgradeStrategy.getClass().getSimpleName());
			
			registry.addEndpoint(ENDPOINT)
				.setHandshakeHandler(new DefaultHandshakeHandler(requestUpgradeStrategy))
				.setAllowedOrigins(ALLOWED_ORIGINS)
				.withSockJS()
				.setClientLibraryUrl(SOCK_JS_CLIENT_LIBRARY_URL);
		}
	}

	private RequestUpgradeStrategy createRequestUpgradeStrategy() {
		if (Environment.isServerJetty()) {
			LOG.info("running in Jetty application server");
			return new JettyRequestUpgradeStrategy();
		}
		if (Environment.isServerTomcat()) {
			LOG.info("running in Tomcat application server");
			return new TomcatRequestUpgradeStrategy();
		}
		return null;
	}
}
