package org.openforis.collect.persistence.xml.internal.unmarshal;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.LABEL;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.NAME;

import java.io.IOException;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.persistence.xml.UIOptionsBinder;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author S. Ricci
 *
 */
class UITabPR extends UITabSetPRBase {
		
	public UITabPR(UIOptionsBinder binder) {
		super("tab", binder);
		
		addChildPullReaders(
			new LabelPR(),
			this
			);
	}
	
	@Override
	protected void onStartTag() throws XmlParseException, XmlPullParserException, IOException {
		String name = getAttribute(NAME, false);
		UIOptions uiOptions = parentTabSet.getUIOptions();
		tabSet = uiOptions.createTab(name);
	}
	
	@Override
	protected void onEndTag() throws XmlParseException {
		parentTabSet.addTab((UITab) tabSet);
	}
	
	private class LabelPR extends LanguageSpecificTextPR {

		public LabelPR() {
			super(LABEL);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			((UITab) tabSet).addLabel(lst);
		}
	}
}
