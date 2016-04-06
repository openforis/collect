/**
 * 
 */
package org.openforis.collect.io.data.proxy;

import java.util.List;

import org.openforis.collect.io.ReferenceDataImportStatus;
import org.openforis.collect.io.data.CSVDataImportJob;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.manager.process.ProcessStatus.Step;
import org.openforis.collect.manager.process.proxy.ProcessStatusProxy;
import org.openforis.collect.utils.Proxies;
import org.openforis.concurrency.Worker.Status;

/**
 * @author S. Ricci
 *
 */
public class DataImportStatusProxy extends ProcessStatusProxy {
	
	private List<DataParsingErrorProxy> errors;

	public DataImportStatusProxy(CSVDataImportJob job) {
		super(createImportStatus(job));
		this.errors = Proxies.fromList(job.getParsingErrors(), DataParsingErrorProxy.class);
	}

	private static ReferenceDataImportStatus<ParsingError> createImportStatus(CSVDataImportJob job) {
		ReferenceDataImportStatus<ParsingError> processStatus = new ReferenceDataImportStatus<ParsingError>();
		processStatus.setStep(toStep(job.getStatus()));
		processStatus.setTotal(100);
		processStatus.setProcessed(job.getProgressPercent());
		processStatus.setErrorMessage(job.getErrorMessage());
		processStatus.setErrorMessageArgs(job.getErrorMessageArgs());
		return processStatus;
	}

	private static Step toStep(Status status) {
		switch (status) {
		case ABORTED:
			return Step.CANCEL;
		case COMPLETED:
			return Step.COMPLETE;
		case FAILED:
			return Step.ERROR;
		case PENDING:
			return Step.INIT;
		case RUNNING:
			return Step.RUN;
		default:
			return null;
		}
	}

	public List<DataParsingErrorProxy> getErrors() {
		return errors;
	}

}
