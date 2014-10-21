package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.ITEM;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ITEMS;



/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
class CodeListItemsPR extends IdmlPullReader {
	
	private boolean includeChildItems;

	public CodeListItemsPR() {
		this(false);
	}
	
	public CodeListItemsPR(boolean includeChildItems) {
		super(ITEMS, 1);
		this.includeChildItems = includeChildItems;
		addChildPullReaders(createNewItemPR());
	}

	protected XmlPullReader createNewItemPR() {
		if ( includeChildItems ) {
			return new CodeListItemPR();
		} else {
			return new SkipElementPR(ITEM);
		}
	}
}