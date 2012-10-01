package org.openforis.collect.designer.util;

/**
 * 
 * @author S. Ricci
 *
 */
public class Resources {
	
	public static final String PAGES_BASE_PATH = "";
	public static final String COMPONENTS_BASE_PATH = "/WEB-INF/view/zul/designer/";
	
	public enum Page {
	
		MAIN("designer.htm"),
		SURVEY_EDIT(PAGES_BASE_PATH + "editSurvey.htm");

		private String location;
	
		private Page(String location) {
			this.location = location;
		}
	
		public String getLocation() {
			return location;
		}
	}
	
	public enum Component {
		
		SELECT_LANGUAGE_POP_UP(
				COMPONENTS_BASE_PATH + "survey_edit/select_language_popup.zul"),
		TABSGROUP(
				COMPONENTS_BASE_PATH + "survey_edit/schema_layout/tabsgroup.zul"),
		TAB_LABEL_POPUP(
				COMPONENTS_BASE_PATH + "survey_edit/schema_layout/tab_label_popup.zul"),
		TABSGROUP_LIST_OF_NODES(
				COMPONENTS_BASE_PATH + "survey_edit/schema_layout/editablenodeslist.zul"),
		SRS_MANAGER_POP_UP(
				COMPONENTS_BASE_PATH + "survey_edit/srs_popup.zul"),
		VERSIONING_POPUP(
				COMPONENTS_BASE_PATH + "survey_edit/versioning_popup.zul"),
		CODE_LIST_ITEM_EDIT_POP_UP(
				COMPONENTS_BASE_PATH + "survey_edit/code_list_item_popup.zul"),
		CODE_LISTS_POPUP(
				COMPONENTS_BASE_PATH + "survey_edit/code_lists_popup.zul"), 
		UNITS_MANAGER_POP_UP(
				COMPONENTS_BASE_PATH + "survey_edit/units_popup.zul"), 
		NODE(
				COMPONENTS_BASE_PATH + "survey_edit/schema/node.zul");
		
		private String location;
		
		private Component(String location) {
			this.location = location;
		}
	
		public String getLocation() {
			return location;
		}
		
	}
}

