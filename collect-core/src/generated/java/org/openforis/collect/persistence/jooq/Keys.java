/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq;


import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import org.openforis.collect.persistence.jooq.tables.OfcCodeList;
import org.openforis.collect.persistence.jooq.tables.OfcConfig;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChain;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChainSteps;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingReport;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStep;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStepValue;
import org.openforis.collect.persistence.jooq.tables.OfcDataQuery;
import org.openforis.collect.persistence.jooq.tables.OfcDataQueryGroup;
import org.openforis.collect.persistence.jooq.tables.OfcDataQueryGroupQuery;
import org.openforis.collect.persistence.jooq.tables.OfcDataQueryType;
import org.openforis.collect.persistence.jooq.tables.OfcDataReport;
import org.openforis.collect.persistence.jooq.tables.OfcDataReportItem;
import org.openforis.collect.persistence.jooq.tables.OfcImagery;
import org.openforis.collect.persistence.jooq.tables.OfcLogo;
import org.openforis.collect.persistence.jooq.tables.OfcMessage;
import org.openforis.collect.persistence.jooq.tables.OfcRecord;
import org.openforis.collect.persistence.jooq.tables.OfcRecordData;
import org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign;
import org.openforis.collect.persistence.jooq.tables.OfcSurvey;
import org.openforis.collect.persistence.jooq.tables.OfcSurveyFile;
import org.openforis.collect.persistence.jooq.tables.OfcTaxon;
import org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName;
import org.openforis.collect.persistence.jooq.tables.OfcTaxonomy;
import org.openforis.collect.persistence.jooq.tables.OfcUser;
import org.openforis.collect.persistence.jooq.tables.OfcUserRole;
import org.openforis.collect.persistence.jooq.tables.OfcUserUsergroup;
import org.openforis.collect.persistence.jooq.tables.OfcUsergroup;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingChainRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingChainStepsRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingReportRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingStepRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingStepValueRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryGroupQueryRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryGroupRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryTypeRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataReportItemRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataReportRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcImageryRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcMessageRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcRecordDataRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcSamplingDesignRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcSurveyFileRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcSurveyRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonVernacularNameRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonomyRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRoleRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserUsergroupRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUsergroupRecord;


/**
 * A class modelling foreign key relationships between tables of the <code>collect</code> 
 * schema
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------

	public static final Identity<OfcImageryRecord, Integer> IDENTITY_OFC_IMAGERY = Identities0.IDENTITY_OFC_IMAGERY;
	public static final Identity<OfcMessageRecord, Integer> IDENTITY_OFC_MESSAGE = Identities0.IDENTITY_OFC_MESSAGE;

	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final UniqueKey<OfcCodeListRecord> OFC_CODE_LIST_PKEY = UniqueKeys0.OFC_CODE_LIST_PKEY;
	public static final UniqueKey<OfcConfigRecord> OFC_CONFIG_PKEY = UniqueKeys0.OFC_CONFIG_PKEY;
	public static final UniqueKey<OfcDataCleansingChainRecord> OFC_DATA_CLEANSING_CHAIN_PKEY = UniqueKeys0.OFC_DATA_CLEANSING_CHAIN_PKEY;
	public static final UniqueKey<OfcDataCleansingReportRecord> OFC_DATA_CLEANSING_REPORT_PKEY = UniqueKeys0.OFC_DATA_CLEANSING_REPORT_PKEY;
	public static final UniqueKey<OfcDataCleansingStepRecord> OFC_DATA_CLEANSING_STEP_PKEY = UniqueKeys0.OFC_DATA_CLEANSING_STEP_PKEY;
	public static final UniqueKey<OfcDataQueryRecord> OFC_DATA_QUERY_PKEY = UniqueKeys0.OFC_DATA_QUERY_PKEY;
	public static final UniqueKey<OfcDataQueryGroupRecord> OFC_DATA_QUERY_GROUP_PKEY = UniqueKeys0.OFC_DATA_QUERY_GROUP_PKEY;
	public static final UniqueKey<OfcDataQueryGroupQueryRecord> OFC_DATA_QUERY_GROUP_QUERY_PKEY = UniqueKeys0.OFC_DATA_QUERY_GROUP_QUERY_PKEY;
	public static final UniqueKey<OfcDataQueryTypeRecord> OFC_DATA_QUERY_TYPE_PKEY = UniqueKeys0.OFC_DATA_QUERY_TYPE_PKEY;
	public static final UniqueKey<OfcDataReportRecord> OFC_DATA_REPORT_PKEY = UniqueKeys0.OFC_DATA_REPORT_PKEY;
	public static final UniqueKey<OfcDataReportItemRecord> OFC_DATA_REPORT_ITEM_PKEY = UniqueKeys0.OFC_DATA_REPORT_ITEM_PKEY;
	public static final UniqueKey<OfcImageryRecord> OFC_IMAGERY_PKEY = UniqueKeys0.OFC_IMAGERY_PKEY;
	public static final UniqueKey<OfcLogoRecord> OFC_LOGO_PKEY = UniqueKeys0.OFC_LOGO_PKEY;
	public static final UniqueKey<OfcMessageRecord> PK_OFC_MESSAGE = UniqueKeys0.PK_OFC_MESSAGE;
	public static final UniqueKey<OfcRecordRecord> OFC_RECORD_PKEY = UniqueKeys0.OFC_RECORD_PKEY;
	public static final UniqueKey<OfcRecordDataRecord> OFC_RECORD_DATA_PKEY = UniqueKeys0.OFC_RECORD_DATA_PKEY;
	public static final UniqueKey<OfcSamplingDesignRecord> PK_OFC_SAMPLING_DESIGN = UniqueKeys0.PK_OFC_SAMPLING_DESIGN;
	public static final UniqueKey<OfcSurveyRecord> OFC_SURVEY_PKEY = UniqueKeys0.OFC_SURVEY_PKEY;
	public static final UniqueKey<OfcSurveyRecord> OFC_SURVEY_NAME_KEY = UniqueKeys0.OFC_SURVEY_NAME_KEY;
	public static final UniqueKey<OfcSurveyRecord> OFC_SURVEY_URI_KEY = UniqueKeys0.OFC_SURVEY_URI_KEY;
	public static final UniqueKey<OfcSurveyFileRecord> OFC_SURVEY_FILE_PKEY = UniqueKeys0.OFC_SURVEY_FILE_PKEY;
	public static final UniqueKey<OfcTaxonRecord> OFC_TAXON_PKEY = UniqueKeys0.OFC_TAXON_PKEY;
	public static final UniqueKey<OfcTaxonRecord> OFC_TAXON_ID_KEY = UniqueKeys0.OFC_TAXON_ID_KEY;
	public static final UniqueKey<OfcTaxonVernacularNameRecord> OFC_TAXON_VERNACULAR_NAME_PKEY = UniqueKeys0.OFC_TAXON_VERNACULAR_NAME_PKEY;
	public static final UniqueKey<OfcTaxonomyRecord> OFC_TAXONOMY_PKEY = UniqueKeys0.OFC_TAXONOMY_PKEY;
	public static final UniqueKey<OfcTaxonomyRecord> OFC_TAXONOMY_NAME_KEY = UniqueKeys0.OFC_TAXONOMY_NAME_KEY;
	public static final UniqueKey<OfcUserRecord> OFC_USER_PKEY = UniqueKeys0.OFC_USER_PKEY;
	public static final UniqueKey<OfcUserRecord> OFC_USER_USERNAME_KEY = UniqueKeys0.OFC_USER_USERNAME_KEY;
	public static final UniqueKey<OfcUserRoleRecord> OFC_USER_ROLE_PKEY = UniqueKeys0.OFC_USER_ROLE_PKEY;
	public static final UniqueKey<OfcUserUsergroupRecord> OFC_USER_USERGROUP_PKEY = UniqueKeys0.OFC_USER_USERGROUP_PKEY;
	public static final UniqueKey<OfcUsergroupRecord> OFC_USERGROUP_PKEY = UniqueKeys0.OFC_USERGROUP_PKEY;
	public static final UniqueKey<OfcUsergroupRecord> OFC_USERGROUP_NAME_KEY = UniqueKeys0.OFC_USERGROUP_NAME_KEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final ForeignKey<OfcCodeListRecord, OfcSurveyRecord> OFC_CODE_LIST__OFC_CODE_LIST_SURVEY_FKEY = ForeignKeys0.OFC_CODE_LIST__OFC_CODE_LIST_SURVEY_FKEY;
	public static final ForeignKey<OfcCodeListRecord, OfcCodeListRecord> OFC_CODE_LIST__OFC_CODE_LIST_PARENT_FKEY = ForeignKeys0.OFC_CODE_LIST__OFC_CODE_LIST_PARENT_FKEY;
	public static final ForeignKey<OfcDataCleansingChainRecord, OfcSurveyRecord> OFC_DATA_CLEANSING_CHAIN__OFC_DATA_CLEANSING_CHAIN_SURVEY_FKEY = ForeignKeys0.OFC_DATA_CLEANSING_CHAIN__OFC_DATA_CLEANSING_CHAIN_SURVEY_FKEY;
	public static final ForeignKey<OfcDataCleansingChainStepsRecord, OfcDataCleansingChainRecord> OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_CHAIN_FKEY = ForeignKeys0.OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_CHAIN_FKEY;
	public static final ForeignKey<OfcDataCleansingChainStepsRecord, OfcDataCleansingStepRecord> OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_STEP_FKEY = ForeignKeys0.OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_STEP_FKEY;
	public static final ForeignKey<OfcDataCleansingReportRecord, OfcDataCleansingChainRecord> OFC_DATA_CLEANSING_REPORT__OFC_DATA_CLEANSING_CHAIN_FKEY = ForeignKeys0.OFC_DATA_CLEANSING_REPORT__OFC_DATA_CLEANSING_CHAIN_FKEY;
	public static final ForeignKey<OfcDataCleansingStepRecord, OfcDataQueryRecord> OFC_DATA_CLEANSING_STEP__OFC_DATA_CLEANSING_STEP_QUERY_FKEY = ForeignKeys0.OFC_DATA_CLEANSING_STEP__OFC_DATA_CLEANSING_STEP_QUERY_FKEY;
	public static final ForeignKey<OfcDataCleansingStepValueRecord, OfcDataCleansingStepRecord> OFC_DATA_CLEANSING_STEP_VALUE__OFC_DATA_CLEANSING_STEP_VALUE_FKEY = ForeignKeys0.OFC_DATA_CLEANSING_STEP_VALUE__OFC_DATA_CLEANSING_STEP_VALUE_FKEY;
	public static final ForeignKey<OfcDataQueryRecord, OfcSurveyRecord> OFC_DATA_QUERY__OFC_DATA_QUERY_SURVEY_FKEY = ForeignKeys0.OFC_DATA_QUERY__OFC_DATA_QUERY_SURVEY_FKEY;
	public static final ForeignKey<OfcDataQueryRecord, OfcDataQueryTypeRecord> OFC_DATA_QUERY__OFC_DATA_QUERY_TYPE_FKEY = ForeignKeys0.OFC_DATA_QUERY__OFC_DATA_QUERY_TYPE_FKEY;
	public static final ForeignKey<OfcDataQueryGroupRecord, OfcSurveyRecord> OFC_DATA_QUERY_GROUP__OFC_DATA_QUERY_GROUP_SURVEY_FKEY = ForeignKeys0.OFC_DATA_QUERY_GROUP__OFC_DATA_QUERY_GROUP_SURVEY_FKEY;
	public static final ForeignKey<OfcDataQueryGroupQueryRecord, OfcDataQueryGroupRecord> OFC_DATA_QUERY_GROUP_QUERY__OFC_DATA_QUERY_GROUP_GROUP_FKEY = ForeignKeys0.OFC_DATA_QUERY_GROUP_QUERY__OFC_DATA_QUERY_GROUP_GROUP_FKEY;
	public static final ForeignKey<OfcDataQueryGroupQueryRecord, OfcDataQueryRecord> OFC_DATA_QUERY_GROUP_QUERY__OFC_DATA_QUERY_GROUP_QUERY_FKEY = ForeignKeys0.OFC_DATA_QUERY_GROUP_QUERY__OFC_DATA_QUERY_GROUP_QUERY_FKEY;
	public static final ForeignKey<OfcDataQueryTypeRecord, OfcSurveyRecord> OFC_DATA_QUERY_TYPE__OFC_DATA_QUERY_TYPE_SURVEY_FKEY = ForeignKeys0.OFC_DATA_QUERY_TYPE__OFC_DATA_QUERY_TYPE_SURVEY_FKEY;
	public static final ForeignKey<OfcDataReportRecord, OfcDataQueryGroupRecord> OFC_DATA_REPORT__OFC_DATA_REPORT_QUERY_GROUP_FKEY = ForeignKeys0.OFC_DATA_REPORT__OFC_DATA_REPORT_QUERY_GROUP_FKEY;
	public static final ForeignKey<OfcDataReportItemRecord, OfcDataReportRecord> OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_REPORT_FKEY = ForeignKeys0.OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_REPORT_FKEY;
	public static final ForeignKey<OfcDataReportItemRecord, OfcDataQueryRecord> OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_QUERY_FKEY = ForeignKeys0.OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_QUERY_FKEY;
	public static final ForeignKey<OfcDataReportItemRecord, OfcRecordRecord> OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_RECORD_FKEY = ForeignKeys0.OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_RECORD_FKEY;
	public static final ForeignKey<OfcRecordRecord, OfcSurveyRecord> OFC_RECORD__OFC_RECORD_SURVEY_FKEY = ForeignKeys0.OFC_RECORD__OFC_RECORD_SURVEY_FKEY;
	public static final ForeignKey<OfcRecordDataRecord, OfcRecordRecord> OFC_RECORD_DATA__OFC_RECORD_DATA_RECORD_FKEY = ForeignKeys0.OFC_RECORD_DATA__OFC_RECORD_DATA_RECORD_FKEY;
	public static final ForeignKey<OfcSamplingDesignRecord, OfcSurveyRecord> OFC_SAMPLING_DESIGN__OFC_SAMPLING_DESIGN_SURVEY_FKEY = ForeignKeys0.OFC_SAMPLING_DESIGN__OFC_SAMPLING_DESIGN_SURVEY_FKEY;
	public static final ForeignKey<OfcSurveyRecord, OfcUsergroupRecord> OFC_SURVEY__OFC_SURVEY_USERGROUP_FKEY = ForeignKeys0.OFC_SURVEY__OFC_SURVEY_USERGROUP_FKEY;
	public static final ForeignKey<OfcSurveyFileRecord, OfcSurveyRecord> OFC_SURVEY_FILE__OFC_SURVEY_FILE_SURVEY_FKEY = ForeignKeys0.OFC_SURVEY_FILE__OFC_SURVEY_FILE_SURVEY_FKEY;
	public static final ForeignKey<OfcTaxonRecord, OfcTaxonomyRecord> OFC_TAXON__OFC_TAXON_TAXONOMY_FKEY = ForeignKeys0.OFC_TAXON__OFC_TAXON_TAXONOMY_FKEY;
	public static final ForeignKey<OfcTaxonRecord, OfcTaxonRecord> OFC_TAXON__OFC_TAXON_PARENT_FKEY = ForeignKeys0.OFC_TAXON__OFC_TAXON_PARENT_FKEY;
	public static final ForeignKey<OfcTaxonVernacularNameRecord, OfcTaxonRecord> OFC_TAXON_VERNACULAR_NAME__OFC_TAXON_VERNACULAR_NAME_TAXON_FKEY = ForeignKeys0.OFC_TAXON_VERNACULAR_NAME__OFC_TAXON_VERNACULAR_NAME_TAXON_FKEY;
	public static final ForeignKey<OfcTaxonomyRecord, OfcSurveyRecord> OFC_TAXONOMY__OFC_TAXONOMY_SURVEY_FKEY = ForeignKeys0.OFC_TAXONOMY__OFC_TAXONOMY_SURVEY_FKEY;
	public static final ForeignKey<OfcUserRoleRecord, OfcUserRecord> OFC_USER_ROLE__OFC_USER_USER_ROLE_FKEY = ForeignKeys0.OFC_USER_ROLE__OFC_USER_USER_ROLE_FKEY;
	public static final ForeignKey<OfcUserUsergroupRecord, OfcUserRecord> OFC_USER_USERGROUP__OFC_USER_USERGROUP_USER_FKEY = ForeignKeys0.OFC_USER_USERGROUP__OFC_USER_USERGROUP_USER_FKEY;
	public static final ForeignKey<OfcUserUsergroupRecord, OfcUsergroupRecord> OFC_USER_USERGROUP__OFC_USER_USERGROUP_GROUP_FKEY = ForeignKeys0.OFC_USER_USERGROUP__OFC_USER_USERGROUP_GROUP_FKEY;
	public static final ForeignKey<OfcUsergroupRecord, OfcUsergroupRecord> OFC_USERGROUP__OFC_USERGROUP_PARENT_FKEY = ForeignKeys0.OFC_USERGROUP__OFC_USERGROUP_PARENT_FKEY;
	public static final ForeignKey<OfcUsergroupRecord, OfcUserRecord> OFC_USERGROUP__OFC_USERGROUP_CREATED_BY_FKEY = ForeignKeys0.OFC_USERGROUP__OFC_USERGROUP_CREATED_BY_FKEY;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Identities0 extends AbstractKeys {
		public static Identity<OfcImageryRecord, Integer> IDENTITY_OFC_IMAGERY = createIdentity(OfcImagery.OFC_IMAGERY, OfcImagery.OFC_IMAGERY.ID);
		public static Identity<OfcMessageRecord, Integer> IDENTITY_OFC_MESSAGE = createIdentity(OfcMessage.OFC_MESSAGE, OfcMessage.OFC_MESSAGE.SEQUENCE_NO);
	}

	private static class UniqueKeys0 extends AbstractKeys {
		public static final UniqueKey<OfcCodeListRecord> OFC_CODE_LIST_PKEY = createUniqueKey(OfcCodeList.OFC_CODE_LIST, OfcCodeList.OFC_CODE_LIST.ID);
		public static final UniqueKey<OfcConfigRecord> OFC_CONFIG_PKEY = createUniqueKey(OfcConfig.OFC_CONFIG, OfcConfig.OFC_CONFIG.NAME);
		public static final UniqueKey<OfcDataCleansingChainRecord> OFC_DATA_CLEANSING_CHAIN_PKEY = createUniqueKey(OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN, OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.ID);
		public static final UniqueKey<OfcDataCleansingReportRecord> OFC_DATA_CLEANSING_REPORT_PKEY = createUniqueKey(OfcDataCleansingReport.OFC_DATA_CLEANSING_REPORT, OfcDataCleansingReport.OFC_DATA_CLEANSING_REPORT.ID);
		public static final UniqueKey<OfcDataCleansingStepRecord> OFC_DATA_CLEANSING_STEP_PKEY = createUniqueKey(OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP, OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.ID);
		public static final UniqueKey<OfcDataQueryRecord> OFC_DATA_QUERY_PKEY = createUniqueKey(OfcDataQuery.OFC_DATA_QUERY, OfcDataQuery.OFC_DATA_QUERY.ID);
		public static final UniqueKey<OfcDataQueryGroupRecord> OFC_DATA_QUERY_GROUP_PKEY = createUniqueKey(OfcDataQueryGroup.OFC_DATA_QUERY_GROUP, OfcDataQueryGroup.OFC_DATA_QUERY_GROUP.ID);
		public static final UniqueKey<OfcDataQueryGroupQueryRecord> OFC_DATA_QUERY_GROUP_QUERY_PKEY = createUniqueKey(OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY, OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID, OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY.QUERY_ID);
		public static final UniqueKey<OfcDataQueryTypeRecord> OFC_DATA_QUERY_TYPE_PKEY = createUniqueKey(OfcDataQueryType.OFC_DATA_QUERY_TYPE, OfcDataQueryType.OFC_DATA_QUERY_TYPE.ID);
		public static final UniqueKey<OfcDataReportRecord> OFC_DATA_REPORT_PKEY = createUniqueKey(OfcDataReport.OFC_DATA_REPORT, OfcDataReport.OFC_DATA_REPORT.ID);
		public static final UniqueKey<OfcDataReportItemRecord> OFC_DATA_REPORT_ITEM_PKEY = createUniqueKey(OfcDataReportItem.OFC_DATA_REPORT_ITEM, OfcDataReportItem.OFC_DATA_REPORT_ITEM.ID);
		public static final UniqueKey<OfcImageryRecord> OFC_IMAGERY_PKEY = createUniqueKey(OfcImagery.OFC_IMAGERY, OfcImagery.OFC_IMAGERY.ID);
		public static final UniqueKey<OfcLogoRecord> OFC_LOGO_PKEY = createUniqueKey(OfcLogo.OFC_LOGO, OfcLogo.OFC_LOGO.POS);
		public static final UniqueKey<OfcMessageRecord> PK_OFC_MESSAGE = createUniqueKey(OfcMessage.OFC_MESSAGE, OfcMessage.OFC_MESSAGE.SEQUENCE_NO);
		public static final UniqueKey<OfcRecordRecord> OFC_RECORD_PKEY = createUniqueKey(OfcRecord.OFC_RECORD, OfcRecord.OFC_RECORD.ID);
		public static final UniqueKey<OfcRecordDataRecord> OFC_RECORD_DATA_PKEY = createUniqueKey(OfcRecordData.OFC_RECORD_DATA, OfcRecordData.OFC_RECORD_DATA.RECORD_ID, OfcRecordData.OFC_RECORD_DATA.SEQ_NUM);
		public static final UniqueKey<OfcSamplingDesignRecord> PK_OFC_SAMPLING_DESIGN = createUniqueKey(OfcSamplingDesign.OFC_SAMPLING_DESIGN, OfcSamplingDesign.OFC_SAMPLING_DESIGN.ID);
		public static final UniqueKey<OfcSurveyRecord> OFC_SURVEY_PKEY = createUniqueKey(OfcSurvey.OFC_SURVEY, OfcSurvey.OFC_SURVEY.ID);
		public static final UniqueKey<OfcSurveyRecord> OFC_SURVEY_NAME_KEY = createUniqueKey(OfcSurvey.OFC_SURVEY, OfcSurvey.OFC_SURVEY.NAME, OfcSurvey.OFC_SURVEY.TEMPORARY);
		public static final UniqueKey<OfcSurveyRecord> OFC_SURVEY_URI_KEY = createUniqueKey(OfcSurvey.OFC_SURVEY, OfcSurvey.OFC_SURVEY.URI, OfcSurvey.OFC_SURVEY.TEMPORARY);
		public static final UniqueKey<OfcSurveyFileRecord> OFC_SURVEY_FILE_PKEY = createUniqueKey(OfcSurveyFile.OFC_SURVEY_FILE, OfcSurveyFile.OFC_SURVEY_FILE.ID);
		public static final UniqueKey<OfcTaxonRecord> OFC_TAXON_PKEY = createUniqueKey(OfcTaxon.OFC_TAXON, OfcTaxon.OFC_TAXON.ID);
		public static final UniqueKey<OfcTaxonRecord> OFC_TAXON_ID_KEY = createUniqueKey(OfcTaxon.OFC_TAXON, OfcTaxon.OFC_TAXON.TAXON_ID, OfcTaxon.OFC_TAXON.TAXONOMY_ID);
		public static final UniqueKey<OfcTaxonVernacularNameRecord> OFC_TAXON_VERNACULAR_NAME_PKEY = createUniqueKey(OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME, OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME.ID);
		public static final UniqueKey<OfcTaxonomyRecord> OFC_TAXONOMY_PKEY = createUniqueKey(OfcTaxonomy.OFC_TAXONOMY, OfcTaxonomy.OFC_TAXONOMY.ID);
		public static final UniqueKey<OfcTaxonomyRecord> OFC_TAXONOMY_NAME_KEY = createUniqueKey(OfcTaxonomy.OFC_TAXONOMY, OfcTaxonomy.OFC_TAXONOMY.SURVEY_ID, OfcTaxonomy.OFC_TAXONOMY.NAME);
		public static final UniqueKey<OfcUserRecord> OFC_USER_PKEY = createUniqueKey(OfcUser.OFC_USER, OfcUser.OFC_USER.ID);
		public static final UniqueKey<OfcUserRecord> OFC_USER_USERNAME_KEY = createUniqueKey(OfcUser.OFC_USER, OfcUser.OFC_USER.USERNAME);
		public static final UniqueKey<OfcUserRoleRecord> OFC_USER_ROLE_PKEY = createUniqueKey(OfcUserRole.OFC_USER_ROLE, OfcUserRole.OFC_USER_ROLE.ID);
		public static final UniqueKey<OfcUserUsergroupRecord> OFC_USER_USERGROUP_PKEY = createUniqueKey(OfcUserUsergroup.OFC_USER_USERGROUP, OfcUserUsergroup.OFC_USER_USERGROUP.USER_ID, OfcUserUsergroup.OFC_USER_USERGROUP.GROUP_ID);
		public static final UniqueKey<OfcUsergroupRecord> OFC_USERGROUP_PKEY = createUniqueKey(OfcUsergroup.OFC_USERGROUP, OfcUsergroup.OFC_USERGROUP.ID);
		public static final UniqueKey<OfcUsergroupRecord> OFC_USERGROUP_NAME_KEY = createUniqueKey(OfcUsergroup.OFC_USERGROUP, OfcUsergroup.OFC_USERGROUP.NAME);
	}

	private static class ForeignKeys0 extends AbstractKeys {
		public static final ForeignKey<OfcCodeListRecord, OfcSurveyRecord> OFC_CODE_LIST__OFC_CODE_LIST_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcCodeList.OFC_CODE_LIST, OfcCodeList.OFC_CODE_LIST.SURVEY_ID);
		public static final ForeignKey<OfcCodeListRecord, OfcCodeListRecord> OFC_CODE_LIST__OFC_CODE_LIST_PARENT_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_CODE_LIST_PKEY, OfcCodeList.OFC_CODE_LIST, OfcCodeList.OFC_CODE_LIST.PARENT_ID);
		public static final ForeignKey<OfcDataCleansingChainRecord, OfcSurveyRecord> OFC_DATA_CLEANSING_CHAIN__OFC_DATA_CLEANSING_CHAIN_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN, OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.SURVEY_ID);
		public static final ForeignKey<OfcDataCleansingChainStepsRecord, OfcDataCleansingChainRecord> OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_CHAIN_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_CLEANSING_CHAIN_PKEY, OfcDataCleansingChainSteps.OFC_DATA_CLEANSING_CHAIN_STEPS, OfcDataCleansingChainSteps.OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID);
		public static final ForeignKey<OfcDataCleansingChainStepsRecord, OfcDataCleansingStepRecord> OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_STEP_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_CLEANSING_STEP_PKEY, OfcDataCleansingChainSteps.OFC_DATA_CLEANSING_CHAIN_STEPS, OfcDataCleansingChainSteps.OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID);
		public static final ForeignKey<OfcDataCleansingReportRecord, OfcDataCleansingChainRecord> OFC_DATA_CLEANSING_REPORT__OFC_DATA_CLEANSING_CHAIN_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_CLEANSING_CHAIN_PKEY, OfcDataCleansingReport.OFC_DATA_CLEANSING_REPORT, OfcDataCleansingReport.OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID);
		public static final ForeignKey<OfcDataCleansingStepRecord, OfcDataQueryRecord> OFC_DATA_CLEANSING_STEP__OFC_DATA_CLEANSING_STEP_QUERY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_QUERY_PKEY, OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP, OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.QUERY_ID);
		public static final ForeignKey<OfcDataCleansingStepValueRecord, OfcDataCleansingStepRecord> OFC_DATA_CLEANSING_STEP_VALUE__OFC_DATA_CLEANSING_STEP_VALUE_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_CLEANSING_STEP_PKEY, OfcDataCleansingStepValue.OFC_DATA_CLEANSING_STEP_VALUE, OfcDataCleansingStepValue.OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID);
		public static final ForeignKey<OfcDataQueryRecord, OfcSurveyRecord> OFC_DATA_QUERY__OFC_DATA_QUERY_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcDataQuery.OFC_DATA_QUERY, OfcDataQuery.OFC_DATA_QUERY.SURVEY_ID);
		public static final ForeignKey<OfcDataQueryRecord, OfcDataQueryTypeRecord> OFC_DATA_QUERY__OFC_DATA_QUERY_TYPE_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_QUERY_TYPE_PKEY, OfcDataQuery.OFC_DATA_QUERY, OfcDataQuery.OFC_DATA_QUERY.TYPE_ID);
		public static final ForeignKey<OfcDataQueryGroupRecord, OfcSurveyRecord> OFC_DATA_QUERY_GROUP__OFC_DATA_QUERY_GROUP_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcDataQueryGroup.OFC_DATA_QUERY_GROUP, OfcDataQueryGroup.OFC_DATA_QUERY_GROUP.SURVEY_ID);
		public static final ForeignKey<OfcDataQueryGroupQueryRecord, OfcDataQueryGroupRecord> OFC_DATA_QUERY_GROUP_QUERY__OFC_DATA_QUERY_GROUP_GROUP_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_QUERY_GROUP_PKEY, OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY, OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID);
		public static final ForeignKey<OfcDataQueryGroupQueryRecord, OfcDataQueryRecord> OFC_DATA_QUERY_GROUP_QUERY__OFC_DATA_QUERY_GROUP_QUERY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_QUERY_PKEY, OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY, OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY.QUERY_ID);
		public static final ForeignKey<OfcDataQueryTypeRecord, OfcSurveyRecord> OFC_DATA_QUERY_TYPE__OFC_DATA_QUERY_TYPE_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcDataQueryType.OFC_DATA_QUERY_TYPE, OfcDataQueryType.OFC_DATA_QUERY_TYPE.SURVEY_ID);
		public static final ForeignKey<OfcDataReportRecord, OfcDataQueryGroupRecord> OFC_DATA_REPORT__OFC_DATA_REPORT_QUERY_GROUP_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_QUERY_GROUP_PKEY, OfcDataReport.OFC_DATA_REPORT, OfcDataReport.OFC_DATA_REPORT.QUERY_GROUP_ID);
		public static final ForeignKey<OfcDataReportItemRecord, OfcDataReportRecord> OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_REPORT_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_REPORT_PKEY, OfcDataReportItem.OFC_DATA_REPORT_ITEM, OfcDataReportItem.OFC_DATA_REPORT_ITEM.REPORT_ID);
		public static final ForeignKey<OfcDataReportItemRecord, OfcDataQueryRecord> OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_QUERY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_DATA_QUERY_PKEY, OfcDataReportItem.OFC_DATA_REPORT_ITEM, OfcDataReportItem.OFC_DATA_REPORT_ITEM.QUERY_ID);
		public static final ForeignKey<OfcDataReportItemRecord, OfcRecordRecord> OFC_DATA_REPORT_ITEM__OFC_DATA_REPORT_ITEM_RECORD_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_RECORD_PKEY, OfcDataReportItem.OFC_DATA_REPORT_ITEM, OfcDataReportItem.OFC_DATA_REPORT_ITEM.RECORD_ID);
		public static final ForeignKey<OfcRecordRecord, OfcSurveyRecord> OFC_RECORD__OFC_RECORD_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcRecord.OFC_RECORD, OfcRecord.OFC_RECORD.SURVEY_ID);
		public static final ForeignKey<OfcRecordDataRecord, OfcRecordRecord> OFC_RECORD_DATA__OFC_RECORD_DATA_RECORD_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_RECORD_PKEY, OfcRecordData.OFC_RECORD_DATA, OfcRecordData.OFC_RECORD_DATA.RECORD_ID);
		public static final ForeignKey<OfcSamplingDesignRecord, OfcSurveyRecord> OFC_SAMPLING_DESIGN__OFC_SAMPLING_DESIGN_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcSamplingDesign.OFC_SAMPLING_DESIGN, OfcSamplingDesign.OFC_SAMPLING_DESIGN.SURVEY_ID);
		public static final ForeignKey<OfcSurveyRecord, OfcUsergroupRecord> OFC_SURVEY__OFC_SURVEY_USERGROUP_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_USERGROUP_PKEY, OfcSurvey.OFC_SURVEY, OfcSurvey.OFC_SURVEY.USERGROUP_ID);
		public static final ForeignKey<OfcSurveyFileRecord, OfcSurveyRecord> OFC_SURVEY_FILE__OFC_SURVEY_FILE_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcSurveyFile.OFC_SURVEY_FILE, OfcSurveyFile.OFC_SURVEY_FILE.SURVEY_ID);
		public static final ForeignKey<OfcTaxonRecord, OfcTaxonomyRecord> OFC_TAXON__OFC_TAXON_TAXONOMY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_TAXONOMY_PKEY, OfcTaxon.OFC_TAXON, OfcTaxon.OFC_TAXON.TAXONOMY_ID);
		public static final ForeignKey<OfcTaxonRecord, OfcTaxonRecord> OFC_TAXON__OFC_TAXON_PARENT_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_TAXON_PKEY, OfcTaxon.OFC_TAXON, OfcTaxon.OFC_TAXON.PARENT_ID);
		public static final ForeignKey<OfcTaxonVernacularNameRecord, OfcTaxonRecord> OFC_TAXON_VERNACULAR_NAME__OFC_TAXON_VERNACULAR_NAME_TAXON_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_TAXON_PKEY, OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME, OfcTaxonVernacularName.OFC_TAXON_VERNACULAR_NAME.TAXON_ID);
		public static final ForeignKey<OfcTaxonomyRecord, OfcSurveyRecord> OFC_TAXONOMY__OFC_TAXONOMY_SURVEY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_SURVEY_PKEY, OfcTaxonomy.OFC_TAXONOMY, OfcTaxonomy.OFC_TAXONOMY.SURVEY_ID);
		public static final ForeignKey<OfcUserRoleRecord, OfcUserRecord> OFC_USER_ROLE__OFC_USER_USER_ROLE_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_USER_PKEY, OfcUserRole.OFC_USER_ROLE, OfcUserRole.OFC_USER_ROLE.USER_ID);
		public static final ForeignKey<OfcUserUsergroupRecord, OfcUserRecord> OFC_USER_USERGROUP__OFC_USER_USERGROUP_USER_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_USER_PKEY, OfcUserUsergroup.OFC_USER_USERGROUP, OfcUserUsergroup.OFC_USER_USERGROUP.USER_ID);
		public static final ForeignKey<OfcUserUsergroupRecord, OfcUsergroupRecord> OFC_USER_USERGROUP__OFC_USER_USERGROUP_GROUP_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_USERGROUP_PKEY, OfcUserUsergroup.OFC_USER_USERGROUP, OfcUserUsergroup.OFC_USER_USERGROUP.GROUP_ID);
		public static final ForeignKey<OfcUsergroupRecord, OfcUsergroupRecord> OFC_USERGROUP__OFC_USERGROUP_PARENT_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_USERGROUP_PKEY, OfcUsergroup.OFC_USERGROUP, OfcUsergroup.OFC_USERGROUP.PARENT_ID);
		public static final ForeignKey<OfcUsergroupRecord, OfcUserRecord> OFC_USERGROUP__OFC_USERGROUP_CREATED_BY_FKEY = createForeignKey(org.openforis.collect.persistence.jooq.Keys.OFC_USER_PKEY, OfcUsergroup.OFC_USERGROUP, OfcUsergroup.OFC_USERGROUP.CREATED_BY);
	}
}
