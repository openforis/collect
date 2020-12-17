package org.openforis.collect.designer.util;

/**
 * 
 * @author S. Ricci
 *
 */
public class Resources {
	
	public static final String PAGES_BASE_PATH = "/";
	public static final String COMPONENTS_BASE_PATH = "/zk/surveydesigner/";
	
	public enum Page {
	
		INDEX("/"),
		DESIGNER("designer.htm"),
		SURVEY_EDIT(COMPONENTS_BASE_PATH + "survey_edit.zul"),
		COLLECT_SWF("collect.swf"),
		PREVIEW_PATH("#/surveypreview/");
		
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
		SURVEY_FILE_POPUP("survey_edit/survey_file.zul"), 
		COLLECT_EARTH_PREVIEW_POPUP("survey_edit/collect_earth_preview_popup.zul"),
		PREVIEW_PREFERENCES_POP_UP("survey_edit/preview_preferences_popup.zul"),
		PREVIEW_POP_UP("survey_edit/preview_popup.zul"),
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
		ATTRIBUTE_DEFAULT_POPUP("survey_edit/schema/attribute_default_popup.zul"), 
		FORMULA_POPUP("survey_edit/schema/attribute/calculated_formula_popup.zul"), 
		PRECISION_POPUP("survey_edit/schema/precision_popup.zul"),
		SURVEY_VALIDATION_RESULTS_POPUP("survey_validation_results_popup.zul"),
		REFERENCE_DATA_IMPORT_ERRORS_POPUP("survey_edit/reference_data/reference_data_import_errors_popup.zul"),
		CONFIRM_SURVEY_ERRORS_POPUP("component/confirm_survey_errors_popup.zul"),
		PROCESS_STATUS_POPUP("component/process_status_popup.zul"),
		JOB_STATUS_POPUP("component/job_status_popup.zul"),
		SURVEY_EXPORT_PARAMETERS_POPUP("component/survey_export_parameters_popup.zul"),
		DATA_EXPORT_ERRORS_POPUP("component/data_export_errors_popup.zul"),
		SURVEY_CLONE_PARAMETERS_POPUP("component/survey_clone_parameters_popup.zul"),
		NODE_EDIT_POPUP("survey_edit/schema/node_popup.zul"),
		SCHEMA_TREE_POPUP("survey_edit/schema/schema_tree_popup.zul"),
		SCHEMA_ATTRIBUTES_IMPORT_POP_UP("survey_edit/schema_attributes_import_popup.zul"),
		ATTRIBUTE_CONVERSION_PARAMETERS_POPUP("survey_edit/schema/attribute_conversion_parameters_popup.zul"),
		SAMPLING_POINT_DATA_IMPORT_POPUP("survey_edit/sampling_point_data/sampling_point_data_import_popup.zul"),
		TAXONOMY_IMPORT_POPUP("survey_edit/taxonomy/taxonomy_import_popup.zul");
		
		private String location;
		
		private Component(String location) {
			this.location = COMPONENTS_BASE_PATH + location;
		}
	
		public String getLocation() {
			return location;
		}
		
	}
}

