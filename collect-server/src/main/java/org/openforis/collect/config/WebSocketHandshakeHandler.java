package org.openforis.collect.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.Environment;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class WebSocketHandshakeHandler extends DefaultHandshakeHandler {

	private static Logger LOG = LogManager.getLogger(WebSocketHandshakeHandler.class);

	public WebSocketHandshakeHandler() {
		super(createRequestUpgradeStrategy());
	}

	private static RequestUpgradeStrategy createRequestUpgradeStrategy() {
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
