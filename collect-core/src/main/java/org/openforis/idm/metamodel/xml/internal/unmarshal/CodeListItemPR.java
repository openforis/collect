package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CODE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.COLOR;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DEPRECATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ITEM;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.QUALIFIABLE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SINCE;

import java.io.IOException;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
class CodeListItemPR extends IdmlPullReader {
	protected CodeListItem parentItem;
	protected CodeListItem item;
	
	public CodeListItemPR() {
		super(ITEM);
		addChildPullReaders(new CodePR(), new LabelPR(), new DescriptionPR());
	} 
	
	public CodeListItemPR(CodeListItem parentItem) {
		this();
		this.parentItem = parentItem;
	}

	@Override
	protected void onStartTag()
			throws XmlParseException, XmlPullParserException, IOException {
		int id = getIntegerAttribute(ID, true);
		Boolean q = getBooleanAttribute(QUALIFIABLE, false);
		String since = getAttribute(SINCE, false);
		String deprecated = getAttribute(DEPRECATED, false);
		String color = getAttribute(COLOR, false);
		int level = parentItem == null ? 1: parentItem.getLevel() + 1;
		createItem(id, level);
		item.setQualifiable(q==null ? false : q);
		item.setSinceVersionByName(since);
		item.setDeprecatedVersionByName(deprecated);
		item.setColor(color);
	}

	protected void createItem(int id, int level) {
		CodeList list = getCodeList();
		this.item = list.createItem(id, level);
	}
	
	private class CodePR extends TextPullReader {
		public CodePR() {
			super(CODE, 1);
		}
		
		@Override
		protected void processText(String text) {
			item.setCode(text);
		}
	}

	private class LabelPR extends LanguageSpecificTextPR {
		public LabelPR() {
			super(LABEL);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			item.addLabel(lst);
		}
	}

	private class DescriptionPR extends LanguageSpecificTextPR {
		public DescriptionPR() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			item.addDescription(lst);
		}
	}

	@Override
	protected XmlPullReader getChildPullReader() throws XmlParseException {
		XmlPullParser parser = getParser();
		String name = parser.getName();
		if ( name.equals(ITEM) ) {
			CodeListItemPR itemPR = createChildItemPR(item);
			itemPR.setParentReader(this);
			return itemPR;
		} else {
			return super.getChildPullReader();
		}
	}

	protected CodeListItemPR createChildItemPR(CodeListItem parentItem) {
		return new CodeListItemPR(parentItem);
	}
	
	@Override
	protected void onEndTag() throws XmlParseException {
		if ( parentItem == null ) {
			CodeList list = getCodeList();
			list.addItem(item);
		} else {
			parentItem.addChildItem(item);
		}
	}
	
	protected CodeList getCodeList() {
		XmlPullReader parentReader = getParentReader();
		while ( parentReader != null && ! (parentReader instanceof CodeListPR) ) {
			parentReader = parentReader.getParentReader();
		}
		if ( parentReader == null ) {
			throw new IllegalStateException("Invalid pull reader hieararchy");
		}
		return ((CodeListPR) parentReader).list;
	}
}