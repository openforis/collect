package org.openforis.collect.io.metadata.collectearth.balloon;

/**
 * 
 * @author S. Ricci
 *
 */
public class CEEnumeratingCodeField extends CEField {

	public CEEnumeratingCodeField(String htmlParameterName, String name,
			String label, String tooltip, boolean multiple, CEFieldType type, boolean key) {
		super(htmlParameterName, name, label, tooltip, multiple, type, key);
		this.setReadOnly(true);
	}

}
