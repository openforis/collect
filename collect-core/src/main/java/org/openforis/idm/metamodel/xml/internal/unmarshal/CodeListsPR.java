package org.openforis.idm.metamodel.xml.internal.unmarshal;


import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
class CodeListsPR extends IdmlPullReader {
	
	private boolean includeChildItems;

	public CodeListsPR() {
		this(false);
	}
	
	public CodeListsPR(boolean includeChildItems) {
		super(CODE_LISTS, 1);
		this.includeChildItems = includeChildItems;
		addChildPullReaders(createCodeListReader());
	}

	protected CodeListPR createCodeListReader() {
		return new CodeListPR(includeChildItems);
	}
	
}