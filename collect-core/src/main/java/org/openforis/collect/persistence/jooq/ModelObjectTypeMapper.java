package org.openforis.collect.persistence.jooq;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
public interface ModelObjectTypeMapper {
	
	public Class<?> getMappedClass(); 
	
	public void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert);
	
	public ModelObject<?> addObject(SchemaObjectDefinition defn, Record r, Entity parent);
}
