package org.openforis.collect.persistence.xml;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.model.ui.UIOptions;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.NodeDefinitionPR.DescriptionPR;
import org.openforis.idm.metamodel.xml.IdmlPullReader;
import org.openforis.idm.metamodel.xml.LanguageSpecificTextPR;
import org.openforis.idm.metamodel.xml.SurveyBinder;
import org.openforis.idm.metamodel.xml.UnitsPR;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.openforis.idm.metamodel.xml.XmlPullReader;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsPullReader extends XmlPullReader {

	private static final String ROOT_TAG = "flex";
	private UIOptionsBinder binder;
	private UIOptions uiOptions;

	public UIOptionsPullReader() {
		super(UIOptions.UI_NAMESPACE_URI, ROOT_TAG);
		
		if ( binder == null ) {
			throw new NullPointerException("binder");
		}
		addChildPullReaders(
			new TabDefinitionPR()
			);
	}

	public UIOptions getUIOptions() {
		return uiOptions;
	}

	protected UIOptionsPullReader(String namespace, String tagName,
			Integer maxCount) {
		super(namespace, tagName, maxCount);
	}

	protected UIOptionsPullReader(String namespace, String tagName) {
		super(namespace, tagName);
	}

	private class TabDefinitionPR extends XmlPullReader {
		
		private UITabDefinition tabDefinition;

		public TabDefinitionPR() {
			super(UIOptions.UI_NAMESPACE_URI, "tabDefinition");
			
			addChildPullReaders(new TabPR());
		}
		
		@Override
		protected void onStartTag() throws XmlParseException,
				XmlPullParserException, IOException {
			super.onStartTag();
			this.tabDefinition = new UITabDefinition();
			tabDefinition.setName(getAttribute("name", true));
		}
		
		private class TabPR extends XmlPullReader {
			
			public TabDefinitionPR() {
				super(UIOptions.UI_NAMESPACE_URI, "tab");
				
				addChildPullReaders(new TabPR());
			}
			
			
		}
	}
}
