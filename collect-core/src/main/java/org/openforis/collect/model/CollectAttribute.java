package org.openforis.collect.model;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;

/**
 * @author G. Miceli
 */
public class CollectAttribute<D extends AttributeDefinition, V> extends Attribute<D, V> {
	public CollectAttribute(D definition) {
		super(definition);
		// TODO Auto-generated constructor stub
	}

	private Character symbol;
	private String remarks;
	private boolean accepted;

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
