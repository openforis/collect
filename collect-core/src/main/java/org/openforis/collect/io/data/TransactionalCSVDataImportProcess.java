package org.openforis.collect.io.data;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uses a transaction to update the records in the database.
 * Records are updated only and only if there is are no errors and the process is not cancelled.
 * 
 * @author S. Ricci
 *
 */
@Component("transactionalCsvDataImportProcess")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class TransactionalCSVDataImportProcess extends CSVDataImportProcess {
	
	@Override
	@Transactional(rollbackFor=ImportException.class)
	public Void call() throws Exception {
		Void result = super.call();
		if ( ! status.isComplete() ) {
			//rollback transaction
			throw new ImportException();
		}
		return result;
	}

}
