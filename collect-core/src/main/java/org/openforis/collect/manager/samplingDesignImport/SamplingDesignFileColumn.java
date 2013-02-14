package org.openforis.collect.manager.samplingDesignImport;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SamplingDesignFileColumn {
	LEVEL_1(0, "level1_code"), 
	LEVEL_2(1, "level2_code"), 
	LEVEL_3(2, "level3_code"), 
	LOCATION(3, "location");
	
	private int index;
	private String name;
	
	private SamplingDesignFileColumn(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getName() {
		return name;
	}
}