package org.openforis.idm.metamodel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListLabelMap extends TypedLanguageSpecificTextAbstractMap<CodeListLabel, CodeListLabel.Type> {

	private static final long serialVersionUID = 1L;

	
	public CodeListLabelMap() {
		super();
	}
	
	public CodeListLabelMap(CodeListLabelMap source) {
		super(source);
	}
}
