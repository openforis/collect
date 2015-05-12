package org.openforis.collect.io.metadata.collectearth.balloon;

/**
 * 
 * @author S. Ricci
 *
 */
public class CETab extends CEFieldSet {

	private boolean main;
	
	public CETab(String name, String label) {
		super(name, label);
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

}
