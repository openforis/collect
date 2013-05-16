/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import org.openforis.collect.model.NodeChange.AttributeAddChange;
import org.openforis.collect.model.NodeChange.AttributeChange;
import org.openforis.collect.model.NodeChange.EntityAddChange;
import org.openforis.collect.model.NodeChange.EntityChange;
import org.openforis.collect.model.NodeChange.NodeDeleteChange;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class NodeChangeSet {

	private NodeChangeMap changeMap;
	private Integer errors;
	private Integer skipped;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer warnings;
	private boolean recordSaved;
	
	public NodeChangeSet() {
		changeMap = new NodeChangeMap();
	}
	
	public List<NodeChange<?>> getChanges() {
		return changeMap.values();
	}

	public void addChange(NodeChange<?> c) {
		NodeChange<?> oldItem = changeMap.getChange(c.getNode());
		if ( oldItem == null || ! (oldItem instanceof NodeDeleteChange) ) {
			if ( oldItem instanceof AttributeAddChange && c instanceof AttributeChange ) {
				((AttributeAddChange) oldItem).merge((AttributeChange) c);
			} else if ( oldItem instanceof EntityAddChange && c instanceof EntityChange ) {
				((EntityAddChange) oldItem).merge((EntityChange) c);
			} else {
				changeMap.putChange(c);
			}
		}
	}
	
	public NodeChange<?> getChange(Node<?> node) {
		return changeMap.getChange(node);
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

	public boolean isRecordSaved() {
		return recordSaved;
	}

	public void setRecordSaved(boolean recordSaved) {
		this.recordSaved = recordSaved;
	}

}
