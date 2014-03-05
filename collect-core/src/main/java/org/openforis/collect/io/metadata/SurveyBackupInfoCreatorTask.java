package org.openforis.collect.io.metadata;

import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

import org.openforis.collect.Collect;
import org.openforis.collect.utils.Dates;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyBackupInfoCreatorTask extends Task {

	private OutputStream outputStream;
	
	@Override
	protected void execute() throws Throwable {
		Properties prop = new Properties();
		prop.setProperty("collect_version", Collect.getVersion());
		prop.setProperty("date", Dates.formatDateToXML(new Date()));
		prop.store(outputStream, null);
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
}
