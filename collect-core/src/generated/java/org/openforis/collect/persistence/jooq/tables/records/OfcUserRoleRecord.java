/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.collect.persistence.jooq.tables.OfcUserRole;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcUserRoleRecord extends UpdatableRecordImpl<OfcUserRoleRecord> implements Record3<Integer, Integer, String> {

	private static final long serialVersionUID = 628328402;

	/**
	 * Setter for <code>collect.ofc_user_role.id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>collect.ofc_user_role.id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>collect.ofc_user_role.user_id</code>.
	 */
	public void setUserId(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>collect.ofc_user_role.user_id</code>.
	 */
	public Integer getUserId() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>collect.ofc_user_role.role</code>.
	 */
	public void setRole(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>collect.ofc_user_role.role</code>.
	 */
	public String getRole() {
		return (String) getValue(2);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<Integer> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row3<Integer, Integer, String> fieldsRow() {
		return (Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row3<Integer, Integer, String> valuesRow() {
		return (Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return OfcUserRole.OFC_USER_ROLE.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field2() {
		return OfcUserRole.OFC_USER_ROLE.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field3() {
		return OfcUserRole.OFC_USER_ROLE.ROLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value2() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value3() {
		return getRole();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcUserRoleRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcUserRoleRecord value2(Integer value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcUserRoleRecord value3(String value) {
		setRole(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcUserRoleRecord values(Integer value1, Integer value2, String value3) {
		value1(value1);
		value2(value2);
		value3(value3);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached OfcUserRoleRecord
	 */
	public OfcUserRoleRecord() {
		super(OfcUserRole.OFC_USER_ROLE);
	}

	/**
	 * Create a detached, initialised OfcUserRoleRecord
	 */
	public OfcUserRoleRecord(Integer id, Integer userId, String role) {
		super(OfcUserRole.OFC_USER_ROLE);

		setValue(0, id);
		setValue(1, userId);
		setValue(2, role);
	}
}
