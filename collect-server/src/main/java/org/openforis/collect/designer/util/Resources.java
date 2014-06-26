package org.openforis.collect.designer.util;

/**
 * 
 * @author S. Ricci
 *
 */
public class Resources {
	
	public static final String PAGES_BASE_PATH = "/";
	public static final String COMPONENTS_BASE_PATH = "/WEB-INF/view/zul/designer/";
	
	public enum Page {
	
		INDEX("index.htm"),
		LOGOUT("logout.htm"),
		DESIGNER("designer.htm"),
		SURVEY_EDIT("editSurvey.htm"),
		COLLECT_SWF("collect.swf"),
		PREVIEW_PATH("index.htm");
		
		private String location;
	
		private Page(String location) {
			this.location = PAGES_BASE_PATH + location;
		}
	
		public String getLocation() {
			return location;
		}
	}
	
	public enum Component {
		SELECT_LANGUAGE_POP_UP("survey_edit/select_language_popup.zul"),
		PREVIEW_PREFERENCES_POP_UP("survey_edit/preview_preferences_popup.zul"),
		TABSGROUP("survey_edit/schema_layout/tabsgroup.zul"),
		TAB_LABEL_POPUP("survey_edit/schema_layout/tab_label_popup.zul"),
		TABSGROUP_LIST_OF_NODES("survey_edit/schema_layout/editablenodeslist.zul"),
		SRS_MANAGER_POP_UP("survey_edit/srs_popup.zul"),
		VERSIONING_POPUP("survey_edit/versioning_popup.zul"),
		CODE_LIST_IMPORT_POPUP("survey_edit/code_list_import_popup.zul"), 
		CODE_LIST_ITEM_EDIT_POP_UP("survey_edit/code_list_item_popup.zul"),
		CODE_LISTS_POPUP("survey_edit/code_lists_popup.zul"),
		UNITS_MANAGER_POP_UP("survey_edit/units_popup.zul"), 
		TAB("survey_edit/schema/tab.zul"),
		ENTITY("survey_edit/schema/entity.zul"),
		ATTRIBUTE("survey_edit/schema/attribute_{0}.zul"),
		ATTRIBUTE_POPUP("survey_edit/schema/attribute_popup.zul"), 
		CHECK_POPUP("survey_edit/schema/check/popup.zul"), 
		SURVEY_IMPORT_POPUP("survey_import_popup.zul"),
		ATTRIBUTE_DEFAULT_POPUP("survey_edit/schema/attribute_default_popup.zul"), 
		FORMULA_POPUP("survey_edit/schema/attribute/calculated_formula_popup.zul"), 
		PRECISION_POPUP("survey_edit/schema/precision_popup.zul"),
		SURVEY_VALIDATION_RESULTS_POPUP("survey_validation_results_popup.zul"),
		CONFIRM_SURVEY_ERRORS_POPUP("component/confirm_survey_errors_popup.zul"),
		PROCESS_STATUS_POPUP("component/process_status_popup.zul"),
		JOB_STATUS_POPUP("component/job_status_popup.zul"),
		SELECT_TEMPLATE_POPUP("component/select_template_popup.zul"), 
		SURVEY_EXPORT_PARAMETERS_POPUP("component/survey_export_parameters_popup.zul"),
		NODE_EDIT_POPUP("survey_edit/schema/node_popup.zul"),
		SCHEMA_TREE_POPUP("survey_edit/schema/schema_tree_popup.zul");
		
		private String location;
		
		private Component(String location) {
			this.location = COMPONENTS_BASE_PATH + location;
		}
	
		public String getLocation() {
			return location;
		}
		
	}
}

