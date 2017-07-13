package org.openforis.collect.relational.util;

import static org.openforis.collect.relational.util.Constants.CODE_TABLE_PK_FORMAT;
import static org.openforis.collect.relational.util.Constants.TABLE_NAME_QNAME;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListTables {
	
	public static String getTableName(CodeAttributeDefinition attrDef) {
		return getTableName(RelationalSchemaConfig.createDefault(), attrDef);
	}
	
	public static String getTableName(RelationalSchemaConfig config, CodeAttributeDefinition attrDef) {
		return getTableName(config, attrDef.getList(), attrDef.getListLevelIndex());
	}
	
	public static String getTableName(CodeList codeList, Integer levelIdx) {
		return getTableName(RelationalSchemaConfig.createDefault(), codeList, levelIdx);
	}
	
	public static String getTableName(RelationalSchemaConfig config, CodeList codeList, Integer levelIdx) {
		String baseTableName = CodeListTables.getBaseTableName(codeList, levelIdx);
		return baseTableName + config.getCodeListTableSuffix();
	}
	
	public static String getIdColumnName(String tableName) {
		return getIdColumnName(RelationalSchemaConfig.createDefault(), tableName);
	}
	
	public static String getIdColumnName(RelationalSchemaConfig config, String tableName) {
		String result = String.format(CODE_TABLE_PK_FORMAT, tableName, config.getIdColumnSuffix());
		return result;
	}

	public static String getCodeColumnName(CodeList codeList, Integer hierarchyIdx) {
		return getBaseTableName(codeList, hierarchyIdx);
	}
	
	public static String getCodeColumnName(String tableName) {
		return getCodeColumnName(RelationalSchemaConfig.createDefault(), tableName);
	}
	
	public static String getCodeColumnName(RelationalSchemaConfig config, String tableName) {
		return StringUtils.removeEnd(tableName, config.getCodeListTableSuffix());
	}

	public static String getLabelColumnName(CodeList codeList, Integer levelIdx) {
		return getLabelColumnName(RelationalSchemaConfig.createDefault(), codeList, levelIdx);
	}
	
	public static String getLabelColumnName(RelationalSchemaConfig config, CodeList codeList, Integer levelIdx) {
		return getLabelColumnName(config, codeList, levelIdx, null);
	}
	
	public static String getLabelColumnName(String tableName) {
		return getLabelColumnName(RelationalSchemaConfig.createDefault(), tableName);
	}

	public static String getLabelColumnName(RelationalSchemaConfig config, String tableName) {
		String baseName = extractBaseTableName(config, tableName);
		return getLabelColumnName(config, baseName, null);
	}

	
	public static String getLabelColumnName(CodeList codeList, Integer levelIdx, String langCode) {
		return getLabelColumnName(RelationalSchemaConfig.createDefault(), codeList, levelIdx, langCode);
	}
	
	public static String getLabelColumnName(RelationalSchemaConfig config, CodeList codeList, Integer levelIdx, String langCode) {
		String baseTableName = getBaseTableName(codeList, levelIdx);
		return getLabelColumnName(config, baseTableName, langCode);
	}

	private static String getLabelColumnName(RelationalSchemaConfig config, String baseTableName, String langCode) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(baseTableName);
		sb.append(config.getLabelColumnSuffix());
		if ( StringUtils.isNotBlank(langCode) ) {
			sb.append("_");
			sb.append(langCode);
		}
		return sb.toString();
	}
	
	public static String getDescriptionColumnName(String tableName) {
		return getDescriptionColumnName(RelationalSchemaConfig.createDefault(), tableName);
	}

	public static String getDescriptionColumnName(RelationalSchemaConfig config, String tableName) {
		String baseName = extractBaseTableName(config, tableName);
		return getDescriptionColumnName(config, baseName, null);
	}
	
	public static String getDescriptionColumnName(CodeList codeList, Integer levelIdx) {
		return getDescriptionColumnName(RelationalSchemaConfig.createDefault(), codeList, levelIdx);
	}
	
	public static String getDescriptionColumnName(RelationalSchemaConfig config, CodeList codeList, Integer levelIdx) {
		return getDescriptionColumnName(config, codeList, levelIdx, null);
	}

	public static String getDescriptionColumnName(CodeList codeList, Integer levelIdx, String langCode) {
		return getDescriptionColumnName(RelationalSchemaConfig.createDefault(), codeList, levelIdx, langCode);
	}
	
	public static String getDescriptionColumnName(RelationalSchemaConfig config, CodeList codeList, Integer levelIdx, String langCode) {
		String baseTableName = getBaseTableName(codeList, levelIdx);
		return getDescriptionColumnName(config, baseTableName, langCode);
	}

	protected static String getDescriptionColumnName(RelationalSchemaConfig config, String baseTableName, String langCode) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(baseTableName);
		sb.append(config.getDescriptionColumnSuffix());
		if ( StringUtils.isNotBlank(langCode) ) {
			sb.append("_");
			sb.append(langCode);
		}
		return sb.toString();
	}
	
	private static String getBaseTableName(CodeList codeList, Integer levelIdx) {
		StringBuilder sb = new StringBuilder();
		String tableNameAnnotation = codeList.getAnnotation(TABLE_NAME_QNAME);
		if ( tableNameAnnotation == null ) {
			sb.append(codeList.getName());
		} else {
			sb.append(tableNameAnnotation);
		}
		if ( codeList.isHierarchical() && levelIdx != null ) {
			List<CodeListLevel> hierarchy = codeList.getHierarchy();
			if (levelIdx >= hierarchy.size()) {
				throw new IllegalArgumentException(String.format(
						"The hierarchy of code list '%s' has %d levels, but level %d has been asked", 
						codeList.getName(), hierarchy.size(), levelIdx + 1));
			}
			CodeListLevel currentLevel = hierarchy.get(levelIdx);
			sb.append("_");
			sb.append(currentLevel.getName());
		}
		return sb.toString();
	}

	private static String extractBaseTableName(RelationalSchemaConfig config, String tableName) {
		String baseName = StringUtils.removeEnd(tableName, config.getCodeListTableSuffix());
		return baseName;
	}
	
}
