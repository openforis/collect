package org.openforis.collect.model;

import org.openforis.idm.model.AttributeMetadata;

/**
 * @author G. Miceli
 */
public class CollectAttributeMetadata implements AttributeMetadata {
	// TODO replace symbol with enum
	
	//private Character symbol;
//	private String remarks;
	// TODO replace state with enum
	private Character state;

	public CollectAttributeMetadata() {
	}
	
	public CollectAttributeMetadata(Character state) {
//		this.symbol = symbol;
//		this.remarks = remarks;
		this.state = state;
	}
//
//	public Character getSymbol() {
//		return symbol;
//	}
//
//	public void setSymbol(Character symbol) {
//		this.symbol = symbol;
//	}
//
//	public String getRemarks() {
//		return remarks;
//	}
//
//	public void setRemarks(String remarks) {
//		this.remarks = remarks;
//	}

	public Character getState() {
		return state;
	}
	
	public void setState(Character state) {
		this.state = state;
	}
}
