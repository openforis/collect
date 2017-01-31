package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CODE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CODE_LISTS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CODING_SCHEME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.COLOR;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DEPRECATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.HIERARCHY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ITEM;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ITEMS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LEVEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LIST;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LOOKUP;
import static org.openforis.idm.metamodel.xml.IdmlConstants.NAME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.QUALIFIABLE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SCOPE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SINCE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TYPE;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;

import liquibase.util.StringUtils;

/**
 * 
 * @author G. Miceli
 *
 */
class CodeListsXS extends VersionableSurveyObjectXS<CodeList, Survey> {

	CodeListsXS() {
		super(LIST);
		setListWrapperTag(CODE_LISTS);
		addChildMarshallers(
				new LabelXS(), 
				new DescriptionXS(), 
				new CodingSchemeXS(), 
				new HierarchyXS(),
				new ItemsXS());
	}
	
	@Override
	protected void marshalInstances(Survey survey) throws IOException {
		List<CodeList> lists = survey.getCodeLists();
		marshal(lists);
	}
	
	@Override
	protected void attributes(CodeList list) throws IOException {
		attribute(ID, list.getId());
		attribute(NAME, list.getName());
		attribute(LOOKUP, list.getLookupTable());
		super.attributes(list);
	}
	
	private class LabelXS extends LanguageSpecificTextXS<CodeList> {

		public LabelXS() {
			super(LABEL);
		}
		
		@Override
		protected void attributes(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
			CodeListLabel label = (CodeListLabel) txt;
			attribute(TYPE, label.getType().name().toLowerCase(Locale.ENGLISH));
			super.attributes(txt, includeLanguage);
		}
		
		@Override
		protected void marshalInstances(CodeList list) throws IOException {
			String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
			marshal(list.getLabels(), defaultLanguage);
		}
	}
	
	private class DescriptionXS extends LanguageSpecificTextXS<CodeList> {

		public DescriptionXS() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void marshalInstances(CodeList list) throws IOException {
			String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
			marshal(list.getDescriptions(), defaultLanguage);
		}
	}

	private class CodingSchemeXS extends XmlSerializerSupport<CodeScope, CodeList> {

		public CodingSchemeXS() {
			super(CODING_SCHEME);
		}
		
		@Override
		protected void marshalInstances(CodeList list) throws IOException {
			marshal(list.getCodeScope());
		}
		
		protected void attributes(CodeScope scope) throws IOException {
			attribute(SCOPE, scope == null ? null : scope.name().toLowerCase(Locale.ENGLISH));
		}
	}

	private class HierarchyXS extends XmlSerializerSupport<CodeListLevel, CodeList> {

		public HierarchyXS() {
			super(LEVEL);
			setListWrapperTag(HIERARCHY);
			addChildMarshallers(new LabelXS(), new DescriptionXS());
		}
		
		@Override
		protected void marshalInstances(CodeList list) throws IOException {
			marshal(list.getHierarchy());
		}

		@Override
		protected void attributes(CodeListLevel level) throws IOException {
			attribute(NAME, level.getName());
		}
		
		private class LabelXS extends LanguageSpecificTextXS<CodeListLevel> {

			public LabelXS() {
				super(LABEL);
			}
			
			@Override
			protected void marshalInstances(CodeListLevel level) throws IOException {
				marshal(level.getLabels());
			}
		}
		
		private class DescriptionXS extends LanguageSpecificTextXS<CodeListLevel> {

			public DescriptionXS() {
				super(DESCRIPTION);
			}
			
			@Override
			protected void marshalInstances(CodeListLevel level) throws IOException {
				marshal(level.getDescriptions());
			}
		}
		
	}

	private abstract class AbstractItemXS<P> extends XmlSerializerSupport<CodeListItem, P> {

		public AbstractItemXS() {
			super(ITEM);
			addChildMarshallers(new CodeXS(), new LabelXS(), new DescriptionXS());
		}

		@Override
		protected void attributes(CodeListItem item) throws IOException {
			attribute(ID, item.getId());
			if ( item.isQualifiable() ) {
				attribute(QUALIFIABLE, "true");
			}
			attribute(SINCE, item.getSinceVersionName());
			attribute(DEPRECATED, item.getDeprecatedVersionName());
			String color = StringUtils.trimToNull(item.getColor());
			if (color != null) {
				attribute(COLOR, color);
			}
		}
		
		private class CodeXS extends TextXS<CodeListItem> {

			public CodeXS() {
				super(CODE);
			}
			
			@Override
			protected void marshalInstances(CodeListItem item) throws IOException {
				marshal(item.getCode());
			}
		}
		
		private class LabelXS extends LanguageSpecificTextXS<CodeListItem> {

			public LabelXS() {
				super(LABEL);
			}
			
			@Override
			protected void marshalInstances(CodeListItem item) throws IOException {
				String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
				marshal(item.getLabels(), defaultLanguage);
			}

		}
		
		private class DescriptionXS extends LanguageSpecificTextXS<CodeListItem> {

			public DescriptionXS() {
				super(DESCRIPTION);
			}
			
			@Override
			protected void marshalInstances(CodeListItem item) throws IOException {
				String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
				marshal(item.getDescriptions(), defaultLanguage);
			}
		}
		
	}
	
	private class ItemsXS extends AbstractItemXS<CodeList> {
		
		public ItemsXS() {
			setListWrapperTag(ITEMS);
			addChildMarshallers(new ChildItemXS());
		}
		
		@Override
		protected void marshalInstances(CodeList list) throws IOException {
			List<? extends CodeListItem> items = getRootItems(list);
			if ( items != null ) {
				marshal(items);
			}
		}

		protected List<? extends CodeListItem> getRootItems(CodeList list) {
			SurveyMarshaller root = (SurveyMarshaller) getRootMarshaller();
			SurveyIdmlBinder binder = root.getBinder();
			SurveyContext context = binder.getSurveyContext();
			List<? extends CodeListItem> items = null;
			if ( list.isExternal() ) {
				if ( root.getParameters().isExternalCodeListsMarshalEnabled() ) {
					ExternalCodeListProvider externalCodeListProvider = context.getExternalCodeListProvider();
					items = externalCodeListProvider.getRootItems(list);
				}
			} else if ( list.isEmpty() ) {
				if ( root.getParameters().isPersistedCodeListsMarshalEnabled() ) {
					CodeListService codeListService = context.getCodeListService();
					items = codeListService.loadRootItems(list);
				}
			} else {
				items = list.getItems();
			}
			return items;
		}
	}

	private class ChildItemXS extends AbstractItemXS<CodeListItem> {
		
		public ChildItemXS() {
			addChildMarshallers(this);
		}
		
		@Override
		protected void marshalInstances(CodeListItem item) throws IOException {
			List<? extends CodeListItem> items = getChildItems(item);
			if ( items != null ) {
				marshal(items);
			}
		}

		protected List<? extends CodeListItem> getChildItems(CodeListItem item) {
			CodeList list = item.getCodeList();
			SurveyMarshaller root = (SurveyMarshaller) getRootMarshaller();
			SurveyIdmlBinder binder = root.getBinder();
			SurveyContext context = binder.getSurveyContext();
			List<? extends CodeListItem> items = null;
			if ( list.isExternal() ) {
				if ( root.getParameters().isExternalCodeListsMarshalEnabled() ) {
					ExternalCodeListProvider externalCodeListProvider = context.getExternalCodeListProvider();
					items = externalCodeListProvider.getChildItems((ExternalCodeListItem) item);
				}
			} else if ( list.isEmpty() ) {
				if ( root.getParameters().isPersistedCodeListsMarshalEnabled() ) {
					CodeListService codeListService = context.getCodeListService();
					items = codeListService.loadChildItems((PersistedCodeListItem) item);
				}
			} else {
				items = item.getChildItems();
			}
			return items;
		}
	}
}
