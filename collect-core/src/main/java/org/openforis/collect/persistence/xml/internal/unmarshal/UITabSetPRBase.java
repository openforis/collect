package org.openforis.collect.persistence.xml.internal.unmarshal;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;

import java.io.IOException;

import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.persistence.xml.UIOptionsBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.openforis.idm.metamodel.xml.internal.unmarshal.XmlPullReader;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author S. Ricci
 *
 */
abstract class UITabSetPRBase extends UIElementPullReader {

	protected UIOptionsBinder binder;
	protected UITabSet parentTabSet;
	protected UITabSet tabSet;

	public UITabSetPRBase(String tabName, UIOptionsBinder binder) {
		super(UI_NAMESPACE_URI, tabName);
		this.binder = binder;
	}

	@Override
	protected final void handleChildTag(XmlPullReader childPR)
			throws XmlPullParserException, IOException, XmlParseException {

		if ( childPR instanceof UITabPR ) {
			UITabPR tabSetPR = (UITabPR) childPR;
			// Store child state in case reused recursively
			UITabSet tmpParentTabSet = tabSetPR.parentTabSet;
			UITabSet tmpTabSet = tabSetPR.tabSet;
			tabSetPR.parentTabSet = tabSet;
			tabSetPR.tabSet = null;
			super.handleChildTag(childPR);
			tabSetPR.parentTabSet = tmpParentTabSet;
			tabSetPR.tabSet = tmpTabSet;
		} else {
			super.handleChildTag(childPR);
		}
	}

	public UITabSet getTabSet() {
		return tabSet;
	}

}
