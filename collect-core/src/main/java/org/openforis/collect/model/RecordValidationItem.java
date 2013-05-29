/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * @author S. Ricci
 *
 */
public class RecordValidationItem {

	private Integer nodeId;
	private String path;
	private ValidationResultFlag severity;
	private String message;
	
	public RecordValidationItem(Integer nodeId, String path,
			ValidationResultFlag severity, String message) {
		super();
		this.nodeId = nodeId;
		this.path = path;
		this.severity = severity;
		this.message = message;
	}

	public RecordValidationItem(String path, ValidationResultFlag severity,
			String message) {
		super();
		this.path = path;
		this.severity = severity;
		this.message = message;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public String getPath() {
		return path;
	}

	public ValidationResultFlag getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}
	
}
