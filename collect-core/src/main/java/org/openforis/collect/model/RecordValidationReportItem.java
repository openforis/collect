/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * @author S. Ricci
 *
 */
public class RecordValidationReportItem {

	private Integer nodeId;
	private String path;
	private String prettyFormatPath;
	private ValidationResultFlag severity;
	private String message;
	
	public RecordValidationReportItem(String path, String prettyFormatPath, ValidationResultFlag severity,
			String message) {
		this(null, path, prettyFormatPath, severity, message);
	}

	public RecordValidationReportItem(Integer nodeId, String path, String prettyFormatPath,
			ValidationResultFlag severity, String message) {
		super();
		this.nodeId = nodeId;
		this.path = path;
		this.prettyFormatPath = prettyFormatPath;
		this.severity = severity;
		this.message = message;
	}

	public String getParentEntityPath() {
		int lastIndexOfSlash = path.lastIndexOf('/');
		return path.substring(0, lastIndexOfSlash > 0 ? lastIndexOfSlash: path.length());
	}

	public String getNodeName() {
		int lastIndexOfSlash = path.lastIndexOf('/');
		return path.substring(lastIndexOfSlash + 1);
	}
	
	public Integer getNodeId() {
		return nodeId;
	}

	public String getPath() {
		return path;
	}

	public String getPrettyFormatPath() {
		return prettyFormatPath;
	}
	
	public ValidationResultFlag getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}
	
}
