package org.openforis.collect.model;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.DefaultAttribute;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 */
public class CollectAttribute<D extends AttributeDefinition, V extends Value> extends DefaultAttribute<D, V> {
	private Long id;
	private Character symbol;
	private String remarks;
	private boolean accepted;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Character getSymbol() {
		return symbol;
	}

	public void setSymbol(Character symbol) {
		this.symbol = symbol;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
}
