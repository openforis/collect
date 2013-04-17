/**
 * 
 */
package org.openforis.collect.model;

import java.util.Collection;

import org.openforis.collect.model.NodeUpdateResponse.NodeDeleteResponse;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateResponseSet {

	private NodeUpdateResponseMap responseMap;
	private Integer errors;
	private Integer skipped;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer warnings;

	public RecordUpdateResponseSet() {
		responseMap = new NodeUpdateResponseMap();
	}
	
	public Collection<NodeUpdateResponse<?>> getResponses() {
		return responseMap.values();
	}

	public void addResponse(NodeUpdateResponse<?> response) {
		NodeUpdateResponse<?> oldResponse = responseMap.getResponse(response.getNode());
		if ( ! (oldResponse instanceof NodeDeleteResponse) ) {
			responseMap.putResponse(response);
		}
	}
	
	public NodeUpdateResponse<?> getResponse(Node<?> node) {
		return responseMap.getResponse(node);
	}
	
	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getWarnings() {
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public Integer getMissingErrors() {
		return missingErrors;
	}

	public void setMissingErrors(Integer missingErrors) {
		this.missingErrors = missingErrors;
	}

	public Integer getMissingWarnings() {
		return missingWarnings;
	}

	public void setMissingWarnings(Integer missingWarnings) {
		this.missingWarnings = missingWarnings;
	}

}
