package org.openforis.collect.io.metadata.collectearth.balloon;


public class CERangeField extends CEField {

	
	private Integer from;
	private Integer to;

	public CERangeField(String htmlParameterName, String name, String label, String tooltip, CEFieldType type, boolean multiple, boolean key, Integer from, Integer to) {
		super(htmlParameterName, name, label, tooltip, multiple, type, key);
		this.from = from;;
		this.to = to;;
	}

	public Integer getFrom() {
		return from;
	}

	public Integer getTo() {
		return to;
	}


}
