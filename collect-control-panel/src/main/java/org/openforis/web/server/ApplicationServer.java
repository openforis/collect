package org.openforis.web.server;

import java.nio.file.Path;

public interface ApplicationServer {

	void start() throws Exception;

	void stop() throws Exception;

	boolean isRunning();

	String getUrl();

	Path getLog();

}