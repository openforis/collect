package org.openforis.idm.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.CustomCheck;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class DependencyGraphTest {

	private static final String ROOT_ENTITY_NAME = "root";
	protected Survey survey;
	protected Record record;
	protected Entity rootEntity;
	protected EntityDefinition rootEntityDef;

	public DependencyGraphTest() {
		super();
	}

	@Before
	public void before() {
		createTestSurvey();
		createTestRecord();
	}

	protected Attribute<?, ?> attribute(Entity parent, String name) {
		return attribute(parent, name, null, null, null);
	}

	protected Attribute<?, ?> attributeWithCheck(Entity parent, String name, String validationExpression) {
		return attribute(parent, name, null, null, validationExpression);
	}

	private Attribute<?, ?> attribute(Entity parent, String name, String calculatedExpression, String relevantExpression, String validationExpression) {
		EntityDefinition parentDefn = parent.getDefinition();
		AttributeDefinition defn;
		try {
			defn = (AttributeDefinition) parentDefn.getChildDefinition(name);
		} catch (Exception e) {
			defn = attributeDefinition(parentDefn, name, calculatedExpression, relevantExpression, validationExpression);
		}
		Attribute<?, ?> attr = (Attribute<?, ?>) defn.createNode();
		parent.add(attr);
		return attr;
	}

	protected Attribute<?, ?> calculatedAttribute(Entity parent, String name, String calculatedExpression) {
		return attribute(parent, name, calculatedExpression, null, null);
	}
		
	protected Entity entity(Entity parent, String name) {
		EntityDefinition parentDefn = parent.getDefinition();
		EntityDefinition defn;
		try {
			defn = (EntityDefinition) parentDefn.getChildDefinition(name);
		} catch (Exception e) {
			defn = entityDefinition(parentDefn, name);
		}
		Entity entity = (Entity) defn.createNode();
		parent.add(entity);
		return entity;
	}

	protected EntityDefinition entityDefinition(EntityDefinition parentDefn, String name) {
		EntityDefinition defn;
		defn = survey.getSchema().createEntityDefinition();
		defn.setName(name);
		parentDefn.addChildDefinition(defn);
		survey.refreshSurveyDependencies();
		return defn;
	}

	protected AttributeDefinition attributeDefinition(EntityDefinition parent, String name) {
		return attributeDefinition(parent, name, null, null, null);
	}
	
	protected AttributeDefinition calculatedAttributeDefinition(EntityDefinition parent, String name, String calculatedExpression) {
		return attributeDefinition(parent, name, calculatedExpression, null, null);
	}

	protected AttributeDefinition attributeDefinition(EntityDefinition parent, String name, String calculatedValueExpression, String relevantExpression, String validationExpression) {
		Schema schema = survey.getSchema();
		AttributeDefinition defn = schema.createTextAttributeDefinition();
		defn.setName(name);
		if ( calculatedValueExpression != null ) {
			defn.setCalculated(true);
			defn.addAttributeDefault(new AttributeDefault(calculatedValueExpression));
		}
		defn.setRelevantExpression(relevantExpression);
		if ( validationExpression != null ) {
			defn.addCheck(new CustomCheck(validationExpression));
		}
		parent.addChildDefinition(defn);
		survey.refreshSurveyDependencies();
		return defn;
	}

	private void createTestSurvey() {
		SurveyContext surveyContext = new TestSurveyContext();
		survey = surveyContext.createSurvey();
		Schema schema = survey.getSchema();
		rootEntityDef = schema.createEntityDefinition();
		rootEntityDef.setName(ROOT_ENTITY_NAME);
		schema.addRootEntityDefinition(rootEntityDef);
	}

	protected void createTestRecord() {
		String rootEntityName;
		List<EntityDefinition> rootEntityDefs = survey.getSchema().getRootEntityDefinitions();
		rootEntityName = rootEntityDefs.get(rootEntityDefs.size() - 1).getName();
		record = new Record(survey, null, rootEntityName);
		rootEntity = record.getRootEntity();
		rootEntityDef = rootEntity.getDefinition();
	}

	protected void assertDependents(Node<?> source, Node<?>... expectedDependents) {
		List<?> dependencies = determineDependents(source);
		
		assertEquals(toPaths(Arrays.asList(expectedDependents)), toPaths((List<Node<?>>) dependencies));
	}

	protected abstract List<?> determineDependents(Node<?> source);

	protected void assertCalculatedDependentsInAnyOrder(Node<?> source, Node<?>... expectedDependents) {
		List<?> dependencies = record.determineCalculatedAttributes(source);
		
		assertEquals(new HashSet(toPaths(Arrays.asList(expectedDependents))), new HashSet(toPaths((List<Node<?>>) dependencies)));
	}

	protected void assertBefore(List<Node<?>> attributes, Attribute<?, ?> z1, Attribute<?, ?> z2) {
		List<String> paths = toPaths(attributes);
		String path1 = z1.getPath();
		String path2 = z2.getPath();
		int i1 = paths.indexOf(path1);
		int i2 = paths.indexOf(path2);
		assertTrue(String.format("%s not found", path1), i1 >= 0);
		assertTrue(String.format("%s not found", path2), i2 >= 0);
		assertTrue(String.format("%s should be before %s", path1, path2), i1 < i2);
	}

	protected List<String> toPaths(List<Node<?>> nodes) {
		List<String> paths = new ArrayList<String>();
		for (Node<?> node : nodes) {
			paths.add(node.getPath());
		}
		return paths;
	}
	
}