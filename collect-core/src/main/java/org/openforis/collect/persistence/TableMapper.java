package org.openforis.collect.persistence;

import org.jooq.DeleteQuery;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Sequence;
import org.jooq.SimpleSelectQuery;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.jooq.UpdatableTable;
import org.jooq.UpdateQuery;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class TableMapper<E> {
	private TableField<?,Integer> idField;
	private Sequence<?> idSequence;
	private Class<E> clazz;
	
	public TableMapper(TableField<?,Integer> idField, Sequence<?> idSequence, Class<E> clazz) {
		this.idField = idField;
		this.idSequence = idSequence;
		this.clazz = clazz;
	}
	

	public SimpleSelectQuery<?> selectQuery(Factory factory, int id) {
		SimpleSelectQuery<?> select = factory.selectQuery(getTable());
		select.addConditions(idField.equal(id));
		return select;
	}

	public DeleteQuery<?> deleteQuery(Factory factory, int id) {
		DeleteQuery<?> delete = factory.deleteQuery(getTable());
		delete.addConditions(idField.equal(id));
		return delete;
	}

	public TableField<?,Integer> getIdField() {
		return idField;
	}
	
	public Sequence<?> getIdSequence() {
		return idSequence;
	}
	
	public Table<?> getTable() {
		return idField.getTable();
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public InsertQuery insertQuery(Factory factory, E entity) {
		int nextId = nextId(factory);
		setId(entity, nextId);
		
		UpdatableRecord record = toRecord(factory, entity);
		
		InsertQuery insert = factory.insertQuery(getTable());
		insert.setRecord(record);
		return insert;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public UpdateQuery updateQuery(Factory factory, E entity) {
		UpdatableRecord record = toRecord(factory, entity);
		
		UpdateQuery update = factory.updateQuery(getTable());
		update.setRecord(record);
		return update;
	}
	
	protected abstract void setId(E entity, int id);

	private int nextId(Factory factory) {
		return factory.nextval(idSequence).intValue();
	}

	public E fromRecord(Record record) {
		E entity = newEntity();
		fromRecord(record, entity);
		return entity;
	}

	private E newEntity() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public UpdatableRecord<?> toRecord(Factory factory, E entity) {
		UpdatableRecord<?> record = factory.newRecord((UpdatableTable) getTable());
		toRecord(entity, record);
		return record;
	}
	
	protected abstract void fromRecord(Record r, E entity);
	
	protected abstract void toRecord(E entity, UpdatableRecord<?> r);
}
