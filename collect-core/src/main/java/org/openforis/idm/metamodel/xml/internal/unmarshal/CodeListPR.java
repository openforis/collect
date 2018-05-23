package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CODING_SCHEME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DEPRECATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.HIERARCHY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LEVEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LIST;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LOOKUP;
import static org.openforis.idm.metamodel.xml.IdmlConstants.NAME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SCOPE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SINCE;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.CodeListLabel.Type;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

class CodeListPR extends IdmlPullReader {
	
	CodeList list;
	private boolean includeChildItems;
	
	public CodeListPR() {
		this(false);
	}
	
	public CodeListPR(boolean includeChildItems) {
		super(LIST);
		this.includeChildItems = includeChildItems;
		addChildPullReaders(new LabelPR(), new DescriptionPR(), new CodingSchemePR(), new HierarchyPR(), createCodeListItemsPR());
	}

	@Override
	protected void onStartTag() throws XmlParseException {
		int id = getIntegerAttribute(ID, true);
		String name = getAttribute(NAME, false);
		String lookupTable = getAttribute(LOOKUP, false);
		String since = getAttribute(SINCE, false);
		String deprecated = getAttribute(DEPRECATED, false);
		Survey survey = getSurvey();
		list = survey.createCodeList(id);
		list.setName(name);
		list.setLookupTable(lookupTable);
		list.setSinceVersionByName(since);
		list.setDeprecatedVersionByName(deprecated);
	}

	protected CodeListItemsPR createCodeListItemsPR() {
		return new CodeListItemsPR(includeChildItems);
	}
	
	private class CodingSchemePR extends IdmlPullReader {

		public CodingSchemePR() {
			super(CODING_SCHEME, 1);
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			String scopeStr = getAttribute(SCOPE, true);
			try {
				CodeScope scope = CodeList.CodeScope.valueOf(scopeStr.toUpperCase());
				list.setCodeScope(scope);
			} catch ( IllegalArgumentException ex ) {
				throw new XmlParseException(getParser(), "invalid scope "+scopeStr);
			}
		}
	}
	
	private class HierarchyPR extends IdmlPullReader {
		public HierarchyPR() {
			super(HIERARCHY, 1);
			addChildPullReaders(new LevelPR());
		}
		
		private class LevelPR extends IdmlPullReader {
			private CodeListLevel level;
			
			public LevelPR() {
				super(LEVEL);
				addChildPullReaders(new LabelPR(), new DescriptionPR());						
			}
			
			@Override
			protected void onStartTag()
					throws XmlParseException, XmlPullParserException, IOException {
				this.level = new CodeListLevel();
				String name = getAttribute(NAME, true);
				level.setName(name);
			}

			private class LabelPR extends LanguageSpecificTextPR {

				public LabelPR() {
					super(LABEL);
				}
				
				@Override
				protected void processText(LanguageSpecificText lst) {
					level.addLabel(lst);
				}
			}

			private class DescriptionPR extends LanguageSpecificTextPR {
				public DescriptionPR() {
					super(DESCRIPTION);
				}
				
				@Override
				protected void processText(LanguageSpecificText lst) {
					level.addDescription(lst);
				}
			}
			
			@Override
			protected void onEndTag() throws XmlParseException {
				list.addLevel(level);
				this.level = null;
			}
		}
	}			
	
	private class LabelPR extends LanguageSpecificTextPR {
		public LabelPR() {
			super(LABEL, true);
		}
		
		@Override
		protected void processText(String lang, String typeStr, String text) throws XmlParseException {
			CodeListLabel label = new CodeListLabel(parseType(typeStr), lang, text);
			list.addLabel(label);
		}

		private Type parseType(String typeStr) throws XmlParseException {
			if (StringUtils.isBlank(typeStr)) {
				return Type.ITEM;
			} else {
				try {
					return CodeListLabel.Type.valueOf(typeStr.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new XmlParseException(getParser(), "invalid type for code list label: " + typeStr, e);
				}
			}
		}
	}

	private class DescriptionPR extends LanguageSpecificTextPR {
		public DescriptionPR() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			list.addDescription(lst);
		}
	}

	@Override
	public void onEndTag() throws XmlParseException {
		addCodeListToSurvey();
		this.list = null;
	}

	protected void addCodeListToSurvey() {
		Survey survey = list.getSurvey();
		survey.addCodeList(list);
	}
}