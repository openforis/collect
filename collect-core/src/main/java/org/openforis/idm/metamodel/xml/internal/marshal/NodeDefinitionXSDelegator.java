package org.openforis.idm.metamodel.xml.internal.marshal;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author G. Miceli
 */
public class NodeDefinitionXSDelegator extends PolymorphicXmlSerializer<NodeDefinition, EntityDefinition> {
	public NodeDefinitionXSDelegator(EntityDefinitionXS entityXS) {
		setDelegate(EntityDefinition.class, entityXS);
		setDelegate(CodeAttributeDefinition.class, new CodeAttributeXS());
		setDelegate(BooleanAttributeDefinition.class, new BooleanAttributeXS());
		setDelegate(TextAttributeDefinition.class, new TextAttributeXS());
		setDelegate(NumberAttributeDefinition.class, new NumberAttributeXS());
		setDelegate(RangeAttributeDefinition.class, new RangeAttributeXS());
		setDelegate(FileAttributeDefinition.class, new FileAttributeXS());
		setDelegate(CoordinateAttributeDefinition.class, new CoordinateAttributeXS());
		setDelegate(DateAttributeDefinition.class, new DateAttributeXS());
		setDelegate(TimeAttributeDefinition.class, new TimeAttributeXS());
		setDelegate(TaxonAttributeDefinition.class, new TaxonAttributeXS());
	}
	
	@Override
	protected void marshalInstances(EntityDefinition parent) throws IOException {
		List<NodeDefinition> children = parent.getChildDefinitions();
		marshal(children);
	}
}
