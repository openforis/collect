package org.openforis.web.server;

import java.io.IOException;

public interface ApplicationServer {

	void initialize() throws IOException;

	void start() throws Exception;

	void stop() throws Exception;

	boolean isRunning();

	String getUrl();

}