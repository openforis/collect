/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import org.openforis.collect.persistence.jooq.Collect;
import org.openforis.collect.persistence.jooq.Keys;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcCodeList extends TableImpl<OfcCodeListRecord> {

	private static final long serialVersionUID = -753701108;

	/**
	 * The reference instance of <code>collect.ofc_code_list</code>
	 */
	public static final OfcCodeList OFC_CODE_LIST = new OfcCodeList();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<OfcCodeListRecord> getRecordType() {
		return OfcCodeListRecord.class;
	}

	/**
	 * The column <code>collect.ofc_code_list.id</code>.
	 */
	public final TableField<OfcCodeListRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.survey_id</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> SURVEY_ID = createField("survey_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.code_list_id</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> CODE_LIST_ID = createField("code_list_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.item_id</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> ITEM_ID = createField("item_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.parent_id</code>.
	 */
	public final TableField<OfcCodeListRecord, Long> PARENT_ID = createField("parent_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

	/**
	 * The column <code>collect.ofc_code_list.sort_order</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> SORT_ORDER = createField("sort_order", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.code</code>.
	 */
	public final TableField<OfcCodeListRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_code_list.qualifiable</code>.
	 */
	public final TableField<OfcCodeListRecord, Boolean> QUALIFIABLE = createField("qualifiable", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>collect.ofc_code_list.since_version_id</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> SINCE_VERSION_ID = createField("since_version_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.deprecated_version_id</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> DEPRECATED_VERSION_ID = createField("deprecated_version_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.label1</code>.
	 */
	public final TableField<OfcCodeListRecord, String> LABEL1 = createField("label1", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.label2</code>.
	 */
	public final TableField<OfcCodeListRecord, String> LABEL2 = createField("label2", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.label3</code>.
	 */
	public final TableField<OfcCodeListRecord, String> LABEL3 = createField("label3", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description1</code>.
	 */
	public final TableField<OfcCodeListRecord, String> DESCRIPTION1 = createField("description1", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description2</code>.
	 */
	public final TableField<OfcCodeListRecord, String> DESCRIPTION2 = createField("description2", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description3</code>.
	 */
	public final TableField<OfcCodeListRecord, String> DESCRIPTION3 = createField("description3", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * The column <code>collect.ofc_code_list.level</code>.
	 */
	public final TableField<OfcCodeListRecord, Integer> LEVEL = createField("level", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>collect.ofc_code_list.image_content</code>.
	 */
	public final TableField<OfcCodeListRecord, byte[]> IMAGE_CONTENT = createField("image_content", org.jooq.impl.SQLDataType.BLOB, this, "");

	/**
	 * The column <code>collect.ofc_code_list.image_file_name</code>.
	 */
	public final TableField<OfcCodeListRecord, String> IMAGE_FILE_NAME = createField("image_file_name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.label4</code>.
	 */
	public final TableField<OfcCodeListRecord, String> LABEL4 = createField("label4", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.label5</code>.
	 */
	public final TableField<OfcCodeListRecord, String> LABEL5 = createField("label5", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description4</code>.
	 */
	public final TableField<OfcCodeListRecord, String> DESCRIPTION4 = createField("description4", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * The column <code>collect.ofc_code_list.description5</code>.
	 */
	public final TableField<OfcCodeListRecord, String> DESCRIPTION5 = createField("description5", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * The column <code>collect.ofc_code_list.color</code>.
	 */
	public final TableField<OfcCodeListRecord, String> COLOR = createField("color", org.jooq.impl.SQLDataType.CHAR.length(6), this, "");

	/**
	 * Create a <code>collect.ofc_code_list</code> table reference
	 */
	public OfcCodeList() {
		this("ofc_code_list", null);
	}

	/**
	 * Create an aliased <code>collect.ofc_code_list</code> table reference
	 */
	public OfcCodeList(String alias) {
		this(alias, OFC_CODE_LIST);
	}

	private OfcCodeList(String alias, Table<OfcCodeListRecord> aliased) {
		this(alias, aliased, null);
	}

	private OfcCodeList(String alias, Table<OfcCodeListRecord> aliased, Field<?>[] parameters) {
		super(alias, Collect.COLLECT, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<OfcCodeListRecord> getPrimaryKey() {
		return Keys.OFC_CODE_LIST_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<OfcCodeListRecord>> getKeys() {
		return Arrays.<UniqueKey<OfcCodeListRecord>>asList(Keys.OFC_CODE_LIST_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<OfcCodeListRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<OfcCodeListRecord, ?>>asList(Keys.OFC_CODE_LIST__OFC_CODE_LIST_SURVEY_FKEY, Keys.OFC_CODE_LIST__OFC_CODE_LIST_PARENT_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcCodeList as(String alias) {
		return new OfcCodeList(alias, this);
	}

	/**
	 * Rename this table
	 */
	public OfcCodeList rename(String name) {
		return new OfcCodeList(name, null);
	}
}
