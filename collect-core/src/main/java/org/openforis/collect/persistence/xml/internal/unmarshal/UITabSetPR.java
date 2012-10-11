/**
 * 
 */
package org.openforis.collect.persistence.xml.internal.unmarshal;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.NAME;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.TAB_SET;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.openforis.idm.metamodel.xml.internal.marshal.XmlPullReader;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author S. Ricci
 *
 */
public class UITabSetPR extends UITabSetPRBase {

	public UITabSetPR() {
		super(TAB_SET);
		
		addChildPullReaders(
			new UITabPR()
		);
	}
	
	@Override
	protected void onStartTag() throws XmlParseException,
			XmlPullParserException, IOException {
		tabSet = new UITabSet();
		tabSet.setName(getAttribute(NAME, true));
		
		setParentSetInChildren(tabSet);
	}
	
	private void setParentSetInChildren(UITabSet tabSet) {
		List<XmlPullReader> childprs = getChildPullReaders();
		for (XmlPullReader pr : childprs) {
			if ( pr instanceof UITabSetPRBase ) {
				((UITabSetPRBase) pr).parentTabSet = tabSet;
			}
		}
	}
	
	

}

