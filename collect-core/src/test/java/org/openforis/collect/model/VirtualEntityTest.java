package org.openforis.collect.model;

import static org.junit.Assert.assertEquals;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.model.Entity;

public class VirtualEntityTest extends AbstractRecordTest {
	
	@Test
	public void testThisVariableCanReturnNodes() {
		record(
			rootEntityDef(
				entityDef("land_use_section", 
					attributeDef("land_use_section_id"),
					attributeDef("land_use_type"),
					entityDef("tree_alias",
						attributeDef("species")
					).multiple()
					.virtual()
					.generatorExpression("parent()/tree[land_use_section_id=$context/land_use_section_id]")
				).multiple(),
				entityDef("tree",
					attributeDef("land_use_section_id"),
					attributeDef("species")
				).multiple()
			),
			entity("tree",
				attribute("land_use_section_id", "1")
			),
			entity("tree",
				attribute("land_use_section_id", "1")
			),
			entity("tree",
				attribute("land_use_section_id", "1")
			),
			entity("tree",
				attribute("land_use_section_id", "2")
			),
			entity("land_use_section",
				attribute("land_use_section_id", "1")
			),
			entity("land_use_section",
				attribute("land_use_section_id", "2")
			)
		);
		List<Entity> treeAliases = record.findNodesByExpression("land_use_section[land_use_section_id=1]/tree_alias");
		assertEquals(3, treeAliases.size());
	}


}
