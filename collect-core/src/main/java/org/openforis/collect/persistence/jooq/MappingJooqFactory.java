package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DeleteQuery;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.Sequence;
import org.jooq.SimpleSelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.UpdatableTable;
import org.jooq.UpdateQuery;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 */
public abstract class MappingJooqFactory<E> extends DialectAwareJooqFactory {
	private static final long serialVersionUID = 1L;
	
	private TableField<?,Integer> idField;
	private Sequence<? extends Number> idSequence;
	private Class<E> clazz;
	
	public MappingJooqFactory(Connection conn, TableField<?,Integer> idField, Sequence<? extends Number> idSequence, Class<E> clazz) {
		super(conn);
		this.idField = idField;
		this.idSequence = idSequence;
		this.clazz = clazz;
	}
	
	protected abstract void setId(E entity, int id);

	protected abstract Integer getId(E entity);
	
	protected abstract void fromRecord(Record r, E object);
	
	protected abstract void fromObject(E object, StoreQuery<?> q);

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

	public SelectQuery selectCountQuery() {
		SelectQuery select = selectQuery();
		select.addSelect(Factory.count());
		select.addFrom(getTable());
		return select;
	}

	public <T> SimpleSelectQuery<?> selectStartsWithQuery(TableField<?,String> field, String searchString) {
		if ( searchString == null || searchString.isEmpty() ) {
			throw new IllegalArgumentException("Search string required");
		}
		SimpleSelectQuery<?> select = selectQuery(getTable());
		searchString = searchString.toUpperCase() + "%";
		select.addConditions(upper(field).like(searchString));
		return select;
	}

	public <T> SimpleSelectQuery<?> selectContainsQuery(TableField<?,String> field, String searchString) {
		if ( searchString == null || searchString.isEmpty() ) {
			throw new IllegalArgumentException("Search string required");
		}
		SimpleSelectQuery<?> select = selectQuery(getTable());
		searchString = "%" + searchString.toUpperCase() + "%";
		select.addConditions(upper(field).like(searchString));
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
	
	@SuppressWarnings({"rawtypes"})
	public InsertQuery insertQuery(E object) {
		Integer id = getId(object);
		if ( id == null ) {
			int nextId = nextId();
			setId(object, nextId);
		}
		InsertQuery insert = insertQuery(getTable());
		fromObject(object, insert);
		return insert;
	}

	protected int nextId() {
		return nextId(idField, idSequence);
	}
	
	protected void restartSequence(Number value) {
		restartSequence(idSequence, value);
	}
	
	@SuppressWarnings({"rawtypes"})
	public UpdateQuery updateQuery(E object) {
		
		UpdateQuery update = updateQuery(getTable());
		fromObject(object, update);
		Integer id = getId(object);
		if ( id == null ) {
			throw new IllegalArgumentException("Cannot update with null id");
		}
		update.addConditions(idField.equal(id));
		return update;
	}

	public E fromRecord(Record record) {
		E entity = newEntity();
		fromRecord(record, entity);
		return entity;
	}
	
	public List<E> fromResult(Result<?> records) {
		List<E> entities = new ArrayList<E>(records.size());
		for (Record record : records) {
			E result = fromRecord(record);
			entities.add(result);
		}
		return entities;
	}

	protected E newEntity() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
