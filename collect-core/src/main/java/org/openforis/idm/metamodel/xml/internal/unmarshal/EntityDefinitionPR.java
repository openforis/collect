package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.ENTITY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.GENERATOR_EXPRESSION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VIRTUAL;

import java.io.IOException;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
class EntityDefinitionPR extends NodeDefinitionPR {

	public EntityDefinitionPR() {
		super(ENTITY);
		addChildPullReaders(
				this,
				new BooleanAttributeDefinitionPR(), 
				new CodeAttributeDefinitionPR(),
				new CoordinateAttributeDefinitionPR(),
				new DateAttributeDefinitionPR(),
				new TimeAttributeDefinitionPR(),
				new FileAttributeDefinitionPR(),
				new NumberAttributeDefinitionPR(),
				new RangeAttributeDefinitionPR(),
				new TaxonAttributeDefinitionPR(),
				new TextAttributeDefinitionPR());
	}

	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createEntityDefinition(id);
	}
	
	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		EntityDefinition def = (EntityDefinition) getDefinition();
		boolean virtual = getBooleanAttributeWithDefault(VIRTUAL, false);
		def.setVirtual(virtual);
		if (virtual) {
			String generatorExpression = getAttribute(GENERATOR_EXPRESSION, true);
			def.setGeneratorExpression(generatorExpression);
		}
	}
}