package org.openforis.collect.persistence.jooq;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jooq.Configuration;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.Sequence;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class MappingDSLContext<I extends Number,E> extends CollectDSLContext {

	private static final long serialVersionUID = 1L;
	
	private TableField<?,I> idField;
	private Sequence<? extends Number> idSequence;
	private Class<E> clazz;
	
	public MappingDSLContext(Configuration config, TableField<?,I> idField, Sequence<? extends Number> idSequence, Class<E> clazz) {
		super(config);
		this.idField = idField;
		this.idSequence = idSequence;
		this.clazz = clazz;
	}
	
	protected abstract void setId(E entity, I id);

	protected abstract I getId(E entity);
	
	protected abstract void fromRecord(Record r, E object);
	
	protected abstract void fromObject(E object, StoreQuery<?> q);

	public SelectQuery<?> selectByIdQuery(I id) {
		SelectQuery<?> select = selectQuery(getTable());
		select.addConditions(idField.eq(id));
		return select;
	}

	public <T> SelectQuery<?> selectByFieldQuery(TableField<?,T> field, T value) {
		SelectQuery<?> select = selectQuery(getTable());
		select.addConditions(field.eq(value));
		return select;
	}

	public SelectQuery<?> selectCountQuery() {
		SelectQuery<?> select = selectQuery();
		select.addSelect(DSL.count());
		select.addFrom(getTable());
		return select;
	}

	public <T> SelectQuery<?> selectStartsWithQuery(TableField<?,String> field, String searchString) {
		if ( searchString == null || searchString.isEmpty() ) {
			throw new IllegalArgumentException("Search string required");
		}
		SelectQuery<?> select = selectQuery(getTable());
		searchString = searchString.toUpperCase(Locale.ENGLISH) + "%";
		select.addConditions(DSL.upper(field).like(searchString));
		return select;
	}

	public <T> SelectQuery<?> selectContainsQuery(TableField<?,String> field, String searchString) {
		if ( searchString == null || searchString.isEmpty() ) {
			throw new IllegalArgumentException("Search string required");
		}
		SelectQuery<?> select = selectQuery(getTable());
		searchString = "%" + searchString.toUpperCase(Locale.ENGLISH) + "%";
		select.addConditions(DSL.upper(field).like(searchString));
		return select;
	}
	
	public DeleteQuery<?> deleteQuery(I id) {
		DeleteQuery<?> delete = deleteQuery(getTable());
		delete.addConditions(idField.equal(id));
		return delete;
	}

	public TableField<?,I> getIdField() {
		return idField;
	}
	
	public Sequence<?> getIdSequence() {
		return idSequence;
	}
	
	public TableImpl<?> getTable() {
		return (TableImpl<?>) idField.getTable();
	}
	
	@SuppressWarnings({"rawtypes"})
	public InsertQuery insertQuery(E object) {
		I id = getId(object);
		if ( id == null ) {
			I nextId = nextId();
			setId(object, nextId);
		}
		InsertQuery insert = insertQuery(getTable());
		fromObject(object, insert);
		return insert;
	}

	public I nextId() {
		return nextId(idField, idSequence);
	}
	
	public void restartSequence(Number value) {
		restartSequence(idSequence, value);
	}
	
	@SuppressWarnings({"rawtypes"})
	public UpdateQuery updateQuery(E object) {
		UpdateQuery update = updateQuery(getTable());
		fromObject(object, update);
		I id = getId(object);
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
	
	protected void addFieldValues(StoreQuery<?> q, Field<Object>[] fields, List<?> values) {
		for ( int i = 0; i < fields.length; i++ ) {
			Field<Object> field = fields[i];
			Object value = i < values.size() ? values.get(i) : null;
			q.addValue(field, value);
		}
	}
	
	protected <T extends Object> List<T> extractFields(Record r, Field<T>[] fields) {
		List<T> result = new ArrayList<T>(fields.length);
		for (int i = 0; i < fields.length; i++) {
			Field<T> field = fields[i];
			T value = r.getValue(field);
			result.add(value);
		}
		return result;
	}
	
}
