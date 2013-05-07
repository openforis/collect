package org.openforis.idm.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.path.Path;
import org.openforis.idm.transform2.Transformation;

/**
 * 
 * @author G. Miceli
 *
 */
public class RelationalTransformer {

	private Survey survey;
	private Map<Path, Transformation> dataTransforms;
	private Schema schema;
	
	public RelationalTransformer(Survey survey) {
		this.survey = survey;
		initDataTableTransformations();
	}
	
	private void initDataTableTransformations() {
		this.dataTransforms = new LinkedHashMap<Path, Transformation>();
		this.schema = survey.getSchema();
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			initDataTableTransformations(root);
		}
	}

	/**
	 * Recursively init table transformations
	 * @param defn
	 */
	private void initDataTableTransformations(EntityDefinition defn) {
		List<NodeDefinition> children = defn.getChildDefinitions();
		for (NodeDefinition child : children) {
			if ( child instanceof EntityDefinition ) {
				initDataTableTransformations((EntityDefinition) child);
			}
		}
		if ( defn.isMultiple() ) {
			Transformation xform = Transformation.createDefaultTransformation(defn);
			Path path = (Path) xform.getRowAxis();
			dataTransforms.put(path, xform);
			System.out.println(path);
		}
	}
	
	public List<Transformation> getTransformations() {
		List<Transformation> xforms = new ArrayList<Transformation>(dataTransforms.values());
		return Collections.unmodifiableList(xforms);
	}
}
