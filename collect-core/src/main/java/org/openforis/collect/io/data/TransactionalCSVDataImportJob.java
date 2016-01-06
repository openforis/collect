package org.openforis.collect.io.data;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uses a transaction to update the records in the database.
 * Records are updated only and only if there are no errors and the process is not cancelled.
 * 
 * @author S. Ricci
 *
 */
@Component(TransactionalCSVDataImportJob.BEAN_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransactionalCSVDataImportJob extends CSVDataImportJob {
	
	public static final String BEAN_NAME = "transactionalCsvDataImportJob";
	
	@Override
	@Transactional(rollbackFor=ImportException.class)
	public synchronized void run() {
		super.run();
		if (isFailed() || isAborted()) {
			throw new ImportException();
		}
	}
	
}
