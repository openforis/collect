package org.openforis.collect.persistence;

import java.sql.Connection;

import org.jooq.DeleteQuery;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Sequence;
import org.jooq.SimpleSelectQuery;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.jooq.UpdatableTable;
import org.jooq.UpdateQuery;
import org.openforis.collect.persistence.jooq.CollectJooqFactory;

/**
 * @author G. Miceli
 */
public abstract class MappingJooqFactory<E> extends CollectJooqFactory {
	private static final long serialVersionUID = 1L;
	
	private TableField<?,Integer> idField;
	private Sequence<?> idSequence;
	private Class<E> clazz;
	
	public MappingJooqFactory(Connection conn, TableField<?,Integer> idField, Sequence<?> idSequence, Class<E> clazz) {
		super(conn);
		this.idField = idField;
		this.idSequence = idSequence;
		this.clazz = clazz;
	}
	
	protected abstract void setId(E entity, int id);
	
	protected abstract void fromRecord(Record r, E entity);
	
	protected abstract void toRecord(E entity, UpdatableRecord<?> r);

	public SimpleSelectQuery<?> selectByIdQuery(int id) {
		SimpleSelectQuery<?> select = selectQuery(getTable());
		select.addConditions(idField.equal(id));
		return select;
	}

	public <T> SimpleSelectQuery<?> selectByFieldQuery(TableField<?,T> field, T value) {
		SimpleSelectQuery<?> select = selectQuery(getTable());
		select.addConditions(field.equal(value));
		return select;
	}

	
	public DeleteQuery<?> deleteQuery(int id) {
		DeleteQuery<?> delete = deleteQuery(getTable());
		delete.addConditions(idField.equal(id));
		return delete;
	}

	public TableField<?,Integer> getIdField() {
		return idField;
	}
	
	public Sequence<?> getIdSequence() {
		return idSequence;
	}
	
	public UpdatableTable<?> getTable() {
		return (UpdatableTable<?>) idField.getTable();
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public InsertQuery insertQuery(E entity) {
		int nextId = nextId();
		setId(entity, nextId);
		
		UpdatableRecord record = toRecord(entity);
		
		InsertQuery insert = insertQuery(getTable());
		insert.setRecord(record);
		return insert;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public UpdateQuery updateQuery(E entity) {
		UpdatableRecord record = toRecord( entity);
		
		UpdateQuery update = updateQuery(getTable());
		update.setRecord(record);
		return update;
	}

	public E fromRecord(Record record) {
		E entity = newEntity();
		fromRecord(record, entity);
		return entity;
	}
	
	@SuppressWarnings("rawtypes")
	public UpdatableRecord<?> toRecord(E entity) {
		UpdatableRecord record = (UpdatableRecord) newRecord((UpdatableTable) getTable());
		toRecord(entity, record);
		return record;
	}
	
	private int nextId() {
		return nextval(idSequence).intValue();
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
}
