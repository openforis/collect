package liquibase.ext.logging;


import static liquibase.logging.LogLevel.DEBUG;
import static liquibase.logging.LogLevel.INFO;
import static liquibase.logging.LogLevel.SEVERE;
import static liquibase.logging.LogLevel.WARNING;
import liquibase.logging.LogLevel;
import liquibase.logging.core.AbstractLogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Stupid class, to enable liquibase logging.
 * <p>
 * Liquibase finds this class by itself by doing a custom component scan (they though sl4fj wasn't generic enough).
 * 
 * @author S. Ricci
 */
public class CollectLiquibaseLogger extends AbstractLogger {
	
	private static final Log LOGGER = LogFactory.getLog(CollectLiquibaseLogger.class);
	
	@Override
	public void setName(String name) {
	}

	@Override
	public void severe(String message) {
		print(SEVERE, message);
	}

	@Override
	public void severe(String message, Throwable e) {
		print(SEVERE, message, e);		
	}

	@Override
	public void warning(String message) {
		print(WARNING, message);		
	}

	@Override
	public void warning(String message, Throwable e) {
		print(WARNING, message, e);		
	}

	@Override
	public void info(String message) {
		print(INFO, message);		
	}

	@Override
	public void info(String message, Throwable e) {
		print(INFO, message, e);		
	}

	@Override
	public void debug(String message) {
		print(DEBUG, message);		
	}

	@Override
	public void debug(String message, Throwable e) {
		print(DEBUG, message, e);		
	}
	
	@Override
	public void setLogLevel(String logLevel, String logFile) {
		super.setLogLevel(logLevel);
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

	private void print(LogLevel logLevel, String message) {
		print(logLevel, message, null);
	}
	
	private void print(LogLevel logLevel, String message, Throwable e) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        switch (logLevel) {
		case SEVERE:
			LOGGER.error(message, e);
			break;
		case WARNING:
			LOGGER.warn(message, e);
			break;
		case INFO:
			LOGGER.info(message, e);
			break;
		case DEBUG:
			LOGGER.debug(message, e);
			break;
		default:
			break;
        }
	}
	
}
