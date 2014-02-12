package org.openforis.collect.remoting.service.dataimport;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.io.data.DataImportState;
import org.openforis.collect.io.data.DataImportState.MainStep;
import org.openforis.collect.io.data.DataImportState.SubStep;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataImportStateProxy implements Proxy {
	
	private transient DataImportState state;

	public CSVDataImportStateProxy(DataImportState state) {
		super();
		this.state = state;
	}

	@ExternalizedProperty
	public MainStep getMainStep() {
		return state.getMainStep();
	}

	@ExternalizedProperty
	public SubStep getSubStep() {
		return state.getSubStep();
	}

	@ExternalizedProperty
	public boolean isRunning() {
		return state.isRunning();
	}

	@ExternalizedProperty
	public boolean isError() {
		return state.isError();
	}

	@ExternalizedProperty
	public boolean isCancelled() {
		return state.isCancelled();
	}

	@ExternalizedProperty
	public int getCount() {
		return state.getCount();
	}

	@ExternalizedProperty
	public int getTotal() {
		return state.getTotal();
	}

	@ExternalizedProperty
	public boolean isComplete() {
		return state.isComplete();
	}

	@ExternalizedProperty
	public String getErrorMessage() {
		return state.getErrorMessage();
	}

	@ExternalizedProperty
	public int getInsertedCount() {
		return state.getInsertedCount();
	}

	@ExternalizedProperty
	public int getUpdatedCount() {
		return state.getUpdatedCount();
	}

	@ExternalizedProperty
	public Map<String, String> getErrors() {
		return state.getErrors();
	}

	@ExternalizedProperty
	public Map<String, Map<String, String>> getWarnings() {
		return state.getWarnings();
	}

}
