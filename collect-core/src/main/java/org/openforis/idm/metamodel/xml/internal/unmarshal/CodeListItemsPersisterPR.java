package org.openforis.idm.metamodel.xml.internal.unmarshal;

import java.io.IOException;

import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;


/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemsPersisterPR extends CodeListItemsPR {

	private int lastChildSortOrder;
	
	CodeListItemsPersisterPR() {
		super();
		lastChildSortOrder = 0;
	}

	@Override
	protected XmlPullReader createNewItemPR() {
		return new CodeListItemPersisterPR();
	}
	
	public int nextChildSortOrder() {
		return ++lastChildSortOrder;
	}
	
	@Override
	protected void onStartTag() throws XmlParseException,
			XmlPullParserException, IOException {
		lastChildSortOrder = 0;
		super.onStartTag();
	}
}
